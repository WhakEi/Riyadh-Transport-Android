package com.riyadhtransport.wear.models;

import java.io.Serializable;

// 2. ADD "implements Serializable" HERE
public class RouteInstruction implements Serializable {
    private String type; // "walk", "metro", "bus"
    private String instruction; // Main instruction text
    private String details; // Additional details
    private int duration; // Duration in minutes
    private String lineColor; // For metro/bus

    public RouteInstruction() {
    }

    public RouteInstruction(String type, String instruction, String details, int duration) {
        this.type = type;
        this.instruction = instruction;
        this.details = details;
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public boolean isWalk() {
        return "walk".equalsIgnoreCase(type);
    }

    public boolean isMetro() {
        return "metro".equalsIgnoreCase(type);
    }

    public boolean isBus() {
        return "bus".equalsIgnoreCase(type);
    }
}
