package com.riyadhtransport.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StationIdResponse {
    @SerializedName("station_name")
    private String stationName;
    
    @SerializedName("matches")
    private List<StationMatch> matches;
    
    public String getStationName() {
        return stationName;
    }
    
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
    
    public List<StationMatch> getMatches() {
        return matches;
    }
    
    public void setMatches(List<StationMatch> matches) {
        this.matches = matches;
    }
    
    public static class StationMatch {
        @SerializedName("full_station_name")
        private String fullStationName;
        
        @SerializedName("station_id")
        private String stationId;
        
        @SerializedName("type")
        private String type;
        
        public String getFullStationName() {
            return fullStationName;
        }
        
        public void setFullStationName(String fullStationName) {
            this.fullStationName = fullStationName;
        }
        
        public String getStationId() {
            return stationId;
        }
        
        public void setStationId(String stationId) {
            this.stationId = stationId;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
    }
}
