import tables.Location;
import tables.RouteStop;
import tables.Trail;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Main {
    static void main(String[] args) {
        // Locations
        Location location1 = new Location(20L, 9.0, 24.0, LocalDateTime.now());
        Location location2 = new Location(10L, 19.0, 210.0, LocalDateTime.now());
        Location location3 = new Location(15L, 29.0, 25.0, LocalDateTime.now());
        Location location4 = new Location(30L, 39.0, 23.0, LocalDateTime.now());


        RouteStop route1 = new RouteStop(29L, location1, LocalDateTime.now());
        RouteStop route2 = new RouteStop(79L, location2, LocalDateTime.now());
        RouteStop route3 = new RouteStop(39L, location3, LocalDateTime.now());
        RouteStop route4 = new RouteStop(9L, location4, LocalDateTime.now());
        ArrayList<RouteStop> routelist = new ArrayList<>();
        routelist.add(route1); routelist.add(route2);

        Trail trail1 = new Trail(1930L, "Hello", routelist);
        trail1.addRoute_stops(route3, route4);

        System.out.println(trail1.toString());


        }
    }
