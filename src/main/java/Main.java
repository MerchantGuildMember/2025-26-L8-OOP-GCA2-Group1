import DAO.*;
import shared.ServerResponse;
import tables.Location;
import tables.RouteStop;
import tables.Trail;
import tables.TrailMedia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final String URL  = System.getenv("URL");
    private static final String USER = System.getenv("USER");
    private static final String PASS = System.getenv("PASS");


    static LocationDAO locationDAO      = new JdbcLocationDAO(URL, USER, PASS);
    static RouteStopDAO routeStopDAO    = new JdbcRouteStopDAO(URL, USER, PASS);
    static TrailDAO trailDAO            = new JdbcTrailDAO(URL, USER, PASS);
    static TrailMediaDAO trailMediaDAO  = new JdbcTrailMediaDAO(URL, USER, PASS);



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

    static void displayMenu() {
        System.out.println("_________Menu_________");
        System.out.println("\t1. Go to CRUD");
        System.out.println("\t2. Exit");
        System.out.println("_______________________");
        System.out.print("Input: ");
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        while (choice != 1 && choice != 2) {
            System.out.println("Try again");
            choice = input.nextInt();
        }
        route(choice);
    }

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
//                deleteMenu();
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
            default:
                System.out.println("Invalid choice");
        }
    }

//              _____ _____  ______       _______ _____ ____  _   _
//             / ____|  __ \|  ____|   /\|__   __|_   _/ __ \| \ | |
//            | |    | |__) | |__     /  \  | |    | || |  | |  \| |
//            | |    |  _  /|  __|   / /\ \ | |    | || |  | | . ` |
//            | |____| | \ \| |____ / ____ \| |   _| || |__| | |\  |
//             \_____|_|  \_\______/_/    \_\_|  |_____\____/|_| \_|

    private static void addNewLocation() {
        Scanner input = new Scanner(System.in);
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
            locationDAO.insert(newLocation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert location");
        }
        createMenu();
    }

    private static void addNewRouteStop() {
        Scanner input = new Scanner(System.in);
        System.out.println("Entering New RouteStop");

        System.out.print("\nRoute Name: ");
        String routeName = input.nextLine();

        System.out.print("\nLocation ID: ");
        Long locationId = input.nextLong(); input.nextLine();

        LocalDateTime time = LocalDateTime.now();

        try {
            Location location = locationDAO.displayById(locationId).getData();
            if (location == null)
                throw new RuntimeException("Location not found: " + locationId);

            RouteStop newRouteStop = new RouteStop(0L, routeName, location, time);
            routeStopDAO.insert(newRouteStop);
            System.out.println("\nInserted RouteStop: " + routeName + " at " + location.getFullAddress());
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert RouteStop: " + e.getMessage());
        }
        createMenu();
    }

    private static void addNewTrail() {
        Scanner input = new Scanner(System.in);
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
            try {
                RouteStop stop = routeStopDAO.displayById(stopId).getData();
                if (stop != null) stops.add(stop);
                else System.out.println("Could not find stop with ID: " + stopId);
            } catch (Exception e) {
                System.out.println("Could not find stop with ID: " + stopId);
            }
        }

        if (stops.isEmpty()) {
            System.out.println("No valid stops found, trail not created.");
            createMenu();
            return;
        }

        try {
            Trail newTrail = new Trail(0L, name, description, difficulty, estimatedTime, stops);
            trailDAO.insert(newTrail);
            System.out.println("\nInserted Trail: " + name + " with " + stops.size() + " stops");
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert Trail: " + e.getMessage());
        }
        createMenu();
    }

    private static void addNewTrailMedia() {
        Scanner input = new Scanner(System.in);
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
            trailMediaDAO.insert(newTrailMedia);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert TrailMedia: " + e.getMessage());
        }
        createMenu();
    }

