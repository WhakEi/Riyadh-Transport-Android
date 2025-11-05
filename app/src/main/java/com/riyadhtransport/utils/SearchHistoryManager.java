package com.riyadhtransport.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riyadhtransport.models.SearchResult;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryManager {
    private static final String PREFS_NAME = "SearchHistoryData";
    private static final String KEY_HISTORY = "search_history";
    private static final int MAX_HISTORY_SIZE = 5;

    /**
     * Get the search history (last 5 searches)
     */
    public static List<SearchResult> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, null);
        
        if (json == null) {
            return new ArrayList<>();
        }
        
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<SearchResult>>(){}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Add a search result to history
     * Keeps only the last 5 searches, removes duplicates
     */
    public static void addToHistory(Context context, SearchResult result) {
        List<SearchResult> history = getHistory(context);
        
        // Remove duplicate if exists (same name and coordinates)
        history.removeIf(item -> 
            item.getName().equals(result.getName()) && 
            item.getLatitude() == result.getLatitude() && 
            item.getLongitude() == result.getLongitude()
        );
        
        // Add to beginning of list
        history.add(0, result);
        
        // Keep only last 5
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }
        
        // Save back
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(history);
        prefs.edit().putString(KEY_HISTORY, json).apply();
    }

    /**
     * Clear all search history
     */
    public static void clearHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_HISTORY).apply();
    }
}
