package io.github.incplusplus.potwhole;

import java.time.ZonedDateTime;

public class ReportItem {
    private String id;
    private ZonedDateTime timeSubmitted;
    private String submitterId;
    private String description;
    // This will probably be the LatLng type once #6 is merged and the library becomes available
    private String coordinates;
    private String imageUrl;

    public ReportItem() {}

    public ReportItem(
            String id,
            ZonedDateTime timeSubmitted,
            String submitterId,
            String description,
            String coordinates,
            String imageUrl) {
        this.id = id;
        this.timeSubmitted = timeSubmitted;
        this.submitterId = submitterId;
        this.description = description;
        this.coordinates = coordinates;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZonedDateTime getTimeSubmitted() {
        return timeSubmitted;
    }

    public void setTimeSubmitted(ZonedDateTime timeSubmitted) {
        this.timeSubmitted = timeSubmitted;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
