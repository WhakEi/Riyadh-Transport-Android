package com.riyadhtransport.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ArrivalResponse {
    @SerializedName("station_name")
    private String stationName;
    
    @SerializedName("arrivals")
    private List<Arrival> arrivals;
    
    public String getStationName() {
        return stationName;
    }
    
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
    
    public List<Arrival> getArrivals() {
        return arrivals;
    }
    
    public void setArrivals(List<Arrival> arrivals) {
        this.arrivals = arrivals;
    }
}
