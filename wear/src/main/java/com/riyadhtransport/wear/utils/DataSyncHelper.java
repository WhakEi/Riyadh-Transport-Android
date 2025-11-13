package com.riyadhtransport.wear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riyadhtransport.wear.models.WearFavorite;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for syncing data between phone and watch
 * Uses SharedPreferences as a simple storage mechanism
 * In production, this would use Wearable Data Layer API
 */
public class DataSyncHelper {
    private static final String PREFS_NAME = "WearDataSync";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    
    public DataSyncHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Get favorites synced from phone
     */
    public List<WearFavorite> getFavorites() {
        String json = prefs.getString(KEY_FAVORITES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        try {
            Type listType = new TypeToken<ArrayList<WearFavorite>>(){}.getType();
            List<WearFavorite> favorites = gson.fromJson(json, listType);
            return favorites != null ? favorites : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Get search history synced from phone
     */
    public List<WearFavorite> getSearchHistory() {
        String json = prefs.getString(KEY_SEARCH_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        try {
            Type listType = new TypeToken<ArrayList<WearFavorite>>(){}.getType();
            List<WearFavorite> history = gson.fromJson(json, listType);
            return history != null ? history : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Save favorites (for testing purposes)
     */
    public void saveFavorites(List<WearFavorite> favorites) {
        String json = gson.toJson(favorites);
        prefs.edit().putString(KEY_FAVORITES, json).apply();
    }
    
    /**
     * Save search history (for testing purposes)
     */
    public void saveSearchHistory(List<WearFavorite> history) {
        String json = gson.toJson(history);
        prefs.edit().putString(KEY_SEARCH_HISTORY, json).apply();
    }
    
    /**
     * Initialize with sample data for testing
     */
    public void initializeSampleData() {
        List<WearFavorite> favorites = new ArrayList<>();
        favorites.add(new WearFavorite("King Abdullah Financial District", 24.7661, 46.6373, "location"));
        favorites.add(new WearFavorite("Olaya Metro Station", 24.6952, 46.6851, "station"));
        favorites.add(new WearFavorite("Riyadh Park Mall", 24.7561, 46.6451, "location"));
        saveFavorites(favorites);
        
        List<WearFavorite> history = new ArrayList<>();
        history.add(new WearFavorite("King Khalid International Airport", 24.9575, 46.6989, "location"));
        history.add(new WearFavorite("Riyadh Gallery Mall", 24.7537, 46.6753, "location"));
        history.add(new WearFavorite("King Fahd Stadium", 24.6771, 46.7084, "location"));
        saveSearchHistory(history);
    }
}
