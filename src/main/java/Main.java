import client.ServerClient;
import tables.Location;
import tables.RouteStop;
import tables.Trail;
import tables.TrailMedia;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main method for interacting with the database in a visual manner.
 *
 * @author Aleksy Cieslak
 * @author Maryna Hordiienko
 */
public class Main {

    static ServerClient server = new ServerClient("localhost", 8080);
    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        welcomeMessage();
        displayMenu();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void welcomeMessage() throws InterruptedException {
        System.out.println("Welcome to Tour & Trail planning system!");
        for (int i = 2; i > 0; i--) {
            System.out.println("Moving on in " + i + " second(s)...");
            Thread.sleep(1000);
        }
        clearScreen();
    }

    /**
     * Read a trimmed line, consuming any leftover newline from a previous nextInt/nextDouble.
     */
    static String readLine() {
        return input.nextLine().trim();
    }

    static int readInt(int min, int max) {
        int value;
        while (true) {
            try {
                value = Integer.parseInt(readLine());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
            }
            System.out.print("Please enter a number between " + min + " and " + max + ": ");
        }
    }

    static long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Long.parseLong(readLine());
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid number, try again.");
            }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(readLine());
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid number, try again.");
            }
        }
    }

    static String send(String request) {
        try {
            return server.send(request);
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            return null;
        }
    }

    // ─── CREATE ─────────────────────────────────────────────────────────────────

    private static void addNewLocation() {
        System.out.println("\n── Add New Location ──");
        double lat = readDouble("Latitude:  ");
        double lon = readDouble("Longitude: ");
        System.out.print("Full Address: ");
        String addr = readLine();

        Location loc = new Location(null, lat, lon, addr, LocalDateTime.now());
        String response = send(server.buildActionWithData("ADD_LOCATION", loc));
        System.out.println("Server: " + response);
        createMenu();
    }

    private static void addNewRouteStop() {
        System.out.println("\n── Add New RouteStop ──");
        System.out.print("Route Name: ");
        String routeName = readLine();
        long locationId = readLong("Location ID: ");

        String locCheck = send(server.buildActionWithId("GET_LOCATION_BY_ID", locationId));
        if (locCheck == null) {
            System.out.println("Location not found.");
            createMenu();
            return;
        }
        System.out.println("Found location: " + locCheck);

        RouteStop rs = new RouteStop(0L, routeName, new Location(locationId, 0, 0, LocalDateTime.now()), LocalDateTime.now());
        String response = send(server.buildActionWithData("ADD_ROUTESTOP", rs));
        System.out.println("Server: " + response);
        createMenu();
    }

    private static void addNewTrail() {
        System.out.println("\n── Add New Trail ──");
        System.out.print("Name: ");
        String name = readLine();
        System.out.print("Description: ");
        String description = readLine();
        System.out.print("Difficulty: ");
        String difficulty = readLine();
        double estimatedTime = readDouble("Estimated Time (hours): ");

        System.out.print("How many stops? ");
        int stopCount = readInt(0, 100);

        ArrayList<RouteStop> stops = new ArrayList<>();
        for (int i = 0; i < stopCount; i++) {
            long stopId = readLong("  Stop " + (i + 1) + " ID: ");
            stops.add(new RouteStop(stopId, new Location(stopId, 0, 0, LocalDateTime.now()), LocalDateTime.now()));
        }

        Trail trail = new Trail(0L, name, description, difficulty, estimatedTime, stops);
        String response = send(server.buildActionWithData("ADD_TRAIL", trail));
        System.out.println("Server: " + response);
        createMenu();
    }

    private static void addNewTrailMedia() {
        System.out.println("\n── Add New TrailMedia ──");
        long trailId = readLong("Trail ID: ");

        System.out.print("Stop ID (blank = none): ");
        String stopRaw = readLine();
        Long stopId = stopRaw.isEmpty() ? null : Long.parseLong(stopRaw);

        System.out.print("Media Type (image/video): ");
        String mediaType = readLine();
        System.out.print("URL: ");
        String url = readLine();
        System.out.print("Caption: ");
        String caption = readLine();

        TrailMedia tm = new TrailMedia(0L, trailId, stopId, mediaType, url, caption, LocalDateTime.now());
        String response = send(server.buildActionWithData("ADD_TRAILMEDIA", tm));
        System.out.println("Server: " + response);
        createMenu();
    }

    // ─── READ ────────────────────────────────────────────────────────────────────

    private static void readEntity(String allAction, String byIdAction, String label) {
        System.out.println("\n── Read " + label + " ──");
        System.out.print("ALL or ID: ");
        String choice = readLine().toUpperCase();
        String response;
        if (choice.equals("ALL")) {
            response = send(server.buildAction(allAction));
        } else if (choice.equals("ID")) {
            long id = readLong("Input ID: ");
            response = send(server.buildActionWithId(byIdAction, id));
        } else {
            System.out.println("Invalid choice.");
            readMenu();
            return;
        }
        System.out.println(response);
        readMenu();
    }

    private static void readLocation() {
        readEntity("GET_ALL_LOCATIONS", "GET_LOCATION_BY_ID", "Location");
    }

    private static void readRouteStop() {
        readEntity("GET_ALL_ROUTESTOPS", "GET_ROUTESTOP_BY_ID", "RouteStop");
    }

    private static void readTrail() {
        readEntity("GET_ALL_TRAILS", "GET_TRAIL_BY_ID", "Trail");
    }

    private static void readTrailMedia() {
        readEntity("GET_ALL_TRAILMEDIA", "GET_TRAILMEDIA_BY_ID", "TrailMedia");
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────────

    private static void updateLocation() {
        System.out.println("\n── Update Location ──");
        long id = readLong("ID to update: ");
        System.out.println("Current: " + send(server.buildActionWithId("GET_LOCATION_BY_ID", id)));

        double lat = readDouble("New Latitude:  ");
        double lon = readDouble("New Longitude: ");
        System.out.print("New Full Address: ");
        String addr = readLine();

        Location loc = new Location(id, lat, lon, addr, LocalDateTime.now());
        System.out.println("Server: " + send(server.buildActionWithData("UPDATE_LOCATION", loc)));
        updateMenu();
    }

    private static void updateRouteStop() {
        System.out.println("\n── Update RouteStop ──");
        long id = readLong("ID to update: ");
        System.out.println("Current: " + send(server.buildActionWithId("GET_ROUTESTOP_BY_ID", id)));

        System.out.print("New Route Name: ");
        String routeName = readLine();
        long locationId = readLong("New Location ID: ");

        RouteStop rs = new RouteStop(id, routeName, new Location(locationId, 0, 0, LocalDateTime.now()), LocalDateTime.now());
        System.out.println("Server: " + send(server.buildActionWithData("UPDATE_ROUTESTOP", rs)));
        updateMenu();
    }

    private static void updateTrail() {
        System.out.println("\n── Update Trail ──");
        long id = readLong("ID to update: ");
        System.out.println("Current: " + send(server.buildActionWithId("GET_TRAIL_BY_ID", id)));

        System.out.print("New Name: ");
        String name = readLine();
        System.out.print("New Description: ");
        String description = readLine();
        System.out.print("New Difficulty: ");
        String difficulty = readLine();
        double estimatedTime = readDouble("New Estimated Time (hours): ");

        Trail trail = new Trail(id, name, description, difficulty, estimatedTime, new ArrayList<>());
        System.out.println("Server: " + send(server.buildActionWithData("UPDATE_TRAIL", trail)));
        updateMenu();
    }

    private static void updateTrailMedia() {
        System.out.println("\n── Update TrailMedia ──");
        long id = readLong("ID to update: ");
        System.out.println("Current: " + send(server.buildActionWithId("GET_TRAILMEDIA_BY_ID", id)));

        System.out.print("New Media Type: ");
        String mediaType = readLine();
        System.out.print("New URL: ");
        String url = readLine();
        System.out.print("New Caption: ");
        String caption = readLine();
        long trailId = readLong("New Trail ID: ");
        System.out.print("New Stop ID (blank = none): ");
        String stopRaw = readLine();
        Long stopId = stopRaw.isEmpty() ? null : Long.parseLong(stopRaw);

        TrailMedia tm = new TrailMedia(id, trailId, stopId, mediaType, url, caption, LocalDateTime.now());
        System.out.println("Server: " + send(server.buildActionWithData("UPDATE_TRAILMEDIA", tm)));
        updateMenu();
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────────

    private static void deleteEntity(String action, String label, Runnable returnMenu) {
        System.out.println("\n── Delete " + label + " ──");
        long id = readLong("ID to delete: ");
        System.out.print("Are you sure? (y/n): ");
        if (readLine().equalsIgnoreCase("y")) {
            System.out.println("Server: " + send(server.buildActionWithId(action, id)));
        } else {
            System.out.println("Cancelled.");
        }
        returnMenu.run();
    }

    private static void deleteLocation() {
        deleteEntity("DELETE_LOCATION", "Location", Main::deleteMenu);
    }

    private static void deleteRouteStop() {
        deleteEntity("DELETE_ROUTESTOP", "RouteStop", Main::deleteMenu);
    }

    private static void deleteTrail() {
        deleteEntity("DELETE_TRAIL", "Trail", Main::deleteMenu);
    }

    private static void deleteTrailMedia() {
        deleteEntity("DELETE_TRAILMEDIA", "TrailMedia", Main::deleteMenu);
    }

    // ─── MENUS ───────────────────────────────────────────────────────────────────

    static void displayMenu() {
        System.out.println("\n_________Menu_________");
        System.out.println("  1. Go to CRUD");
        System.out.println("  2. Exit");
        System.out.println("______________________");
        System.out.print("Input: ");
        int choice = readInt(1, 2);
        if (choice == 1) displayCRUD();
        else System.exit(0);
    }

    static void displayCRUD() {
        System.out.println("\n_______________________");
        System.out.println("  1. Create");
        System.out.println("  2. Read");
        System.out.println("  3. Update");
        System.out.println("  4. Delete");
        System.out.println("  5. Return");
        System.out.println("_______________________");
        System.out.print("Input: ");
        int choice = readInt(1, 5);
        switch (choice) {
            case 1 -> createMenu();
            case 2 -> readMenu();
            case 3 -> updateMenu();
            case 4 -> deleteMenu();
            case 5 -> displayMenu();
        }
    }

    static void createMenu() {
        System.out.println("\n___________Create___________");
        System.out.println("  1. Add new Location");
        System.out.println("  2. Add new RouteStop");
        System.out.println("  3. Add new Trail");
        System.out.println("  4. Add new TrailMedia");
        System.out.println("  5. Return");
        System.out.println("____________________________");
        System.out.print("Input: ");
        switch (readInt(1, 5)) {
            case 1 -> addNewLocation();
            case 2 -> addNewRouteStop();
            case 3 -> addNewTrail();
            case 4 -> addNewTrailMedia();
            case 5 -> displayCRUD();
        }
    }

    static void readMenu() {
        System.out.println("\n___________Read___________");
        System.out.println("  1. Read Location");
        System.out.println("  2. Read RouteStop");
        System.out.println("  3. Read Trail");
        System.out.println("  4. Read TrailMedia");
        System.out.println("  5. Return");
        System.out.println("__________________________");
        System.out.print("Input: ");
        switch (readInt(1, 5)) {
            case 1 -> readLocation();
            case 2 -> readRouteStop();
            case 3 -> readTrail();
            case 4 -> readTrailMedia();
            case 5 -> displayCRUD();
        }
    }

    static void updateMenu() {
        System.out.println("\n___________Update___________");
        System.out.println("  1. Update Location");
        System.out.println("  2. Update RouteStop");
        System.out.println("  3. Update Trail");
        System.out.println("  4. Update TrailMedia");
        System.out.println("  5. Return");
        System.out.println("____________________________");
        System.out.print("Input: ");
        switch (readInt(1, 5)) {
            case 1 -> updateLocation();
            case 2 -> updateRouteStop();
            case 3 -> updateTrail();
            case 4 -> updateTrailMedia();
            case 5 -> displayCRUD();
        }
    }

    static void deleteMenu() {
        System.out.println("\n___________Delete___________");
        System.out.println("  1. Delete Location");
        System.out.println("  2. Delete RouteStop");
        System.out.println("  3. Delete Trail");
        System.out.println("  4. Delete TrailMedia");
        System.out.println("  5. Return");
        System.out.println("____________________________");
        System.out.print("Input: ");
        switch (readInt(1, 5)) {
            case 1 -> deleteLocation();
            case 2 -> deleteRouteStop();
            case 3 -> deleteTrail();
            case 4 -> deleteTrailMedia();
            case 5 -> displayCRUD();
        }
    }
}