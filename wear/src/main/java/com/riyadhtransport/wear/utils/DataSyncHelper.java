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
 * This class now reads Favorites and History from SharedPreferences,
 * which are saved by the DataLayerListenerService.
 */
public class DataSyncHelper {

    // Constants for SharedPreferences
    public static final String PREFS_NAME = "RiyadhTransportPrefs";
    public static final String FAVORITES_KEY = "favorites_json";
    public static final String HISTORY_KEY = "history_json";

    // Constants for Data Layer paths (must match mobile app)
    public static final String FAVORITES_PATH = "/favorites";
    public static final String HISTORY_PATH = "/history";

    private final SharedPreferences prefs;
    private final Gson gson;

    public DataSyncHelper(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Reads the list of favorites from SharedPreferences.
     * @return A list of WearFavorite objects, or an empty list if none are saved.
     */
    public List<WearFavorite> getFavorites() {
        String json = prefs.getString(FAVORITES_KEY, null);
        if (json == null) {
            return new ArrayList<>(); // Return empty list, not null
        }

        // Use Gson to turn the JSON string back into a List
        Type listType = new TypeToken<ArrayList<WearFavorite>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    /**
     * Reads the list of search history from SharedPreferences.
     * @return A list of WearFavorite objects, or an empty list if none are saved.
     */
    public List<WearFavorite> getSearchHistory() {
        String json = prefs.getString(HISTORY_KEY, null);
        if (json == null) {
            return new ArrayList<>(); // Return empty list, not null
        }

        // Use Gson to turn the JSON string back into a List
        Type listType = new TypeToken<ArrayList<WearFavorite>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    /**
     * This method is no longer needed, but is kept empty to avoid
     * breaking any existing calls. The sample data is gone.
     */
    public void initializeSampleData() {
        // This method is now intentionally blank.
    }
}
