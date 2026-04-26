package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import shared.ServerResponse;
import tables.Location;
import tables.RouteStop;
import tables.Trail;
import tables.TrailMedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Interactive console client for the Trail Tracker application.
 *
 * <p>Provides a full menu-driven interface for all CRUD operations across
 * all four entities: Location, Trail, RouteStop, and TrailMedia.
 * All communication with the server uses JSON over a TCP socket.
 * All server replies are parsed as {@link ServerResponse} objects.</p>
 *
 * @author Maryna Hordiienko
 */
public class Client {

    // === Fields ===
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private final PrintWriter fOut;
    private final BufferedReader fIn;
    private final Scanner fScanner;
    private final Gson fGson;

    // === Constructors ===
    // Creates: a Client wired to the given socket streams
    public Client(PrintWriter out, BufferedReader in) {
        fOut = out;
        fIn = in;
        fScanner = new Scanner(System.in);
        fGson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString()))
                .create();
    }

    // === Public API ===
    // Creates: the application entry point — opens a socket and starts the menu
    public static void main(String[] args) {
        System.out.println("Connecting to " + HOST + ":" + PORT + " ...");

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected!\n");
            new Client(out, in).run();

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    // Runs: the main menu loop until the user chooses Exit
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n=============================");
            System.out.println("       TRAIL TRACKER");
            System.out.println("=============================");
            System.out.println("1 - Locations");
            System.out.println("2 - Trails");
            System.out.println("3 - Route Stops");
            System.out.println("4 - Trail Media");
            System.out.println("0 - Exit");
            System.out.print("Choose: ");

            switch (fScanner.nextLine().trim()) {
                case "1":
                    locationMenu();
                    break;
                case "2":
                    trailMenu();
                    break;
                case "3":
                    routeStopMenu();
                    break;
                case "4":
                    trailMediaMenu();
                    break;
                case "0":
                    running = false;
                    disconnect();
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // === Helpers ===

    // ─────────────────────────────────────────────────────────────────────
    // LOCATION
    // ─────────────────────────────────────────────────────────────────────

    // Displays: the Location CRUD sub-menu
    private void locationMenu() {
        System.out.println("\n--- Locations ---");
        System.out.println("1 - Display All");
        System.out.println("2 - Display by ID");
        System.out.println("3 - Add");
        System.out.println("4 - Update");
        System.out.println("5 - Delete");
        System.out.print("Choose: ");

        switch (fScanner.nextLine().trim()) {
            case "1":
                getAllLocations();
                break;
            case "2":
                getLocationById();
                break;
            case "3":
                addLocation();
                break;
            case "4":
                updateLocation();
                break;
            case "5":
                deleteLocation();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // Gets: all locations from the server and prints each one
    private void getAllLocations() {
        String raw = send(action("GET_ALL_LOCATIONS"));
        Type type = new TypeToken<ServerResponse<ArrayList<Location>>>() {
        }.getType();
        ServerResponse<ArrayList<Location>> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            resp.getData().forEach(loc -> System.out.println("  " + loc));
    }

    // Gets: one location by user-entered ID and prints it
    private void getLocationById() {
        long id = promptLong("Location ID: ");
        JsonObject req = action("GET_LOCATION_BY_ID");
        req.addProperty("id", id);
        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Location>>() {
        }.getType();
        ServerResponse<Location> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  " + resp.getData());
    }

    // Adds: a new location built from user input and prints the created record
    private void addLocation() {
        System.out.println("-- New Location --");
        double lat = promptDouble("Latitude:     ");
        double lon = promptDouble("Longitude:    ");
        String addr = promptString("Full address: ");

        Location loc = new Location(0L, lat, lon, addr, LocalDateTime.now());
        JsonObject req = action("ADD_LOCATION");
        req.add("data", fGson.toJsonTree(loc));

        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Location>>() {
        }.getType();
        ServerResponse<Location> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  Created: " + resp.getData());
    }

    // Updates: an existing location using user-entered values
    private void updateLocation() {
        System.out.println("-- Update Location --");
        long id = promptLong("Location ID to update: ");
        double lat = promptDouble("New latitude:     ");
        double lon = promptDouble("New longitude:    ");
        String addr = promptString("New full address: ");

        Location loc = new Location(id, lat, lon, addr, LocalDateTime.now());
        JsonObject req = action("UPDATE_LOCATION");
        req.add("data", fGson.toJsonTree(loc));

        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Location>>() {
        }.getType();
        ServerResponse<Location> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  Updated: " + resp.getData());
    }

    // Deletes: a location by user-entered ID
    private void deleteLocation() {
        long id = promptLong("Location ID to delete: ");
        JsonObject req = action("DELETE_LOCATION");
        req.addProperty("id", id);
        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Void>>() {
        }.getType();
        ServerResponse<Void> resp = fGson.fromJson(raw, type);
        printStatus(resp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // TRAIL
    // ─────────────────────────────────────────────────────────────────────

    // Displays: the Trail CRUD sub-menu
    private void trailMenu() {
        System.out.println("\n--- Trails ---");
        System.out.println("1 - Display All");
        System.out.println("2 - Display by ID");
        System.out.println("3 - Add");
        System.out.println("4 - Update");
        System.out.println("5 - Delete");
        System.out.print("Choose: ");

        switch (fScanner.nextLine().trim()) {
            case "1":
                getAllTrails();
                break;
            case "2":
                getTrailById();
                break;
            case "3":
                addTrail();
                break;
            case "4":
                updateTrail();
                break;
            case "5":
                deleteTrail();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // Gets: all trails from the server and prints each one
    private void getAllTrails() {
        String raw = send(action("GET_ALL_TRAILS"));
        Type type = new TypeToken<ServerResponse<ArrayList<Trail>>>() {
        }.getType();
        ServerResponse<ArrayList<Trail>> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            resp.getData().forEach(t -> System.out.println("  " + t));
    }

    // Gets: one trail by user-entered ID and prints it
    private void getTrailById() {
        long id = promptLong("Trail ID: ");
        JsonObject req = action("GET_TRAIL_BY_ID");
        req.addProperty("id", id);
        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Trail>>() {
        }.getType();
        ServerResponse<Trail> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  " + resp.getData());
    }

    // Adds: a new trail with stops built from user-entered route stop IDs
    private void addTrail() {
        System.out.println("-- New Trail --");
        String name = promptString("Name:           ");
        String description = promptString("Description:    ");
        String difficulty = promptString("Difficulty:     ");
        double estTime = promptDouble("Estimated time (hours): ");

        System.out.println("Enter route stop IDs for this trail (comma-separated, e.g. 1,2,3): ");
        String[] parts = fScanner.nextLine().trim().split(",");
        ArrayList<RouteStop> stops = new ArrayList<>();
        for (String part : parts) {
            try {
                long stopId = Long.parseLong(part.trim());
                // Creates: a minimal RouteStop shell — the server resolves full data via DAO
                stops.add(new RouteStop(stopId, null, null, LocalDateTime.now()));
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid stop ID: " + part.trim());
            }
        }

        if (stops.isEmpty()) {
            System.out.println("No valid stop IDs entered — trail not created.");
            return;
        }

        Trail trail = new Trail(0L, name, description, difficulty, estTime, stops);
        JsonObject req = action("ADD_TRAIL");
        req.add("data", fGson.toJsonTree(trail));

        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Trail>>() {
        }.getType();
        ServerResponse<Trail> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  Created: " + resp.getData());
    }

    // Updates: an existing trail using user-entered values
    private void updateTrail() {
        System.out.println("-- Update Trail --");
        long id = promptLong("Trail ID to update:  ");
        String name = promptString("New name:           ");
        String description = promptString("New description:    ");
        String difficulty = promptString("New difficulty:     ");
        double estTime = promptDouble("New estimated time: ");

        System.out.println("New route stop IDs (comma-separated): ");
        String[] parts = fScanner.nextLine().trim().split(",");
        ArrayList<RouteStop> stops = new ArrayList<>();
        for (String part : parts) {
            try {
                long stopId = Long.parseLong(part.trim());
                stops.add(new RouteStop(stopId, null, null, LocalDateTime.now()));
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid stop ID: " + part.trim());
            }
        }

        Trail trail = new Trail(id, name, description, difficulty, estTime, stops);
        JsonObject req = action("UPDATE_TRAIL");
        req.add("data", fGson.toJsonTree(trail));

        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Trail>>() {
        }.getType();
        ServerResponse<Trail> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  Updated: " + resp.getData());
    }

    // Deletes: a trail by user-entered ID
    private void deleteTrail() {
        long id = promptLong("Trail ID to delete: ");
        JsonObject req = action("DELETE_TRAIL");
        req.addProperty("id", id);
        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Void>>() {
        }.getType();
        ServerResponse<Void> resp = fGson.fromJson(raw, type);
        printStatus(resp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ROUTE STOP
    // ─────────────────────────────────────────────────────────────────────

    // Displays: the RouteStop CRUD sub-menu
    private void routeStopMenu() {
        System.out.println("\n--- Route Stops ---");
        System.out.println("1 - Display All");
        System.out.println("2 - Display by ID");
        System.out.println("3 - Add");
        System.out.println("4 - Update");
        System.out.println("5 - Delete");
        System.out.print("Choose: ");

        switch (fScanner.nextLine().trim()) {
            case "1":
                getAllRouteStops();
                break;
            case "2":
                getRouteStopById();
                break;
            case "3":
                addRouteStop();
                break;
            case "4":
                updateRouteStop();
                break;
            case "5":
                deleteRouteStop();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // Gets: all route stops from the server and prints each one
    private void getAllRouteStops() {
        String raw = send(action("GET_ALL_ROUTESTOPS"));
        Type type = new TypeToken<ServerResponse<ArrayList<RouteStop>>>() {
        }.getType();
        ServerResponse<ArrayList<RouteStop>> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            resp.getData().forEach(rs -> System.out.println("  " + rs));
    }

    // Gets: one route stop by user-entered ID and prints it
    private void getRouteStopById() {
        long id = promptLong("RouteStop ID: ");
        JsonObject req = action("GET_ROUTESTOP_BY_ID");
        req.addProperty("id", id);
        String raw = send(req);
        Type type = new TypeToken<ServerResponse<RouteStop>>() {
        }.getType();
        ServerResponse<RouteStop> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  " + resp.getData());
    }

    // Adds: a new route stop linked to an existing location ID
    private void addRouteStop() {
        System.out.println("-- New Route Stop --");
        String routeName = promptString("Route name:  ");
        long locationId = promptLong("Location ID: ");

        // Creates: a shell Location with only the ID — server uses it as FK
        Location locShell = new Location(locationId, 0.0, 0.0, LocalDateTime.now());
        RouteStop rs = new RouteStop(0L, routeName, locShell, LocalDateTime.now());
        JsonObject req = action("ADD_ROUTESTOP");
        req.add("data", fGson.toJsonTree(rs));

        String raw = send(req);
        Type type = new TypeToken<ServerResponse<RouteStop>>() {
        }.getType();
        ServerResponse<RouteStop> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  Created: " + resp.getData());
    }

    // Updates: an existing route stop using user-entered values
    private void updateRouteStop() {
        System.out.println("-- Update Route Stop --");
        long id = promptLong("RouteStop ID to update: ");
        String routeName = promptString("New route name:  ");
        long locationId = promptLong("New location ID: ");

        Location locShell = new Location(locationId, 0.0, 0.0, LocalDateTime.now());
        RouteStop rs = new RouteStop(id, routeName, locShell, LocalDateTime.now());
        JsonObject req = action("UPDATE_ROUTESTOP");
        req.add("data", fGson.toJsonTree(rs));

        String raw = send(req);
        Type type = new TypeToken<ServerResponse<RouteStop>>() {
        }.getType();
        ServerResponse<RouteStop> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  Updated: " + resp.getData());
    }

    // Deletes: a route stop by user-entered ID
    private void deleteRouteStop() {
        long id = promptLong("RouteStop ID to delete: ");
        JsonObject req = action("DELETE_ROUTESTOP");
        req.addProperty("id", id);
        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Void>>() {
        }.getType();
        ServerResponse<Void> resp = fGson.fromJson(raw, type);
        printStatus(resp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // TRAIL MEDIA
    // ─────────────────────────────────────────────────────────────────────

    // Displays: the TrailMedia CRUD sub-menu
    // Displays: the TrailMedia CRUD sub-menu
    private void trailMediaMenu() {
        System.out.println("\n--- Trail Media ---");
        System.out.println("1 - Display All");
        System.out.println("2 - Display by ID");
        System.out.println("3 - Add");
        System.out.println("4 - Update");
        System.out.println("5 - Delete");
        System.out.println("6 - Upload File F18");
        System.out.println("7 - Download File F19");
        System.out.println("8 - Get Metadata Only F20");
        System.out.print("Choose: ");

        switch (fScanner.nextLine().trim()) {
            case "1":
                getAllTrailMedia();
                break;
            case "2":
                getTrailMediaById();
                break;
            case "3":
                addTrailMedia();
                break;
            case "4":
                updateTrailMedia();
                break;
            case "5":
                deleteTrailMedia();
                break;
            case "6":
                uploadFile();
                break;
            case "7":
                downloadFile();
                break;
            case "8":
                getMetadata();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // Gets: all trail media from the server and prints each one
    private void getAllTrailMedia() {
        String raw = send(action("GET_ALL_TRAILMEDIA"));
        Type type = new TypeToken<ServerResponse<ArrayList<TrailMedia>>>() {
        }.getType();
        ServerResponse<ArrayList<TrailMedia>> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            resp.getData().forEach(tm -> System.out.println("  " + tm));
    }

    // Gets: one trail media record by user-entered ID and prints it
    private void getTrailMediaById() {
        long id = promptLong("TrailMedia ID: ");
        JsonObject req = action("GET_TRAILMEDIA_BY_ID");
        req.addProperty("id", id);
        String raw = send(req);
        Type type = new TypeToken<ServerResponse<TrailMedia>>() {
        }.getType();
        ServerResponse<TrailMedia> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  " + resp.getData());
    }

    // Adds: a new trail media record built from user input
    private void addTrailMedia() {
        System.out.println("-- New Trail Media --");
        long trailId = promptLong("Trail ID:         ");
        String stopInput = promptString("Stop ID (or blank for none): ");
        Long stopId = stopInput.isBlank() ? null : Long.parseLong(stopInput.trim());
        String mediaType = promptString("Media type (image/video/audio): ");
        String url = promptString("URL:              ");
        String caption = promptString("Caption:          ");

        TrailMedia tm = new TrailMedia(0L, trailId, stopId, mediaType, url, caption, LocalDateTime.now());
        JsonObject req = action("ADD_TRAILMEDIA");
        req.add("data", fGson.toJsonTree(tm));

        String raw = send(req);
        Type type = new TypeToken<ServerResponse<TrailMedia>>() {
        }.getType();
        ServerResponse<TrailMedia> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  Created: " + resp.getData());
    }

    // Updates: an existing trail media record using user-entered values
    private void updateTrailMedia() {
        System.out.println("-- Update Trail Media --");
        long id = promptLong("TrailMedia ID to update: ");
        long trailId = promptLong("New trail ID:            ");
        String stopInput = promptString("New stop ID (or blank for none): ");
        Long stopId = stopInput.isBlank() ? null : Long.parseLong(stopInput.trim());
        String mediaType = promptString("New media type:  ");
        String url = promptString("New URL:         ");
        String caption = promptString("New caption:     ");

        TrailMedia tm = new TrailMedia(id, trailId, stopId, mediaType, url, caption, LocalDateTime.now());
        JsonObject req = action("UPDATE_TRAILMEDIA");
        req.add("data", fGson.toJsonTree(tm));

        String raw = send(req);
        Type type = new TypeToken<ServerResponse<TrailMedia>>() {
        }.getType();
        ServerResponse<TrailMedia> resp = fGson.fromJson(raw, type);
        printStatus(resp);
        if (resp.isOk() && resp.getData() != null)
            System.out.println("  Updated: " + resp.getData());
    }

    // Deletes: a trail media record by user-entered ID
    private void deleteTrailMedia() {
        long id = promptLong("TrailMedia ID to delete: ");
        JsonObject req = action("DELETE_TRAILMEDIA");
        req.addProperty("id", id);
        String raw = send(req);
        Type type = new TypeToken<ServerResponse<Void>>() {
        }.getType();
        ServerResponse<Void> resp = fGson.fromJson(raw, type);
        printStatus(resp);
    }

    // ─────────────────────────────────────────────────────────────────────
    // BINARY FILE HANDLING
    // ─────────────────────────────────────────────────────────────────────

    // Uploads: a binary file from disk — reads bytes, Base64-encodes, sends to server (F18)
    private void uploadFile() {
        System.out.println("-- Upload File --");
        long trailId      = promptLong("Trail ID: ");
        String stopInput  = promptString("Stop ID (or blank for none): ");
        Long stopId       = stopInput.isBlank() ? null : Long.parseLong(stopInput.trim());
        String mediaType  = promptString("Media type (IMAGE/VIDEO/AUDIO): ");
        String filePath   = promptString("Full path to file (e.g. C:/files/photo.jpg): ");

        try {
            // Reads: the file from disk into a byte array
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            byte[] fileBytes        = java.nio.file.Files.readAllBytes(path);
            String fileName         = path.getFileName().toString();
            int fileSize            = fileBytes.length;

            // Detects: MIME type from file extension; falls back to octet-stream if unknown
            String contentType = java.nio.file.Files.probeContentType(path);
            if (contentType == null)
                contentType = "application/octet-stream";

            // Converts: raw bytes to Base64 string for safe JSON transport
            String base64 = java.util.Base64.getEncoder().encodeToString(fileBytes);

            JsonObject req = action("UPLOAD_FILE");
            req.addProperty("trailId",     trailId);
            req.addProperty("mediaType",   mediaType);
            req.addProperty("fileName",    fileName);
            req.addProperty("contentType", contentType);
            req.addProperty("fileSize",    fileSize);
            req.addProperty("fileData",    base64);
            if (stopId != null)
                req.addProperty("stopId", stopId);

            String raw  = send(req);
            Type   type = new TypeToken<ServerResponse<TrailMedia>>() {}.getType();
            ServerResponse<TrailMedia> resp = fGson.fromJson(raw, type);
            printStatus(resp);
            if (resp.isOk())
                System.out.println("  File uploaded successfully.");
        }
        catch (java.io.IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }

    // Downloads: a binary file from the server and saves it to disk (F19)
    private void downloadFile() {
        System.out.println("-- Download File --");
        long id          = promptLong("TrailMedia ID: ");
        String outputDir = promptString("Save to folder (e.g. C:/downloads): ");

        JsonObject req = action("GET_FILE");
        req.addProperty("id", id);
        String raw = send(req);

        Type type = new TypeToken<ServerResponse<com.google.gson.JsonObject>>() {}.getType();
        ServerResponse<com.google.gson.JsonObject> resp = fGson.fromJson(raw, type);
        printStatus(resp);

        if (!resp.isOk() || resp.getData() == null)
            return;

        com.google.gson.JsonObject payload = resp.getData();
        String fileName = payload.get("fileName").getAsString();
        String base64   = payload.get("fileData").getAsString();

        // Converts: Base64 string back to raw bytes
        byte[] fileBytes = java.util.Base64.getDecoder().decode(base64);

        // Saves: reconstructed file to disk preserving the original filename
        try {
            java.nio.file.Path outPath = java.nio.file.Paths.get(outputDir, fileName);
            java.nio.file.Files.write(outPath, fileBytes);
            System.out.println("  File saved to: " + outPath.toAbsolutePath());
        }
        catch (java.io.IOException e) {
            System.out.println("Could not save file: " + e.getMessage());
        }
    }

    // Gets: metadata only for a stored file — no binary data downloaded (F20)
    private void getMetadata() {
        System.out.println("-- File Metadata --");
        long id = promptLong("TrailMedia ID: ");

        JsonObject req = action("GET_METADATA");
        req.addProperty("id", id);
        String raw = send(req);

        Type type = new TypeToken<ServerResponse<com.google.gson.JsonObject>>() {}.getType();
        ServerResponse<com.google.gson.JsonObject> resp = fGson.fromJson(raw, type);
        printStatus(resp);

        if (resp.isOk() && resp.getData() != null) {
            com.google.gson.JsonObject meta = resp.getData();
            System.out.println("  ID:           " + meta.get("id").getAsLong());
            System.out.println("  File name:    " + meta.get("fileName").getAsString());
            System.out.println("  Content type: " + meta.get("contentType").getAsString());
            System.out.println("  File size:    " + meta.get("fileSize").getAsInt() + " bytes");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TRANSPORT + SHARED HELPERS
    // ─────────────────────────────────────────────────────────────────────

    // Sends: one JSON request line and returns the server's response line
    private String send(JsonObject request) {
        try {
            fOut.println(fGson.toJson(request));
            return fIn.readLine();
        } catch (IOException e) {
            return fGson.toJson(ServerResponse.error("IO error: " + e.getMessage()));
        }
    }

    // Sends: a DISCONNECT request before closing so the server can log it cleanly
    private void disconnect() {
        send(action("DISCONNECT"));
        System.out.println("Disconnected. Goodbye!");
    }

    // Builds: a JsonObject containing only the given action string
    private static JsonObject action(String actionName) {
        JsonObject obj = new JsonObject();
        obj.addProperty("action", actionName);
        return obj;
    }

    // Prints: the status and message fields of any ServerResponse
    private static void printStatus(ServerResponse<?> resp) {
        System.out.println("[" + resp.getStatus() + "] " + resp.getMessage());
    }

    // Gets: a long entered by the user, retrying until input is valid
    private long promptLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Long.parseLong(fScanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Gets: a double entered by the user, retrying until input is valid
    private double promptDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(fScanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Gets: a non-blank string entered by the user, retrying if blank
    private String promptString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = fScanner.nextLine().trim();
            if (!value.isBlank())
                return value;
            System.out.println("Value cannot be blank.");
        }
    }
}
