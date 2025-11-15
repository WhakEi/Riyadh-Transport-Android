package com.riyadhtransport.wear.models;

import com.google.gson.annotations.SerializedName;

public class WearArrival {
    @SerializedName("time")
    private String time; // e.g., "5 min", "12:30"
    
    @SerializedName("line")
    private String line; // e.g., "Blue Line", "Bus 230"
    
    @SerializedName("destination")
    private String destination;
    
    @SerializedName("minutes")
    private Integer minutes; // Minutes until arrival (null if >60)
    
    public WearArrival() {
    }
    
    public WearArrival(String time, String line, String destination, Integer minutes) {
        this.time = time;
        this.line = line;
        this.destination = destination;
        this.minutes = minutes;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getLine() {
        return line;
    }
    
    public void setLine(String line) {
        this.line = line;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public Integer getMinutes() {
        return minutes;
    }
    
    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }
    
    public boolean isLive() {
        return minutes != null && minutes < 60;
    }
}
