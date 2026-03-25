import DAO.*;
import client.ServerClient;
import shared.ServerResponse;
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
 *
 *
 */

public class Main {

//    private static final String URL  = System.getenv("URL");
//    private static final String USER = System.getenv("USER");
//    private static final String PASS = System.getenv("PASS");


    static ServerClient server = new ServerClient("localhost", 8080);
    static Scanner input = new Scanner(System.in);



    public static void main(String[] args) throws Exception {

//        welcomeMessage();

        displayMenu();


    }
    static void welcomeMessage() throws Exception {
        System.out.println("Welcome to Tour & Trail planning system!");
        for(int i = 2; i > 0; i--) {
            System.out.println("Moving on in "+i+" seconds...");
            Thread.sleep(1000);
        }
        clearScreen();
    }


//       _____ _____  ______       _______ ______
//      / ____|  __ \|  ____|   /\|__   __|  ____|
//     | |    | |__) | |__     /  \  | |  | |__
//     | |    |  _  /|  __|   / /\ \ | |  |  __|
//     | |____| | \ \| |____ / ____ \| |  | |____
//      \_____|_|  \_\______/_/    \_\_|  |______|
//
//

    private static void addNewLocation() {
        System.out.println("Entering New Location");

        System.out.print("\nLatitude: ");
        Double latitude = input.nextDouble(); input.nextLine();

        System.out.print("\nLongitude: ");
        Double longitude = input.nextDouble(); input.nextLine();

        System.out.print("\nFull Address: ");
        String fullAddress = input.nextLine();

        LocalDateTime time = LocalDateTime.now();
        System.out.println("\nInserting Location: "+latitude+ ", "+longitude+", \""+fullAddress+"\", "+time);

        Location newLocation = new Location(null, latitude, longitude, fullAddress, time);
        try {
            server.send(server.buildActionWithData("ADD_LOCATION", newLocation));
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert location");
        }
        createMenu();
    }
    private static void addNewRouteStop() {
        System.out.println("Entering New RouteStop");

        System.out.print("\nRoute Name: ");
        String routeName = input.nextLine();

        System.out.print("\nLocation ID: ");
        Long locationId = input.nextLong(); input.nextLine();

        LocalDateTime time = LocalDateTime.now();

        try {
            String locResponse = server.send(server.buildActionWithId("GET_LOCATION_BY_ID", locationId));
            System.out.println("\nServer response: " + locResponse);

            RouteStop newRouteStop = new RouteStop(0L, routeName, new Location(locationId, 0, 0, time), time);
            String response = server.send(server.buildActionWithData("ADD_ROUTESTOP", newRouteStop));
            System.out.println("\nInserted RouteStop: " + response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert RouteStop: " + e.getMessage());
        }
        createMenu();
    }
    private static void addNewTrail() {
        System.out.println("Entering New Trail");

        System.out.print("\nName: ");
        String name = input.nextLine();

        System.out.print("\nDescription: ");
        String description = input.nextLine();

        System.out.print("\nDifficulty: ");
        String difficulty = input.nextLine();

        System.out.print("\nEstimated Time (hours): ");
        Double estimatedTime = input.nextDouble(); input.nextLine();

        System.out.print("\nHow many stops? ");
        int stopCount = input.nextInt(); input.nextLine();

        ArrayList<RouteStop> stops = new ArrayList<>();
        for (int i = 0; i < stopCount; i++) {
            System.out.print("Stop " + (i + 1) + " ID: ");
            Long stopId = input.nextLong(); input.nextLine();
            stops.add(new RouteStop(stopId, new Location(stopId, 0, 0, LocalDateTime.now()), LocalDateTime.now()));
        }

        if (stops.isEmpty()) {
            System.out.println("No valid stops found, trail not created.");
            createMenu();
            return;
        }

        try {
            Trail newTrail = new Trail(0L, name, description, difficulty, estimatedTime, stops);
            String response = server.send(server.buildActionWithData("ADD_TRAIL", newTrail));
            System.out.println("\nServer response: " + response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert Trail: " + e.getMessage());
        }
        createMenu();
    }
    private static void addNewTrailMedia() {
        System.out.println("Entering New TrailMedia");

        System.out.print("\nTrail ID: ");
        Long trailId = input.nextLong(); input.nextLine();

        System.out.print("\nStop ID (leave blank if none): ");
        String stopInput = input.nextLine();
        Long stopId = stopInput.isBlank() ? null : Long.parseLong(stopInput);

        System.out.print("\nMedia Type (image/video): ");
        String mediaType = input.nextLine();

        System.out.print("\nURL: ");
        String url = input.nextLine();

        System.out.print("\nCaption: ");
        String caption = input.nextLine();

        LocalDateTime time = LocalDateTime.now();
        System.out.println("\nInserting TrailMedia: " + mediaType + ", \"" + url + "\", " + time);

        TrailMedia newTrailMedia = new TrailMedia(0L, trailId, stopId, mediaType, url, caption, time);
        try {
            String response = server.send(server.buildActionWithData("ADD_TRAILMEDIA", newTrailMedia));
            System.out.println("\nServer response: " + response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert TrailMedia: " + e.getMessage());
        }
        createMenu();
    }

//      _____  ______          _____
//     |  __ \|  ____|   /\   |  __ \
//     | |__) | |__     /  \  | |  | |
//     |  _  /|  __|   / /\ \ | |  | |
//     | | \ \| |____ / ____ \| |__| |
//     |_|  \_\______/_/    \_\_____/
//
//

    private static void readLocation() {
        System.out.println("ID | ALL");
        String choice = input.nextLine();
        try {
            if (choice.equals("ALL")) {
                String response = server.send(server.buildAction("GET_ALL_LOCATIONS"));
                System.out.println(response);
            } else if (choice.equals("ID")) {
                System.out.print("Input ID: ");
                Long id = input.nextLong(); input.nextLine();
                String response = server.send(server.buildActionWithId("GET_LOCATION_BY_ID", id));
                System.out.println(response);
            } else {
                System.out.println("Invalid choice");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        readMenu();
    }
    private static void readTrail() {
        System.out.println("ID | ALL");
        String choice = input.nextLine();
        try {
            if (choice.equals("ALL")) {
                String response = server.send(server.buildAction("GET_ALL_TRAILS"));
                System.out.println(response);
            } else if (choice.equals("ID")) {
                System.out.print("Input ID: ");
                Long id = input.nextLong(); input.nextLine();
                String response = server.send(server.buildActionWithId("GET_TRAIL_BY_ID", id));
                System.out.println(response);
            } else {
                System.out.println("Invalid choice");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        readMenu();
    }
    private static void readTrailMedia() {
        System.out.println("ID | ALL");
        String choice = input.nextLine();
        try {
            if (choice.equals("ALL")) {
                String response = server.send(server.buildAction("GET_ALL_TRAILMEDIA"));
                System.out.println(response);
            } else if (choice.equals("ID")) {
                System.out.print("Input ID: ");
                Long id = input.nextLong(); input.nextLine();
                String response = server.send(server.buildActionWithId("GET_TRAILMEDIA_BY_ID", id));
                System.out.println(response);
            } else {
                System.out.println("Invalid choice");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        readMenu();
    }
    private static void readRouteStop() {
        System.out.println("ID | ALL");
        String choice = input.nextLine();
        try {
            if (choice.equals("ALL")) {
                String response = server.send(server.buildAction("GET_ALL_ROUTESTOPS"));
                System.out.println(response);
            } else if (choice.equals("ID")) {
                System.out.print("Input ID: ");
                Long id = input.nextLong(); input.nextLine();
                String response = server.send(server.buildActionWithId("GET_ROUTESTOP_BY_ID", id));
                System.out.println(response);
            } else {
                System.out.println("Invalid choice");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        readMenu();
    }

//      _    _ _____  _____       _______ ______
//     | |  | |  __ \|  __ \   /\|__   __|  ____|
//     | |  | | |__) | |  | | /  \  | |  | |__
//     | |  | |  ___/| |  | |/ /\ \ | |  |  __|
//     | |__| | |    | |__| / ____ \| |  | |____
//      \____/|_|    |_____/_/    \_\_|  |______|
//
//

    private static void updateLocation() {
        System.out.println("Enter ID of entity you wish to update: ");
        Long id = input.nextLong(); input.nextLine();

        try {
            String current = server.send(server.buildActionWithId("GET_LOCATION_BY_ID", id));
            System.out.println("Current: " + current);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.print("\nLatitude: ");
        Double latitude = input.nextDouble(); input.nextLine();
        System.out.print("\nLongitude: ");
        Double longitude = input.nextDouble(); input.nextLine();
        System.out.print("\nFull Address: ");
        String fullAddress = input.nextLine();

        Location updated = new Location(id, latitude, longitude, fullAddress, LocalDateTime.now());
        try {
            String response = server.send(server.buildActionWithData("UPDATE_LOCATION", updated));
            System.out.println("\nServer response: " + response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update location: " + e.getMessage());
        }
        updateMenu();
    }
    private static void updateTrail() {
        System.out.println("Enter ID of entity you wish to update: ");
        Long id = input.nextLong(); input.nextLine();

        try {
            String current = server.send(server.buildActionWithId("GET_TRAIL_BY_ID", id));
            System.out.println("Current: " + current);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.print("\nName: ");
        String name = input.nextLine();
        System.out.print("\nDescription: ");
        String description = input.nextLine();
        System.out.print("\nDifficulty: ");
        String difficulty = input.nextLine();
        System.out.print("\nEstimated Time (hours): ");
        Double estimatedTime = input.nextDouble(); input.nextLine();

        Trail updated = new Trail(id, name, description, difficulty, estimatedTime, new ArrayList<>());
        try {
            String response = server.send(server.buildActionWithData("UPDATE_TRAIL", updated));
            System.out.println("\nServer response: " + response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update trail: " + e.getMessage());
        }
        updateMenu();
    }
    private static void updateTrailMedia() {
        System.out.println("Enter ID of entity you wish to update: ");
        Long id = input.nextLong(); input.nextLine();

        try {
            String current = server.send(server.buildActionWithId("GET_TRAILMEDIA_BY_ID", id));
            System.out.println("Current: " + current);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.print("\nMedia Type: ");
        String mediaType = input.nextLine();
        System.out.print("\nURL: ");
        String url = input.nextLine();
        System.out.print("\nCaption: ");
        String caption = input.nextLine();
        System.out.print("\nTrail ID: ");
        Long trailId = input.nextLong(); input.nextLine();
        System.out.print("\nStop ID (leave blank if none): ");
        String stopInput = input.nextLine();
        Long stopId = stopInput.isBlank() ? null : Long.parseLong(stopInput);

        TrailMedia updated = new TrailMedia(id, trailId, stopId, mediaType, url, caption, LocalDateTime.now());
        try {
            String response = server.send(server.buildActionWithData("UPDATE_TRAILMEDIA", updated));
            System.out.println("\nServer response: " + response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update TrailMedia: " + e.getMessage());
        }
        updateMenu();
    }
    private static void updateRouteStop() {
        System.out.println("Enter ID of entity you wish to update: ");
        Long id = input.nextLong(); input.nextLine();

        try {
            String current = server.send(server.buildActionWithId("GET_ROUTESTOP_BY_ID", id));
            System.out.println("Current: " + current);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.print("\nRoute Name: ");
        String routeName = input.nextLine();
        System.out.print("\nLocation ID: ");
        Long locationId = input.nextLong(); input.nextLine();

        RouteStop updated = new RouteStop(id, routeName, new Location(locationId, 0, 0, LocalDateTime.now()), LocalDateTime.now());
        try {
            String response = server.send(server.buildActionWithData("UPDATE_ROUTESTOP", updated));
            System.out.println("\nServer response: " + response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update RouteStop: " + e.getMessage());
        }
        updateMenu();
    }


//      _____  ______ _      ______ _______ ______
//     |  __ \|  ____| |    |  ____|__   __|  ____|
//     | |  | | |__  | |    | |__     | |  | |__
//     | |  | |  __| | |    |  __|    | |  |  __|
//     | |__| | |____| |____| |____   | |  | |____
//     |_____/|______|______|______|  |_|  |______|
//
//

    private static void deleteLocation() {
        System.out.println("Enter ID of location to delete: ");
        Long id = input.nextLong(); input.nextLine();
        System.out.print("Are you sure? (y/n): ");
        String confirm = input.nextLine();
        if (confirm.equalsIgnoreCase("y")) {
            try {
                String response = server.send(server.buildActionWithId("DELETE_LOCATION", id));
                System.out.println("Server response: " + response);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete location: " + e.getMessage());
            }
        } else {
            System.out.println("Cancelled.");
        }
        deleteMenu();
    }
    private static void deleteRouteStop() {
        System.out.println("Enter ID of RouteStop to delete: ");
        Long id = input.nextLong(); input.nextLine();
        System.out.print("Are you sure? (y/n): ");
        String confirm = input.nextLine();
        if (confirm.equalsIgnoreCase("y")) {
            try {
                String response = server.send(server.buildActionWithId("DELETE_ROUTESTOP", id));
                System.out.println("Server response: " + response);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete RouteStop: " + e.getMessage());
            }
        } else {
            System.out.println("Cancelled.");
        }
        deleteMenu();
    }
    private static void deleteTrail() {
        System.out.println("Enter ID of trail to delete: ");
        Long id = input.nextLong(); input.nextLine();
        System.out.print("Are you sure? (y/n): ");
        String confirm = input.nextLine();
        if (confirm.equalsIgnoreCase("y")) {
            try {
                String response = server.send(server.buildActionWithId("DELETE_TRAIL", id));
                System.out.println("Server response: " + response);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete trail: " + e.getMessage());
            }
        } else {
            System.out.println("Cancelled.");
        }
        deleteMenu();
    }
    private static void deleteTrailMedia() {
        System.out.println("Enter ID of TrailMedia to delete: ");
        Long id = input.nextLong(); input.nextLine();
        System.out.print("Are you sure? (y/n): ");
        String confirm = input.nextLine();
        if (confirm.equalsIgnoreCase("y")) {
            try {
                String response = server.send(server.buildActionWithId("DELETE_TRAILMEDIA", id));
                System.out.println("Server response: " + response);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete TrailMedia: " + e.getMessage());
            }
        } else {
            System.out.println("Cancelled.");
        }
        deleteMenu();
    }

//      __  __ ______ _   _ _    _  _____
//     |  \/  |  ____| \ | | |  | |/ ____|
//     | \  / | |__  |  \| | |  | | (___
//     | |\/| |  __| | . ` | |  | |\___ \
//     | |  | | |____| |\  | |__| |____) |
//     |_|  |_|______|_| \_|\____/|_____/
//
//

    static void displayMenu() {
        System.out.println("_________Menu_________");
        System.out.println("\t1. Go to CRUD");
        System.out.println("\t2. Exit");
        System.out.println("_______________________");
        System.out.print("Input: ");
        int choice = input.nextInt(); input.nextLine();
        while (choice != 1 && choice != 2) {
            System.out.println("Try again");
            choice = input.nextInt(); input.nextLine();
        }
        route(choice);
    }
    static void createMenu() {
        System.out.println("___________Crud___________");
        System.out.println("1. Add new Location");
        System.out.println("2. Add new RouteStop");
        System.out.println("3. Add new Trail");
        System.out.println("4. Add new TrailMedia");
        System.out.println("5. Return");
        System.out.println("__________________________");
        System.out.print("Input: ");
        int choice = input.nextInt(); input.nextLine();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt(); input.nextLine();
        }

        if(choice == 5) { displayCRUD(); return; }
        route(choice+6);

    }
    static void readMenu() {
        System.out.println("___________cRud___________");
        System.out.println("1. Read Location");
        System.out.println("2. Read RouteStop");
        System.out.println("3. Read Trail");
        System.out.println("4. Read TrailMedia");
        System.out.println("5. Return");
        System.out.println("__________________________");
        System.out.print("Input: ");
        int choice = input.nextInt(); input.nextLine();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt(); input.nextLine();
        }

        if(choice == 5) { displayCRUD(); return; }
        route(choice+10);
    }
    static void updateMenu() {
        System.out.println("___________crUd___________");
        System.out.println("1. Update Location");
        System.out.println("2. Update RouteStop");
        System.out.println("3. Update Trail");
        System.out.println("4. Update TrailMedia");
        System.out.println("5. Return");
        System.out.println("__________________________");
        System.out.print("Input: ");
        int choice = input.nextInt(); input.nextLine();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt(); input.nextLine();
        }
        if(choice == 5) { displayCRUD(); return; }


        route(choice+14);
    }

    static void deleteMenu() {
        System.out.println("___________cruD___________");
        System.out.println("1. Delete Location");
        System.out.println("2. Delete RouteStop");
        System.out.println("3. Delete Trail");
        System.out.println("4. Delete TrailMedia");
        System.out.println("5. Return");
        System.out.println("__________________________");
        System.out.print("Input: ");
        int choice = input.nextInt(); input.nextLine();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt(); input.nextLine();
        }
        if(choice == 5) { displayCRUD(); return; }


        route(choice+18);
    }


    static void displayCRUD() {
        System.out.println("_______________________");
        System.out.println("1. Create");
        System.out.println("2. Read");
        System.out.println("3. Update");
        System.out.println("4. Delete");
        System.out.println("5. Return");
        System.out.println("_______________________");
        System.out.print("Input: ");
        int choice = input.nextInt(); input.nextLine();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt(); input.nextLine();
        }

        if(choice == 5) { displayMenu(); return; }
        route(choice+2);

    }

    static void clearScreen() {
        for(int i = 0; i < 5; i++) {
            System.out.println("\n");
        }
    }

//      _____   ____  _    _ _______ ______ _____
//     |  __ \ / __ \| |  | |__   __|  ____|  __ \
//     | |__) | |  | | |  | |  | |  | |__  | |__) |
//     |  _  /| |  | | |  | |  | |  |  __| |  _  /
//     | | \ \| |__| | |__| |  | |  | |____| | \ \
//     |_|  \_\\____/ \____/   |_|  |______|_|  \_\
//
//

    static void route(int choice) {
        switch (choice) {
            case 1:
                displayCRUD();
                break;
            case 2:
                System.exit(0);
                break;
            case 3:
                createMenu();
                break;
            case 4:
                readMenu();
                break;
            case 5:
                updateMenu();
                break;
            case 6:
                deleteMenu();
                break;
            case 7:
                addNewLocation();
                break;
            case 8:
                addNewRouteStop();
                break;
            case 9:
                addNewTrail();
                break;
            case 10:
                addNewTrailMedia();
                break;
            case 11:
                readLocation();
                break;
            case 12:
                readRouteStop();
                break;
            case 13:
                readTrail();
                break;
            case 14:
                readTrailMedia();
                break;
            case 15:
                updateLocation();
                break;
            case 16:
                updateTrail();
                break;
            case 17:
                updateTrailMedia();
                break;
            case 18:
                updateRouteStop();
                break;
            case 19:
                deleteLocation();
                break;
            case 20:
                deleteTrail();
                break;
            case 21:
                deleteTrailMedia();
                break;
            case 22:
                deleteRouteStop();
                break;
            default:
                System.out.println("Invalid choice");
        }
    }
}

