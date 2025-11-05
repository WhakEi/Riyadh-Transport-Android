package com.riyadhtransport.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riyadhtransport.api.AppWriteClient;
import com.riyadhtransport.models.LineAlert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        // Always fetch from API to ensure deleted alerts are removed
        // Cache is used only as fallback on error
        fetchAlertsFromApi(context, callback);
    }

    /**
     * Fetch alerts from AppWrite using REST API
     */
    private static void fetchAlertsFromApi(Context context, AlertsCallback callback) {
        Log.d(TAG, "Fetching alerts from AppWrite REST API...");

        // Get collection ID based on current language
        String collectionId = AppWriteClient.getAlertsCollectionId(context);

        // Call AppWrite REST API using Retrofit
        AppWriteClient.getApiService().listDocuments(
                AppWriteClient.DATABASE_ID,
                collectionId,
                AppWriteClient.PROJECT_ID
        ).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> responseBody = response.body();
                        List<LineAlert> alerts = new ArrayList<>();

                        // Parse documents from AppWrite response
                        Object documentsObj = responseBody.get("documents");
                        if (documentsObj instanceof List) {
                            List<?> documentsList = (List<?>) documentsObj;

                            for (Object docObj : documentsList) {
                                if (docObj instanceof Map) {
                                    Map<String, Object> doc = (Map<String, Object>) docObj;

                                    String title = doc.containsKey("title") ? doc.get("title").toString() : "";
                                    String message = doc.containsKey("message") ? doc.get("message").toString() : "";
                                    String createdAt = doc.containsKey("$createdAt") ? doc.get("$createdAt").toString() : "";

                                    if (!title.isEmpty()) {
                                        alerts.add(new LineAlert(title, message, createdAt));
                                    }
                                }
                            }
                        }

                        Log.d(TAG, "Successfully fetched " + alerts.size() + " alerts from AppWrite");

                        // Cache the alerts
                        cacheAlerts(context, alerts);

                        // Return success
                        callback.onSuccess(alerts);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing AppWrite response: " + e.getMessage());
                        handleApiError(context, callback, e.getMessage());
                    }
                } else {
                    Log.e(TAG, "AppWrite API error: " + response.code());
                    handleApiError(context, callback, "API returned error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error fetching alerts from AppWrite: " + t.getMessage());
                handleApiError(context, callback, t.getMessage());
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
