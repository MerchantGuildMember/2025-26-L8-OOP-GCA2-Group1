import java.util.List;

public class TrailDTO {
    // Fields
    private String name;
    private String description;
    private String difficulty;
    private Double estimated_time;
    private List<RouteStop> stops;

    // Constructors
    public TrailDTO(String name, String description, String difficulty, Double estimated_time, List<RouteStop> stops) {
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.estimated_time = estimated_time;
        this.stops = stops;
    }
    public TrailDTO(Long id, String name, List<RouteStop> stops) {
        this.name = name;
        this.stops = stops;
    }

    public TrailDTO() {}

    // Getters
    public String getName() {return name;}
    public String getDescription() {return description;}
    public String getDifficulty() {return difficulty;}
    public Double getEstimated_time() {return estimated_time;}
    public List<RouteStop> getStops() {return stops;}

    // Setters
    public void setName(String name) {this.name = name;}
    public void setDescription(String description) {this.description = description;}
    public void setDifficulty(String difficulty) {this.difficulty = difficulty;}
    public void setEstimated_time(Double estimated_time) {this.estimated_time = estimated_time;}
    public void setStops(List<RouteStop> stops) {this.stops = stops;}

}
