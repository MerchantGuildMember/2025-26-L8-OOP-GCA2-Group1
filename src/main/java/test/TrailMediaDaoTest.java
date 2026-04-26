package test;

import DAO.JdbcTrailMediaDAO;
import org.junit.jupiter.api.*;
import tables.TrailMedia;
import shared.ServerResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Core unit tests for JdbcTrailMediaDAO.
 * Covers: displayAll, insert with auto-generated ID,
 * getById, and JSON round-trip.
 *
 * @author Maryna Hordiienko
 */
class TrailMediaDaoTest {

    // === Fields ===
    private static final String URL  = "jdbc:mysql://localhost:3306/oop_gca2";
    private static final String USER = "oop_gca2_user";
    private static final String PASS = "one";

    private JdbcTrailMediaDAO fDao;
    private TrailMedia fTestMedia;

    // Creates: a fresh DAO and known test object before each test
    @BeforeEach
    void setUp() {
        fDao = new JdbcTrailMediaDAO(URL, USER, PASS);
        fTestMedia = new TrailMedia(
                0L, 1L, null,
                "IMAGE",
                "http://test.com/img.jpg",
                "Test caption",
                LocalDateTime.of(2024, 1, 1, 12, 0),
                null, "", "", 0
        );
    }

    // Checks: displayAll returns a non-null non-empty list when seed data exists
    @Test
    void displayAll_returnsNonEmptyList_whenSeedDataExists() throws Exception {
        ServerResponse<ArrayList<TrailMedia>> response = fDao.displayAll();

        assertNotNull(response);
        assertEquals("Success", response.getStatus());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
    }

    // Checks: insert returns the saved entity with a positive auto-generated ID
    @Test
    void insert_returnsEntityWithGeneratedId_whenValidMediaProvided() throws Exception {
        TrailMedia inserted = fDao.insert(fTestMedia);

        assertNotNull(inserted);
        assertTrue(inserted.getId() > 0);
        assertEquals("IMAGE", inserted.getMedia_type());

        // Cleans up: removes test row so the database stays clean
        fDao.deleteById(inserted.getId());
    }

    // Checks: displayById returns the correct entity for a known inserted ID
    @Test
    void displayById_returnsCorrectEntity_whenIdExists() throws Exception {
        TrailMedia inserted = fDao.insert(fTestMedia);
        long id = inserted.getId();

        ServerResponse<TrailMedia> response = fDao.displayById(id);

        assertNotNull(response);
        assertEquals("Success", response.getStatus());
        assertNotNull(response.getData());
        assertEquals(id, response.getData().getId());

        // Cleans up: removes test row
        fDao.deleteById(id);
    }

    // Checks: displayById returns error response when the ID does not exist
    @Test
    void displayById_returnsErrorResponse_whenIdDoesNotExist() throws Exception {
        ServerResponse<TrailMedia> response = fDao.displayById(999_999L);

        assertNotNull(response);
        assertEquals("Error", response.getStatus());
        assertNull(response.getData());
    }

    // Checks: TrailMedia serialises to JSON and deserialises back with all fields preserved
    @Test
    void trailMedia_jsonRoundTrip_preservesAllFields() {
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonSerializer<LocalDateTime>)
                                (src, t, ctx) -> new com.google.gson.JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonDeserializer<LocalDateTime>)
                                (json, t, ctx) -> LocalDateTime.parse(json.getAsString()))
                .create();

        TrailMedia original = new TrailMedia(
                5L, 1L, null,
                "AUDIO",
                "http://test.com/sound.ogg",
                "Nature sounds",
                LocalDateTime.of(2024, 6, 15, 9, 30),
                null, "sound.ogg", "audio/ogg", 2048
        );

        // Converts: object to JSON string and back
        String json              = gson.toJson(original);
        TrailMedia reconstructed = gson.fromJson(json, TrailMedia.class);

        assertNotNull(reconstructed);
        assertEquals(original.getId(),             reconstructed.getId());
        assertEquals(original.getMedia_type(),     reconstructed.getMedia_type());
        assertEquals(original.getUrl(),            reconstructed.getUrl());
        assertEquals(original.getFFileName(),      reconstructed.getFFileName());
        assertEquals(original.getFContentType(),   reconstructed.getFContentType());
        assertEquals(original.getFFileSize(),      reconstructed.getFFileSize());
    }
}
