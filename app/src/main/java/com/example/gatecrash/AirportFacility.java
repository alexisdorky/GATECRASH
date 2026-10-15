package com.example.gatecrash;

public class AirportFacility {
    private String id;
    private String name;
    private String category; // Restrooms, Charging, Dining, Lounges, Shopping, Medical
    private String terminal;
    private String gateNear;
    private double latitude;
    private double longitude;
    private float rating;
    private String openHours;
    private String description;
    private String iconEmoji;

    public AirportFacility(String id, String name, String category, String terminal,
                           String gateNear, double latitude, double longitude,
                           float rating, String openHours, String description, String iconEmoji) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.terminal = terminal;
        this.gateNear = gateNear;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.openHours = openHours;
        this.description = description;
        this.iconEmoji = iconEmoji;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getTerminal() { return terminal; }
    public String getGateNear() { return gateNear; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getRating() { return rating; }
    public String getOpenHours() { return openHours; }
    public String getDescription() { return description; }
    public String getIconEmoji() { return iconEmoji; }

    // Calculate approximate walking distance in meters from given lat/lng
    public int calculateDistanceMeters(double fromLat, double fromLng) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(fromLat, fromLng, this.latitude, this.longitude, results);
        return Math.round(results[0]);
    }

    // Walking time estimate at ~75 meters/min (normal airport walking speed)
    public int estimateWalkingMinutes(int distanceMeters) {
        return Math.max(1, (int) Math.ceil(distanceMeters / 75.0));
    }
}
