package com.riyadhtransport.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riyadhtransport.api.AppWriteClient;
import com.riyadhtransport.models.LineAlert;
import io.appwrite.services.Databases;
import io.appwrite.models.DocumentList;
import io.appwrite.models.Document;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        
        // Use executor to run AppWrite call on background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Get AppWrite databases service
                Databases databases = AppWriteClient.getDatabases(context);
                String databaseId = AppWriteClient.getDatabaseId();
                String collectionId = AppWriteClient.ALERTS_COLLECTION_ID;
                
                // Fetch documents from AppWrite
                DocumentList documentList = databases.listDocuments(
                    databaseId,
                    collectionId
                );
                
                // Parse alerts from documents
                List<LineAlert> alerts = new ArrayList<>();
                List<Document> documents = documentList.getDocuments();
                
                for (Document doc : documents) {
                    try {
                        // Extract fields from document
                        String title = doc.getData().containsKey("title") ? 
                            doc.getData().get("title").toString() : "";
                        String message = doc.getData().containsKey("message") ? 
                            doc.getData().get("message").toString() : "";
                        String createdAt = doc.getData().containsKey("$createdAt") ? 
                            doc.getData().get("$createdAt").toString() : "";
                        
                        if (!title.isEmpty()) {
                            LineAlert alert = new LineAlert(title, message, createdAt);
                            alerts.add(alert);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing alert document: " + e.getMessage());
                    }
                }
                
                Log.d(TAG, "Successfully fetched " + alerts.size() + " alerts from AppWrite");
                
                // Cache the alerts
                cacheAlerts(context, alerts);
                
                // Return results on main thread
                callback.onSuccess(alerts);
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching alerts from AppWrite: " + e.getMessage());
                
                // Try to use cached alerts as fallback
                List<LineAlert> cachedAlerts = getCachedAlerts(context);
                if (!cachedAlerts.isEmpty()) {
                    Log.d(TAG, "Using cached alerts as fallback");
                    callback.onSuccess(cachedAlerts);
                } else {
                    callback.onError("Failed to fetch alerts: " + e.getMessage());
                }
            } finally {
                executor.shutdown();
            }
        });
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
