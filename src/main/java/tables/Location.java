package tables;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Location class, the heart of it all. Gives information about where the user would be, where trail stops are, where trails start and finish etc
 *
 * @author Aleksy Cieslak
 *
 *
 */

public class Location {

    // Fields
    private Long id;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String full_address;
    private LocalDateTime created_at;

    // Constructors - I imagine id, lat, long and created_at to be absolutely required
    //              - full address not really if it's some remote place
    public Location(Long id, double latitude, double longitude, LocalDateTime created_at) {
        if(id != null && id < 0) {throw new IllegalArgumentException("id must be >= 0 if provided");}

        this.id = id;
        this.latitude = BigDecimal.valueOf(latitude);
        this.longitude = BigDecimal.valueOf(longitude);
        this.created_at = created_at;
    }

    public Location(Long id, double latitude, double longitude, String full_address, LocalDateTime created_at) {
        if(id != null && id < 0) {throw new IllegalArgumentException("id must be >= 0 if provided");}

        this.id = id;
        this.latitude = BigDecimal.valueOf(latitude);
        this.longitude = BigDecimal.valueOf(longitude);
        this.full_address = full_address;
        this.created_at = created_at;
    }

    public Location(Long id, BigDecimal latitude, BigDecimal longitude, String full_address, LocalDateTime created_at) {
        if(id != null && id < 0) {throw new IllegalArgumentException("id must be >= 0 if provided");}

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.full_address = full_address;
        this.created_at = created_at;
    }



    // Getters
    public Long getId() {return id;}
    public BigDecimal getLatitude() {return latitude;}
    public BigDecimal getLongitude() {return longitude;}
    public String getFullAddress() {return full_address;}
    public LocalDateTime getCreationTime() {return created_at;}


    // Setters
    public void setId(Long id) {this.id = id;}
    public void setLatitude(BigDecimal lat) {this.latitude = lat;}
    public void setLongitude(BigDecimal lon) {this.longitude = lon;}
    public void setFullAddress(String full_address) {this.full_address = full_address;}
    public void setCreationTime(LocalDateTime created_at) {this.created_at = created_at;}
}




