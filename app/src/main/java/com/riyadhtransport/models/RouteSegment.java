package com.riyadhtransport.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RouteSegment {
    @SerializedName("type")
    private String type; // "walk", "metro", "bus"
    
    @SerializedName("line")
    private String line;
    
    @SerializedName("stations")
    private List<String> stations;
    
    @SerializedName("duration")
    private double duration; // in seconds
    
    @SerializedName("distance")
    private Double distance; // in meters (for walking)
    
    @SerializedName("from")
    private Object from; // Can be String or coordinate object
    
    @SerializedName("to")
    private Object to; // Can be String or coordinate object
    
    // Live arrival data fields (not serialized, only used at runtime)
    private transient Integer waitMinutes;
    private transient String arrivalStatus; // "checking", "live", "hidden", "normal"
    private transient String refinedTerminus;
    private transient Integer nextArrivalMinutes;
    private transient List<Integer> upcomingArrivals;
    
    public RouteSegment() {
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getLine() {
        return line;
    }
    
    public void setLine(String line) {
        this.line = line;
    }
    
    public List<String> getStations() {
        return stations;
    }
    
    public void setStations(List<String> stations) {
        this.stations = stations;
    }
    
    public double getDuration() {
        return duration;
    }
    
    public void setDuration(double duration) {
        this.duration = duration;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public Object getFrom() {
        return from;
    }
    
    public void setFrom(Object from) {
        this.from = from;
    }
    
    public Object getTo() {
        return to;
    }
    
    public void setTo(Object to) {
        this.to = to;
    }
    
    public boolean isWalking() {
        return "walk".equalsIgnoreCase(type);
    }
    
    public boolean isMetro() {
        return "metro".equalsIgnoreCase(type);
    }
    
    public boolean isBus() {
        return "bus".equalsIgnoreCase(type);
    }
    
    public int getStopCount() {
        return stations != null ? stations.size() : 0;
    }
    
    // Live arrival data getters and setters
    public Integer getWaitMinutes() {
        return waitMinutes;
    }
    
    public void setWaitMinutes(Integer waitMinutes) {
        this.waitMinutes = waitMinutes;
    }
    
    public String getArrivalStatus() {
        return arrivalStatus;
    }
    
    public void setArrivalStatus(String arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
    }
    
    public String getRefinedTerminus() {
        return refinedTerminus;
    }
    
    public void setRefinedTerminus(String refinedTerminus) {
        this.refinedTerminus = refinedTerminus;
    }
    
    public Integer getNextArrivalMinutes() {
        return nextArrivalMinutes;
    }
    
    public void setNextArrivalMinutes(Integer nextArrivalMinutes) {
        this.nextArrivalMinutes = nextArrivalMinutes;
    }
    
    public List<Integer> getUpcomingArrivals() {
        return upcomingArrivals;
    }
    
    public void setUpcomingArrivals(List<Integer> upcomingArrivals) {
        this.upcomingArrivals = upcomingArrivals;
    }
}
