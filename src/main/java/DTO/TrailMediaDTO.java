package DTO;

import java.time.LocalDateTime;

public class TrailMediaDTO {
    // Fields
    private String media_type;
    private String url;
    private String caption;
    private LocalDateTime creation_time;
    private byte[] fFileData;
    private String fFileName;
    private String fContentType;
    private int fFileSize;


    // Constructors

    public TrailMediaDTO(String media_type, String url, String caption, LocalDateTime creation_time) {
        this.media_type = media_type;
        this.url = url;
        this.caption = caption;
        this.creation_time = creation_time;
    }

    public TrailMediaDTO() {
    }

    // Getters
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
