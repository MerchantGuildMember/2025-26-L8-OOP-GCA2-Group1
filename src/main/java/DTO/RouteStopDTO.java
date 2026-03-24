package DTO;

public class RouteStopDTO {
    // Fields
    private String route_name;
    private LocationDTO location;

    // Constructors
    public RouteStopDTO(String route_name, LocationDTO location_dto) {
        this.route_name = route_name;
        this.location = location_dto;
    }

    public RouteStopDTO(LocationDTO location_dto) {
        this.location = location_dto;
    }

    public RouteStopDTO() {
    }

    // Getters
    public String getRoute_name() {
        return route_name;
    }

    public LocationDTO getLocation() {
        return location;
    }

    // Setters
    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public void setLocation(LocationDTO location) {
        this.location = location;
    }
}
