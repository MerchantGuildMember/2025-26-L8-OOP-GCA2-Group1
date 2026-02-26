
public class LocationDTO {

    // Fields
    private double latitude;
    private double longitude;
    private String full_address;

    // Constructors - I imagine id, lat, long and created_at to be absolutely required
    //              - full address not really if it's some remote place
    public LocationDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationDTO(double latitude, double longitude, String full_address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.full_address = full_address;
    }

    public LocationDTO() {}


    // Getters
    public double getLatitude() {return latitude;}
    public double getLongitude() {return longitude;}
    public String getFullAddress() {return full_address;}


    // Setters
    public void setLatitude(double lat) {this.latitude = lat;}
    public void setLongitude(double lon) {this.longitude = lon;}
    public void setFullAddress(String full_address) {this.full_address = full_address;}
}




