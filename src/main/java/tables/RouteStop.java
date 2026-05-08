package tables;

import java.time.LocalDateTime;

/**
 * RouteStop class for temporary breaks in trails
 *
 * @author Aleksy Cieslak
 *
 *
 */

public class RouteStop {
    // Fields
    private Long id;
    private String route_name;
    private Location location;
    private LocalDateTime created_at;

    // Constructors
    public RouteStop(Long id, String route_name, Location location, LocalDateTime created_at) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be >= 0");
        }

        this.id = id;
        this.route_name = route_name;
        this.location = location;
        this.created_at = created_at;
    }

    public RouteStop(Long id, Location location, LocalDateTime created_at) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be >= 0");
        }

        this.id = id;
        this.location = location;
        this.created_at = created_at;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getRoute_name() {
        return route_name;
    }

    public Location getLocation() {
        return location;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "RouteStop{id=" + id +
                ", name='" + route_name + '\'' +
                ", location=" + location +
                '}';
    }
}
