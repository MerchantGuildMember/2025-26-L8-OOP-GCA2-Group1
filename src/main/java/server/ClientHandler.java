package server;

import DAO.JdbcLocationDAO;
import DAO.JdbcRouteStopDAO;
import DAO.JdbcTrailDAO;
import DAO.JdbcTrailMediaDAO;
import DAO.LocationDAO;
import DAO.RouteStopDAO;
import DAO.TrailDAO;
import DAO.TrailMediaDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import shared.ServerResponse;
import tables.Location;
import tables.RouteStop;
import tables.Trail;
import tables.TrailMedia;
import utils.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Handles a single client connection on a dedicated thread.
 *
 * <p>Reads newline-delimited JSON requests, routes each to the correct DAO
 * method, and writes a {@link ServerResponse} JSON back on the same line.
 * Exceptions are caught here — they are never propagated to the client as
 * raw stack traces.</p>
 *
 * @author Maryna Hordiienko (primary)
 */
public class ClientHandler implements Runnable {

    // === Fields ===
    private final Socket fClientSocket;
    private final LocationDAO fLocationDAO;
    private final TrailDAO fTrailDAO;
    private final RouteStopDAO fRouteStopDAO;
    private final TrailMediaDAO fTrailMediaDAO;
    private final Gson fGson;

