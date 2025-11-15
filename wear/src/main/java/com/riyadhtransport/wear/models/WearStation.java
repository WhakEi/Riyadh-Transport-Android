package com.riyadhtransport.wear.models;

import com.google.gson.annotations.SerializedName;

public class WearStation {
    @SerializedName("value")
    private String value;
    
    @SerializedName("label")
    private String label;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("type")
    private String type; // "metro" or "bus"
    
    @SerializedName("lat")
    private double latitude;
    
    @SerializedName("lng")
    private double longitude;
    
    @SerializedName("distance")
    private Double distance; // Distance in meters
    
    @SerializedName("duration")
    private Double duration; // Walking duration in seconds
    
    // Compass-specific fields
    private float bearing; // Bearing from user location to station
    private float normalizedDistance; // Distance normalized for compass display (0.0 to 1.0)
    
    public WearStation() {
    }
    
    public WearStation(String value, String label, String type, double latitude, double longitude) {
        this.value = value;
        this.label = label;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public Double getDuration() {
        return duration;
    }
    
    public void setDuration(Double duration) {
        this.duration = duration;
    }
    
    public float getBearing() {
        return bearing;
    }
    
    public void setBearing(float bearing) {
        this.bearing = bearing;
    }
    
    public float getNormalizedDistance() {
        return normalizedDistance;
    }
    
    public void setNormalizedDistance(float normalizedDistance) {
        this.normalizedDistance = normalizedDistance;
    }
    
    public boolean isMetro() {
        return "metro".equalsIgnoreCase(type);
    }
    
    public boolean isBus() {
        return "bus".equalsIgnoreCase(type);
    }
    
    public String getDisplayName() {
        String displayName = null;
        if (label != null) displayName = label;
        else if (name != null) displayName = name;
        else displayName = value;
        
        if (displayName != null) {
            displayName = displayName.replaceAll("\\s*\\(Bus\\)\\s*$", "")
                                    .replaceAll("\\s*\\(Metro\\)\\s*$", "")
                                    .trim();
        }
        
        return displayName;
    }
}
