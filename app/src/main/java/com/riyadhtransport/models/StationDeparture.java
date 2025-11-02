package com.riyadhtransport.models;

import com.google.gson.annotations.SerializedName;

public class StationDeparture {
    @SerializedName("number")
    private String number;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("destination")
    private String destination;
    
    @SerializedName("actualDepartureTimePlanned")
    private String actualDepartureTimePlanned;
    
    @SerializedName("departureTimePlanned")
    private String departureTimePlanned;
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public String getActualDepartureTimePlanned() {
        return actualDepartureTimePlanned;
    }
    
    public void setActualDepartureTimePlanned(String actualDepartureTimePlanned) {
        this.actualDepartureTimePlanned = actualDepartureTimePlanned;
    }
    
    public String getDepartureTimePlanned() {
        return departureTimePlanned;
    }
    
    public void setDepartureTimePlanned(String departureTimePlanned) {
        this.departureTimePlanned = departureTimePlanned;
    }
}
