package com.riyadhtransport.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
// MODIFIED: Import your new AppWriteClient
import com.riyadhtransport.api.AppWriteClient;
import com.riyadhtransport.models.LineAlert;

// Appwrite Imports
import io.appwrite.Client; // This import is no longer strictly needed here, but safe to keep
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.services.Databases;
import io.appwrite.models.DocumentList;
import io.appwrite.models.Document;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertsManager {
    private static final String TAG = "AlertsManager";
    private static final String PREFS_NAME = "AlertsData";
    private static final String KEY_ALERTS = "alerts_list";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Callback interface for alert fetching
     */
    public interface AlertsCallback {
        void onSuccess(List<LineAlert> alerts);
        void onError(String message);
    }

    // NEW (Fix 4): Add the missing handleApiError method
    private static void handleApiError(Context context, AlertsCallback callback, String errorMessage) {
        // Try to use cached alerts as fallback
        List<LineAlert> cachedAlerts = getCachedAlerts(context);
        if (!cachedAlerts.isEmpty()) {
            Log.d(TAG, "Using cached alerts as fallback");
            callback.onSuccess(cachedAlerts);
        } else {
            callback.onError("Failed to fetch alerts: " + errorMessage);
        }
    }

    /**
     * Get alerts from cache or fetch from API
     */
    public static void getAlerts(Context context, AlertsCallback callback) {
        // Check cache first
        List<LineAlert> cachedAlerts = getCachedAlerts(context);
        long lastUpdate = getLastUpdateTime(context);
        long currentTime = System.currentTimeMillis();

        // Use cache if it's fresh
        if (!cachedAlerts.isEmpty() && (currentTime - lastUpdate) < CACHE_DURATION_MS) {
            Log.d(TAG, "Using cached alerts (" + cachedAlerts.size() + " alerts)");
            callback.onSuccess(cachedAlerts);
            return;
        }

        // Fetch from API
        fetchAlertsFromApi(context, callback);
    }

    /**
     * Fetch alerts from AppWrite
     */
    private static void fetchAlertsFromApi(Context context, AlertsCallback callback) {
        Log.d(TAG, "Fetching alerts from AppWrite...");

        // NEW: Create a handler for the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        try {
            // MODIFIED: Use your AppWriteClient class
            Databases databases = AppWriteClient.getDatabases(context);
            // --- End of Direct Initialization ---

            // MODIFIED: Use your AppWriteClient class
            String databaseId = AppWriteClient.getDatabaseId();
            String collectionId = AppWriteClient.ALERTS_COLLECTION_ID;

            // MODIFIED: This is the correct way to call listDocuments for SDK 8.1.0
            databases.listDocuments(
                databaseId,
                collectionId,
                new CoroutineCallback<DocumentList>((DocumentList documentList) -> {
                    // onSuccess: Runs on a background thread
                    try {
                        List<LineAlert> alerts = new ArrayList<>();

                        // MODIFIED (Fix 3): Iterate over Object and cast to Document
                        for (Object docObj : documentList.getDocuments()) {
                            if (docObj instanceof Document) {
                                Document doc = (Document) docObj;
                                try {
                                    // Cast getData() to a Map
                                    Map<String, Object> data = (Map<String, Object>) doc.getData();

                                    // Read from the 'data' map
                                    String title = data.containsKey("title") ?
                                        data.get("title").toString() : "";
                                    String message = data.containsKey("message") ?
                                        data.get("message").toString() : "";

                                    // Get createdAt from the document itself
                                    String createdAt = doc.getCreatedAt();

                                    if (!title.isEmpty()) {
                                        LineAlert alert = new LineAlert(title, message, createdAt);
                                        alerts.add(alert);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing alert document: " + e.getMessage());
                                }
                            }
                        }
                        Log.d(TAG, "Successfully fetched " + alerts.size() + " alerts from AppWrite");
                        cacheAlerts(context, alerts);

                        // Return results on main thread
                        mainHandler.post(() -> callback.onSuccess(alerts));

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing Appwrite response: " + e.getMessage());
                        mainHandler.post(() -> handleApiError(context, callback, e.getMessage()));
                        // MODIFIED: Add required 'return null;' inside the catch block
                        return null;
                    }

                    // THIS IS THE FIX for the "bad return type" error
                    return null;
                }, (Throwable error) -> {
                    // onError: Runs on a background thread
                    Log.e(TAG, "Error fetching alerts from AppWrite: " + error.getMessage());
                    // Return results on main thread
                    mainHandler.post(() -> handleApiError(context, callback, error.getMessage()));

                    // THIS IS THE FIX for the "bad return type" error
                    return null;
                })
            );

        } catch (AppwriteException e) {
            Log.e(TAG, "Error fetching alerts from AppWrite (outer): " + e.getMessage());
            mainHandler.post(() -> handleApiError(context, callback, e.getMessage()));
        }
    }

    /**
     * Get line-specific alerts for a given line number
     */
    public static void getAlertsForLine(Context context, String lineNumber, AlertsCallback callback) {
        getAlerts(context, new AlertsCallback() {
            @Override
            public void onSuccess(List<LineAlert> alerts) {
                List<LineAlert> lineAlerts = new ArrayList<>();
                for (LineAlert alert : alerts) {
                    if (alert.appliesToLine(lineNumber)) {
                        lineAlerts.add(alert);
                    }
                }
                callback.onSuccess(lineAlerts);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /**
     * Get general alerts (not line-specific)
     */
    public static void getGeneralAlerts(Context context, AlertsCallback callback) {
        getAlerts(context, new AlertsCallback() {
            @Override
            public void onSuccess(List<LineAlert> alerts) {
                List<LineAlert> generalAlerts = new ArrayList<>();
                for (LineAlert alert : alerts) {
                    if (alert.isGeneralAlert()) {
                        generalAlerts.add(alert);
                    }
                }
                callback.onSuccess(generalAlerts);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /**
     * Cache alerts to SharedPreferences
     */
    private static void cacheAlerts(Context context, List<LineAlert> alerts) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(alerts);
        prefs.edit()
            .putString(KEY_ALERTS, json)
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply();
    }

    /**
     * Get cached alerts from SharedPreferences
     */
    private static List<LineAlert> getCachedAlerts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ALERTS, null);

        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<LineAlert>>(){}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing cached alerts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get last update time from SharedPreferences
     */
    private static long getLastUpdateTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_UPDATE, 0);
    }

    /**
     * Clear cached alerts
     */
    public static void clearCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