//                _____  ______          _____
//               |  __ \|  ____|   /\   |  __ \
//               | |__) | |__     /  \  | |  | |
//               |  _  /|  __|   / /\ \ | |  | |
//               | | \ \| |____ / ____ \| |__| |
//               |_|  \_\______/_/    \_\_____/

    private static void readLocation() {
        System.out.println("ID | ALL");
        Scanner input = new Scanner(System.in);
        String choice = input.nextLine();
        if (choice.equals("ALL")) {
            ServerResponse<ArrayList<Location>> result = null;
            try {
                result = locationDAO.displayAll();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.printf("%-5s %-12s %-12s %-30s%n", "ID", "Latitude", "Longitude", "Full Address");
            System.out.println("-".repeat(62));
            result.getData().forEach(location -> {
                System.out.printf("%-5d %-12s %-12s %-30s%n",
                        location.getId(),
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getFullAddress());
            });
        }
        else if (choice.equals("ID")) {
            System.out.print("Input ID: ");
            Long id = input.nextLong(); input.nextLine();
            try {
                var result = locationDAO.displayById(id);
                Location location = result.getData();
                if (location == null) {
                    System.out.println("Location not found: " + id);
                } else {
                    System.out.printf("%-5s %-12s %-12s %-30s%n", "ID", "Latitude", "Longitude", "Full Address");
                    System.out.println("-".repeat(62));
                    System.out.printf("%-5d %-12s %-12s %-30s%n",
                            location.getId(),
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getFullAddress());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else System.out.println("Invalid choice");
        readMenu();
    }

    private static void readTrail() {
        System.out.println("ID | ALL");
        Scanner input = new Scanner(System.in);
        String choice = input.nextLine();
        if (choice.equals("ALL")) {
            try {
                var result = trailDAO.displayAll();
                System.out.printf("%-5s %-20s %-12s %-10s %-6s%n", "ID", "Name", "Difficulty", "Est. Time", "Stops");
                System.out.println("-".repeat(56));
                result.getData().forEach(trail ->
                        System.out.printf("%-5d %-20s %-12s %-10s %-6d%n",
                                trail.getId(),
                                trail.getName(),
                                trail.getDifficulty(),
                                trail.getEstimated_time(),
                                trail.getStops().size())
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (choice.equals("ID")) {
            System.out.print("Input ID: ");
            Long id = input.nextLong(); input.nextLine();
            try {
                var result = trailDAO.displayById(id);
                Trail trail = result.getData();
                if (trail == null) {
                    System.out.println("Trail not found: " + id);
                } else {
                    System.out.printf("%-5s %-20s %-12s %-10s %-6s%n", "ID", "Name", "Difficulty", "Est. Time", "Stops");
                    System.out.println("-".repeat(56));
                    System.out.printf("%-5d %-20s %-12s %-10s %-6d%n",
                            trail.getId(),
                            trail.getName(),
                            trail.getDifficulty(),
                            trail.getEstimated_time(),
                            trail.getStops().size());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else System.out.println("Invalid choice");
        readMenu();
    }

    private static void readTrailMedia() {
        System.out.println("ID | ALL");
        Scanner input = new Scanner(System.in);
        String choice = input.nextLine();
        if (choice.equals("ALL")) {
            try {
                var result = trailMediaDAO.displayAll();
                System.out.printf("%-5s %-10s %-10s %-10s %-30s %-20s%n", "ID", "Trail ID", "Stop ID", "Type", "URL", "Caption");
                System.out.println("-".repeat(88));
                result.getData().forEach(media ->
                        System.out.printf("%-5d %-10d %-10s %-10s %-30s %-20s%n",
                                media.getId(),
                                media.getTrail_id(),
                                media.getStop_id() != null ? media.getStop_id() : "-",
                                media.getMedia_type(),
                                media.getUrl(),
                                media.getCaption())
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (choice.equals("ID")) {
            System.out.print("Input ID: ");
            Long id = input.nextLong(); input.nextLine();
            try {
                var result = trailMediaDAO.displayById(id);
                TrailMedia media = result.getData();
                if (media == null) {
                    System.out.println("TrailMedia not found: " + id);
                } else {
                    System.out.printf("%-5s %-10s %-10s %-10s %-30s %-20s%n", "ID", "Trail ID", "Stop ID", "Type", "URL", "Caption");
                    System.out.println("-".repeat(88));
                    System.out.printf("%-5d %-10d %-10s %-10s %-30s %-20s%n",
                            media.getId(),
                            media.getTrail_id(),
                            media.getStop_id() != null ? media.getStop_id() : "-",
                            media.getMedia_type(),
                            media.getUrl(),
                            media.getCaption());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else System.out.println("Invalid choice");
        readMenu();
    }

    private static void readRouteStop() {
        System.out.println("ID | ALL");
        Scanner input = new Scanner(System.in);
        String choice = input.nextLine();
        if (choice.equals("ALL")) {
            try {
                var result = routeStopDAO.displayAll();
                System.out.printf("%-5s %-20s %-30s%n", "ID", "Route Name", "Location");
                System.out.println("-".repeat(57));
                result.getData().forEach(stop ->
                        System.out.printf("%-5d %-20s %-30s%n",
                                stop.getId(),
                                stop.getRoute_name(),
                                stop.getLocation().getFullAddress())
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (choice.equals("ID")) {
            System.out.print("Input ID: ");
            Long id = input.nextLong(); input.nextLine();
            try {
                var result = routeStopDAO.displayById(id);
                RouteStop stop = result.getData();
                if (stop == null) {
                    System.out.println("RouteStop not found: " + id);
                } else {
                    System.out.printf("%-5s %-20s %-30s%n", "ID", "Route Name", "Location");
                    System.out.println("-".repeat(57));
                    System.out.printf("%-5d %-20s %-30s%n",
                            stop.getId(),
                            stop.getRoute_name(),
                            stop.getLocation().getFullAddress());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else System.out.println("Invalid choice");
        readMenu();
    }


    static void createMenu() {
        System.out.println("_______________________");
        System.out.println("1. Add new Location");
        System.out.println("2. Add new RouteStop");
        System.out.println("3. Add new Trail");
        System.out.println("4. Add new TrailMedia");
        System.out.println("5. Return");
        System.out.println("_______________________");
        System.out.print("Input: ");
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt();
        }

        if(choice == 5) { displayCRUD(); return; }
        route(choice+6);
        input.close();

    }

    static void readMenu() {
        System.out.println("_______________________");
        System.out.println("1. Read Location");
        System.out.println("2. Read RouteStop");
        System.out.println("3. Read Trail");
        System.out.println("4. Read TrailMedia");
        System.out.println("5. Return");
        System.out.println("_______________________");
        System.out.print("Input: ");
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt();
        }

        if(choice == 5) { displayCRUD(); return; }
        route(choice+10);
        input.close();
    }

    static void updateMenu() {
        System.out.println("_______________________");
        System.out.println("1. Update Location");
        System.out.println("2. Update RouteStop");
        System.out.println("3. Update Trail");
        System.out.println("4. Update TrailMedia");
        System.out.println("5. Return");
        System.out.println("_______________________");
        System.out.print("Input: ");
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt();
        }
        if(choice == 5) { displayCRUD(); return; }
        input.close();


//        route(choice+6);
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
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        while (choice > 5 || choice < 1) {
            System.out.println("Try again");
            choice = input.nextInt();
        }

        if(choice == 5) { displayMenu(); return; }
        route(choice+2);
        input.close();

    }

    static void clearScreen() {
        for(int i = 0; i < 5; i++) {
            System.out.println("\n");
        }
    }
}

