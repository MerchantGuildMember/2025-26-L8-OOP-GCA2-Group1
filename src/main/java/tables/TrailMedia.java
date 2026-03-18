package tables;

import java.time.LocalDateTime;

/**
 * Trail Media class for holding images attained from these trips
 * Gives information about the image and its location
 *
 * @author Aleksy Cieslak
 *
 *
 */


public class TrailMedia {
    // Fields
    private Long id;
    private Long trail_id;
    private Long stop_id;
    private String media_type;
    private String url;
    private String caption;
    private LocalDateTime creation_time;


    // Constructors

    public TrailMedia(Long id, Long trail_id, Long stop_id, String media_type, String url, String caption, LocalDateTime creation_time) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be >= 0");
        }
        if (trail_id < 0) {
            throw new IllegalArgumentException("id must be >= 0");
        }
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }

        this.id = id;
        this.trail_id = trail_id;
        this.stop_id = stop_id;
        this.media_type = media_type;
        this.url = url;
        this.caption = caption;
        this.creation_time = creation_time;
    }

    public TrailMedia(Long id, Long trail_id, String caption, String url, String media_type, LocalDateTime creation_time) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be >= 0");
        }

        this.id = id;
        this.trail_id = trail_id;
        this.caption = caption;
        this.url = url;
        this.media_type = media_type;
        this.creation_time = creation_time;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getTrail_id() {
        return trail_id;
    }

    public Long getStop_id() {
        return stop_id;
    }

    public String getMedia_type() {
        return media_type;
    }

    public String getUrl() {
        return url;
    }

    public String getCaption() {
        return caption;
    }

    public LocalDateTime getCreation_time() {
        return creation_time;
    }


    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTrail_id(Long trail_id) {
        this.trail_id = trail_id;
    }

    public void setStop_id(Long stop_id) {
        this.stop_id = stop_id;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setCreation_time(LocalDateTime creation_time) {
        this.creation_time = creation_time;
    }


}
