package com.riyadhtransport.models;

import com.google.gson.annotations.SerializedName;

public class RefinedTerminusResponse {
    @SerializedName("line_number")
    private String lineNumber;
    
    @SerializedName("api_destination")
    private String apiDestination;
    
    @SerializedName("refined_terminus")
    private String refinedTerminus;
    
    public String getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getApiDestination() {
        return apiDestination;
    }
    
    public void setApiDestination(String apiDestination) {
        this.apiDestination = apiDestination;
    }
    
    public String getRefinedTerminus() {
        return refinedTerminus;
    }
    
    public void setRefinedTerminus(String refinedTerminus) {
        this.refinedTerminus = refinedTerminus;
    }
}
