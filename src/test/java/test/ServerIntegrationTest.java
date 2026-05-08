package test;

import DAO.JdbcTrailMediaDAO;
import client.ServerClient;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import shared.ServerResponse;
import tables.TrailMedia;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Server integration tests for Trail Tracker — Stage 4 (F23).
 *
 * <p>Sends real JSON requests to the running server over a TCP socket
 * and asserts the responses are correct. The server must be running
 * on localhost:8080 before these tests are executed.</p>
 *
 * <p>Covers: GET_ALL_TRAILMEDIA, GET_TRAILMEDIA_BY_ID, ADD_TRAILMEDIA,
 * DELETE_TRAILMEDIA, UPLOAD_FILE (binary upload), GET_FILE (binary retrieval),
 * GET_METADATA, and DISCONNECT.</p>
 *
 * @author Maryna Hordiienko
 */
class ServerIntegrationTest {

    // === Fields ===
    private static final String HOST = "localhost";
    private static final int    PORT = 8080;

    private ServerClient fClient;
    private Gson         fGson;

    // Creates: a fresh ServerClient and Gson instance before each test
    @BeforeEach
    void setUp() {
        fClient = new ServerClient(HOST, PORT);
        fGson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>)
                                (src, t, ctx) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>)
                                (json, t, ctx) -> LocalDateTime.parse(json.getAsString()))
                .create();
    }

    // ── GET_ALL_TRAILMEDIA ────────────────────────────────────────────────

    // Checks: server returns OK response with a non-empty list for GET_ALL_TRAILMEDIA
    @Test
    void server_getAllTrailMedia_returnsOkWithNonEmptyList() throws Exception {
        String request = fClient.buildAction("GET_ALL_TRAILMEDIA");
        String raw     = fClient.send(request);

        assertNotNull(raw, "Server response must not be null");

        Type type = new TypeToken<ServerResponse<ArrayList<TrailMedia>>>() {}.getType();
        ServerResponse<ArrayList<TrailMedia>> response = fGson.fromJson(raw, type);

        assertTrue(response.isOk(), "Response status must be OK");
        assertNotNull(response.getData(), "Data must not be null");
        assertFalse(response.getData().isEmpty(), "List must contain seed data records");
    }

    // ── GET_TRAILMEDIA_BY_ID ──────────────────────────────────────────────

    // Checks: server returns OK and correct entity when requested by valid ID
    @Test
    void server_getTrailMediaById_returnsCorrectEntity_whenIdExists() throws Exception {
        // Uses: seed data ID 1 which is always present after mysqlSetup.sql
        String request = fClient.buildActionWithId("GET_TRAILMEDIA_BY_ID", 1L);
        String raw     = fClient.send(request);

        assertNotNull(raw);

        Type type = new TypeToken<ServerResponse<TrailMedia>>() {}.getType();
        ServerResponse<TrailMedia> response = fGson.fromJson(raw, type);

        assertTrue(response.isOk(), "Response status must be OK for existing ID");
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
    }

    // Checks: server returns ERROR response for a non-existent ID
    @Test
    void server_getTrailMediaById_returnsError_whenIdDoesNotExist() throws Exception {
        String request = fClient.buildActionWithId("GET_TRAILMEDIA_BY_ID", 999_999L);
        String raw     = fClient.send(request);

        assertNotNull(raw);

        Type type = new TypeToken<ServerResponse<TrailMedia>>() {}.getType();
        ServerResponse<TrailMedia> response = fGson.fromJson(raw, type);

        assertFalse(response.isOk(), "Response must not be OK for non-existent ID");
    }

    // ── ADD_TRAILMEDIA and DELETE_TRAILMEDIA ──────────────────────────────

    // Checks: server inserts a new TrailMedia record and returns it with a generated ID
    @Test
    void server_addTrailMedia_returnsInsertedEntityWithGeneratedId() throws Exception {
        TrailMedia newMedia = new TrailMedia(
                0L, 1L, null,
                "IMAGE",
                "http://server-test.com/img.jpg",
                "Server test caption",
                LocalDateTime.of(2024, 7, 1, 10, 0),
                null, "", "", 0
        );

        String request = fClient.buildActionWithData("ADD_TRAILMEDIA", newMedia);
        String raw     = fClient.send(request);

        assertNotNull(raw);

        Type type = new TypeToken<ServerResponse<TrailMedia>>() {}.getType();
        ServerResponse<TrailMedia> response = fGson.fromJson(raw, type);

        assertTrue(response.isOk(), "Response status must be OK for successful insert");
        assertNotNull(response.getData(), "Inserted entity must not be null");
        assertTrue(response.getData().getId() > 0, "Auto-generated ID must be positive");

        // Cleans up: sends delete request to remove the test record
        long insertedId = response.getData().getId();
        String deleteRequest = fClient.buildActionWithId("DELETE_TRAILMEDIA", insertedId);
        fClient.send(deleteRequest);
    }

    // ── UPLOAD_FILE and GET_FILE (binary round-trip) ──────────────────────

    // Checks: binary file upload stores bytes on the server and retrieval returns exact bytes
    @Test
    void server_uploadFile_andGetFile_bytesMatchExactly() throws Exception {
        // Creates: known binary content simulating an image file
        byte[] originalBytes = "SERVER_INTEGRATION_TEST_BINARY_DATA".getBytes();
        String base64Encoded = Base64.getEncoder().encodeToString(originalBytes);

        // Builds: UPLOAD_FILE request with Base64 payload (F18)
        JsonObject uploadReq = new JsonObject();
        uploadReq.addProperty("action",      "UPLOAD_FILE");
        uploadReq.addProperty("trailId",     1L);
        uploadReq.addProperty("mediaType",   "IMAGE");
        uploadReq.addProperty("fileName",    "server_test.png");
        uploadReq.addProperty("contentType", "image/png");
        uploadReq.addProperty("fileSize",    originalBytes.length);
        uploadReq.addProperty("fileData",    base64Encoded);

        String uploadRaw = fClient.send(fGson.toJson(uploadReq));
        assertNotNull(uploadRaw, "Upload response must not be null");

        Type tmType = new TypeToken<ServerResponse<TrailMedia>>() {}.getType();
        ServerResponse<TrailMedia> uploadResponse = fGson.fromJson(uploadRaw, tmType);

        assertTrue(uploadResponse.isOk(), "Upload must return OK. Got: " + uploadRaw);
        assertNotNull(uploadResponse.getData(), "Upload response data must not be null");
        long uploadedId = uploadResponse.getData().getId();
        assertTrue(uploadedId > 0, "Uploaded record must have a positive ID");

        // Retrieves: the record directly from DAO to verify bytes (F19)
        String DB_URL = "jdbc:mysql://localhost:3306/oop_gca2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        JdbcTrailMediaDAO dao = new JdbcTrailMediaDAO(DB_URL, "oop_gca2_user", "one");
        ServerResponse<TrailMedia> dbResponse = dao.displayById(uploadedId);

        assertNotNull(dbResponse.getData(), "Record must exist in database after upload");
        byte[] retrievedBytes = dbResponse.getData().getFFileData();

        // Verifies: the bytes in the database exactly match the bytes that were uploaded
        assertNotNull(retrievedBytes, "File data must not be null after upload");
        assertArrayEquals(originalBytes, retrievedBytes,
                "Bytes stored in DB must exactly match bytes sent in UPLOAD_FILE");

        // Cleans up: deletes the test record
        String deleteRequest = fClient.buildActionWithId("DELETE_TRAILMEDIA", uploadedId);
        fClient.send(deleteRequest);
    }

    // Checks: metadata query returns fields without BLOB — verified via DAO (F20)
    @Test
    void server_getMetadata_returnsMetadataOnly_withoutBlobField() throws Exception {
        // Uploads: a file via server so we have a real record with metadata
        byte[] fileBytes = "METADATA_TEST".getBytes();
        String base64    = Base64.getEncoder().encodeToString(fileBytes);

        JsonObject uploadReq = new JsonObject();
        uploadReq.addProperty("action",      "UPLOAD_FILE");
        uploadReq.addProperty("trailId",     1L);
        uploadReq.addProperty("mediaType",   "IMAGE");
        uploadReq.addProperty("fileName",    "meta_test.jpg");
        uploadReq.addProperty("contentType", "image/jpeg");
        uploadReq.addProperty("fileSize",    fileBytes.length);
        uploadReq.addProperty("fileData",    base64);

        String uploadRaw = fClient.send(fGson.toJson(uploadReq));
        assertNotNull(uploadRaw, "Upload response must not be null");

        Type tmType = new TypeToken<ServerResponse<TrailMedia>>() {}.getType();
        ServerResponse<TrailMedia> uploadResp = fGson.fromJson(uploadRaw, tmType);

        assertTrue(uploadResp.isOk(), "Upload must succeed. Got: " + uploadRaw);
        assertNotNull(uploadResp.getData(), "Upload must return inserted entity");
        long uploadedId = uploadResp.getData().getId();
        assertTrue(uploadedId > 0, "Uploaded ID must be positive");

        // Gets: metadata only via DAO — BLOB column deliberately not fetched (F20)
        String DB_URL = "jdbc:mysql://localhost:3306/oop_gca2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        JdbcTrailMediaDAO dao = new JdbcTrailMediaDAO(DB_URL, "oop_gca2_user", "one");
        ServerResponse<TrailMedia> metaResponse = dao.getMetadataById(uploadedId);

        assertTrue(metaResponse.isOk(), "getMetadataById must return OK");
        assertNotNull(metaResponse.getData(), "Metadata must not be null");

        TrailMedia meta = metaResponse.getData();

        // Verifies: metadata fields are present
        assertEquals("meta_test.jpg", meta.getFFileName());
        assertEquals("image/jpeg",    meta.getFContentType());
        assertEquals(fileBytes.length, meta.getFFileSize());

        // Verifies: BLOB is null — not loaded in metadata-only query
        assertNull(meta.getFFileData(), "BLOB data must be null in metadata-only query (F20)");

        // Cleans up
        String deleteRequest = fClient.buildActionWithId("DELETE_TRAILMEDIA", uploadedId);
        fClient.send(deleteRequest);
    }

    // ── DISCONNECT ────────────────────────────────────────────────────────

    // Checks: server responds to DISCONNECT with a Goodbye message (F21)
    @Test
    void server_disconnect_returnsGoodbyeResponse() throws Exception {
        String request = fClient.buildAction("DISCONNECT");
        String raw     = fClient.send(request);

        assertNotNull(raw, "DISCONNECT response must not be null");

        Type type = new TypeToken<ServerResponse<Void>>() {}.getType();
        ServerResponse<Void> response = fGson.fromJson(raw, type);

        assertTrue(response.isOk(), "DISCONNECT response must be OK");
        assertTrue(response.getMessage().contains("Goodbye"),
                "DISCONNECT message must say Goodbye");
    }

    // ── Unknown action ────────────────────────────────────────────────────

    // Checks: server returns an error for an unknown action type
    @Test
    void server_unknownAction_returnsErrorResponse() throws Exception {
        String request = fClient.buildAction("TOTALLY_UNKNOWN_ACTION");
        String raw     = fClient.send(request);

        assertNotNull(raw);

        Type type = new TypeToken<ServerResponse<Void>>() {}.getType();
        ServerResponse<Void> response = fGson.fromJson(raw, type);

        assertFalse(response.isOk(), "Server must return ERROR for unknown action types");
        assertTrue(response.getMessage().contains("Unknown"),
                "Error message must identify the unknown action");
    }
}
