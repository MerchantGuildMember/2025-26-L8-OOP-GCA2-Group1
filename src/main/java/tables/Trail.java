package tables;

import java.util.ArrayList;

/**
 * Trail Class
 * holds id, name, trail difficulty, its estimated time and the amount of stops
 *
 * @author Aleksy Cieslak
 *
 */

public class Trail {
    // Fields
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private Double estimated_time;
    private ArrayList<RouteStop> stops;

    // Constructors
    public Trail(Long id, String name, String description, String difficulty, Double estimated_time, ArrayList<RouteStop> stops) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be >= 0");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("must have a name");
        }

        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.estimated_time = estimated_time;
        this.stops = stops;
    }

    public Trail(Long id, String name, ArrayList<RouteStop> stops) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be >= 0");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("must have a name");
        }

        this.id = id;
        this.name = name;
        this.stops = stops;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Double getEstimated_time() {
        return estimated_time;
    }

    public ArrayList<RouteStop> getStops() {
        return stops;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setEstimated_time(Double estimated_time) {
        this.estimated_time = estimated_time;
    }

    public void setStops(ArrayList<RouteStop> stops) {
        this.stops = stops;
    }


    // to string


    @Override
    public String toString() {
        return "tables.Trail{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", estimated_time=" + estimated_time +
                ", stops=" + stops +
                '}';
    }

    // add more
    public void addRoute_stops(RouteStop... args) {
        for (RouteStop arg : args) {
            getStops().add(arg);
        }
    }


}
