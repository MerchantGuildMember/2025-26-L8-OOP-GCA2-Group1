import java.util.List;

public class Trail {
    // Fields
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private Double estimated_time;
    private List<RouteStop> stops;

    // Constructors
    public Trail(Long id, String name, String description, String difficulty, Double estimated_time, List<RouteStop> stops) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.estimated_time = estimated_time;
        this.stops = stops;
    }
    public Trail(Long id, String name, List<RouteStop> stops) {
        this.id = id;
        this.name = name;
        this.stops = stops;
    }

    // Getters
    public Long getId() {return id;}
    public String getName() {return name;}
    public String getDescription() {return description;}
    public String getDifficulty() {return difficulty;}
    public Double getEstimated_time() {return estimated_time;}
    public List<RouteStop> getStops() {return stops;}

    // Setters
    public void setId(Long id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setDescription(String description) {this.description = description;}
    public void setDifficulty(String difficulty) {this.difficulty = difficulty;}
    public void setEstimated_time(Double estimated_time) {this.estimated_time = estimated_time;}
    public void setStops(List<RouteStop> stops) {this.stops = stops;}



}
