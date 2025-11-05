package com.riyadhtransport.models;

import com.google.gson.annotations.SerializedName;

public class LineAlert {
    @SerializedName("title")
    private String title;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("$createdAt")
    private String createdAt;
    
    // Parsed fields
    private String affectedLine; // null if general alert, line number if line-specific
    private String displayTitle; // title without the square brackets and line number
    
    public LineAlert() {
    }
    
    public LineAlert(String title, String message, String createdAt) {
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        parseTitle();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        parseTitle();
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getAffectedLine() {
        return affectedLine;
    }
    
    public String getDisplayTitle() {
        return displayTitle;
    }
    
    /**
     * Parse title to extract affected line number if present
     * Format: [lineNumber] Title text
     * Example: "[150] Detour from usual route" -> affectedLine = "150", displayTitle = "Detour from usual route"
     */
    private void parseTitle() {
        if (title == null || title.isEmpty()) {
            displayTitle = "";
            affectedLine = null;
            return;
        }
        
        // Check if title starts with square bracket
        if (title.startsWith("[")) {
            int endBracket = title.indexOf("]");
            if (endBracket > 0) {
                // Extract line number from brackets
                affectedLine = title.substring(1, endBracket).trim();
                // Remove the bracket part from display title
                displayTitle = title.substring(endBracket + 1).trim();
                return;
            }
        }
        
        // No brackets found - general alert
        affectedLine = null;
        displayTitle = title;
    }
    
    /**
     * Check if this is a line-specific alert
     */
    public boolean isLineSpecific() {
        return affectedLine != null;
    }
    
    /**
     * Check if this is a general alert
     */
    public boolean isGeneralAlert() {
        return affectedLine == null;
    }
    
    /**
     * Check if this alert applies to a specific line
     */
    public boolean appliesToLine(String lineNumber) {
        if (affectedLine == null) {
            return false; // General alerts don't apply to specific lines
        }
        return affectedLine.equals(lineNumber);
    }
}