    // === Constructors ===
    // Creates: a ClientHandler bound to one socket with its own DAO instances
    public ClientHandler(Socket clientSocket, String dbUrl, String dbUser, String dbPass) {
        if (clientSocket == null)
            throw new IllegalArgumentException("clientSocket is required");
        if (dbUrl == null || dbUrl.isBlank())
            throw new IllegalArgumentException("dbUrl is required");

        fClientSocket = clientSocket;
        fLocationDAO = new JdbcLocationDAO(dbUrl, dbUser, dbPass);
        fTrailDAO = new JdbcTrailDAO(dbUrl, dbUser, dbPass);
        fRouteStopDAO = new JdbcRouteStopDAO(dbUrl, dbUser, dbPass);
        fTrailMediaDAO = new JdbcTrailMediaDAO(dbUrl, dbUser, dbPass);
        fGson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString()))
                .create();
    }

    // === Public API ===
    // Runs: the client request/response loop until the client disconnects
    @Override
    public void run() {
        String thread = Thread.currentThread().getName();
        System.out.println("[" + thread + "] Connected: " + fClientSocket.getInetAddress());

        try (Socket socket = fClientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank())
                    continue;
                System.out.println("[" + thread + "] << " + line);
                String response = dispatch(line);
                System.out.println("[" + thread + "] >> " + response);
                out.println(response);

                // F21: exit the loop cleanly when the client sends DISCONNECT
                // so the worker thread is released without waiting for EOF.
                if (isDisconnect(line)) {
                    System.out.println("[" + thread + "] Client requested DISCONNECT — closing connection.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[" + thread + "] IO error: " + e.getMessage());
        }

        System.out.println("[" + thread + "] Disconnected.");
    }

    // Checks: whether the given request line is a DISCONNECT action.
    // Returns false for malformed JSON so a bad line never takes the thread down.
    private boolean isDisconnect(String requestJson) {
        try {
            JsonObject req = JsonParser.parseString(requestJson).getAsJsonObject();
            return req.has("action") && "DISCONNECT".equals(req.get("action").getAsString());
        } catch (Exception e) {
            return false;
        }
    }

    // === Helpers ===
    // Dispatches: parses the action field and calls the matching handler method
    private String dispatch(String requestJson) {
        try {
            JsonObject req = JsonParser.parseString(requestJson).getAsJsonObject();
            String action = req.get("action").getAsString();

            switch (action) {

                // ── Location ──────────────────────────────────────────
                case "GET_ALL_LOCATIONS":
                    return toJson(fLocationDAO.displayAll());

                case "GET_LOCATION_BY_ID": {
                    long id = req.get("id").getAsLong();
                    return toJson(fLocationDAO.displayById(id));
                }

                case "ADD_LOCATION": {
                    Location loc = fGson.fromJson(req.get("data"), Location.class);
                    Location inserted = fLocationDAO.insert(loc);
                    return toJson(ServerResponse.ok("Location added", inserted));
                }

                case "UPDATE_LOCATION": {
                    Location loc = fGson.fromJson(req.get("data"), Location.class);
                    Location updated = fLocationDAO.update(loc);
                    return toJson(ServerResponse.ok("Location updated", updated));
                }

                case "DELETE_LOCATION": {
                    long id = req.get("id").getAsLong();
                    boolean ok = fLocationDAO.deleteById(id);
                    if (ok)
                        return toJson(ServerResponse.<Void>ok("Location deleted", null));
                    return toJson(ServerResponse.<Void>error("Location not found: " + id));
                }

                // ── Trail ─────────────────────────────────────────────
                case "GET_ALL_TRAILS":
                    return toJson(fTrailDAO.displayAll());

                case "GET_TRAIL_BY_ID": {
                    long id = req.get("id").getAsLong();
                    return toJson(fTrailDAO.displayById(id));
                }

                case "ADD_TRAIL": {
                    Trail trail = fGson.fromJson(req.get("data"), Trail.class);
                    Trail inserted = fTrailDAO.insert(trail);
                    return toJson(ServerResponse.ok("Trail added", inserted));
                }

                case "UPDATE_TRAIL": {
                    Trail trail = fGson.fromJson(req.get("data"), Trail.class);
                    Trail updated = fTrailDAO.update(trail);
                    return toJson(ServerResponse.ok("Trail updated", updated));
                }

                case "DELETE_TRAIL": {
                    long id = req.get("id").getAsLong();
                    boolean ok = fTrailDAO.deleteById(id);
                    if (ok)
                        return toJson(ServerResponse.<Void>ok("Trail deleted", null));
                    return toJson(ServerResponse.<Void>error("Trail not found: " + id));
                }

                // ── RouteStop ─────────────────────────────────────────
                case "GET_ALL_ROUTESTOPS":
                    return toJson(fRouteStopDAO.displayAll());

                case "GET_ROUTESTOP_BY_ID": {
                    long id = req.get("id").getAsLong();
                    return toJson(fRouteStopDAO.displayById(id));
                }

                case "ADD_ROUTESTOP": {
                    RouteStop rs = fGson.fromJson(req.get("data"), RouteStop.class);
                    RouteStop inserted = fRouteStopDAO.insert(rs);
                    return toJson(ServerResponse.ok("RouteStop added", inserted));
                }

                case "UPDATE_ROUTESTOP": {
                    RouteStop rs = fGson.fromJson(req.get("data"), RouteStop.class);
                    RouteStop updated = fRouteStopDAO.update(rs);
                    return toJson(ServerResponse.ok("RouteStop updated", updated));
                }

                case "DELETE_ROUTESTOP": {
                    long id = req.get("id").getAsLong();
                    boolean ok = fRouteStopDAO.deleteById(id);
                    if (ok)
                        return toJson(ServerResponse.<Void>ok("RouteStop deleted", null));
                    return toJson(ServerResponse.<Void>error("RouteStop not found: " + id));
                }

                // ── TrailMedia ────────────────────────────────────────
                case "GET_ALL_TRAILMEDIA":
                    return toJson(fTrailMediaDAO.displayAll());

                case "GET_TRAILMEDIA_BY_ID": {
                    long id = req.get("id").getAsLong();
                    return toJson(fTrailMediaDAO.displayById(id));
                }

                case "ADD_TRAILMEDIA": {
                    TrailMedia tm = fGson.fromJson(req.get("data"), TrailMedia.class);
                    TrailMedia inserted = fTrailMediaDAO.insert(tm);
                    return toJson(ServerResponse.ok("TrailMedia added", inserted));
                }

                case "UPDATE_TRAILMEDIA": {
                    TrailMedia tm = fGson.fromJson(req.get("data"), TrailMedia.class);
                    TrailMedia updated = fTrailMediaDAO.update(tm);
                    return toJson(ServerResponse.ok("TrailMedia updated", updated));
                }

                case "DELETE_TRAILMEDIA": {
                    long id = req.get("id").getAsLong();
                    boolean ok = fTrailMediaDAO.deleteById(id);
                    if (ok)
                        return toJson(ServerResponse.<Void>ok("TrailMedia deleted", null));
                    return toJson(ServerResponse.<Void>error("TrailMedia not found: " + id));
                }

                // ── Lifecycle ─────────────────────────────────────────
                case "DISCONNECT":
                    return toJson(ServerResponse.<Void>ok("Goodbye", null));

                default:
                    return toJson(ServerResponse.<Void>error("Unknown action: " + action));
            }

        } catch (Exception e) {
            return toJson(ServerResponse.<Void>error("Server error: " + e.getMessage()));
        }
    }

    // Converts: any object to a single-line JSON string using fGson
    private String toJson(Object obj) {
        return fGson.toJson(obj);
    }
}
