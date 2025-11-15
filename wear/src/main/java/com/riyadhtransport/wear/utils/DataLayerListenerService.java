package com.riyadhtransport.wear;

import android.content.SharedPreferences;
import android.util.Log;

// --- Imports for MessageClient ---
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.riyadhtransport.wear.utils.DataSyncHelper;

import java.nio.charset.StandardCharsets; // Import this

/**
 * This service now listens for direct messages (MessageClient)
 * instead of data items (DataClient) for a more reliable, instant sync.
 */
public class DataLayerListenerService extends WearableListenerService {

    // --- Use this TAG to filter in Logcat ---
    private static final String TAG = "DataLayerListener";

    /**
     * This method is called when a direct message is received from the mobile app.
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "onMessageReceived: Message received from mobile app!");

        String path = messageEvent.getPath();
        SharedPreferences.Editor editor = getSharedPreferences(
                DataSyncHelper.PREFS_NAME, MODE_PRIVATE).edit();

        boolean dataWasSaved = false;

        // Check if it's the favorites message
        if (DataSyncHelper.FAVORITES_PATH.equals(path)) {
            // Get the data (as bytes) and convert to a JSON string
            byte[] data = messageEvent.getData();
            String favoritesJson = new String(data, StandardCharsets.UTF_8);

            if (favoritesJson != null) {
                Log.d(TAG, "Saving favorites... JSON received: " + favoritesJson);
                editor.putString(DataSyncHelper.FAVORITES_KEY, favoritesJson);
                dataWasSaved = true;
            } else {
                Log.w(TAG, "Received null data for FAVORITES_KEY");
            }

        // Check if it's the history message
        } else if (DataSyncHelper.HISTORY_PATH.equals(path)) {
            byte[] data = messageEvent.getData();
            String historyJson = new String(data, StandardCharsets.UTF_8);

            if (historyJson != null) {
                Log.d(TAG, "Saving history... JSON received: " + historyJson);
                editor.putString(DataSyncHelper.HISTORY_KEY, historyJson);
                dataWasSaved = true;
            } else {
                Log.w(TAG, "Received null data for HISTORY_KEY");
            }

        } else {
            Log.w(TAG, "Received message for unknown path: " + path);
        }

        if (dataWasSaved) {
            // Use commit() to save synchronously and ensure it finishes
            boolean saveSuccess = editor.commit();
            Log.d(TAG, "SharedPreferences save success: " + saveSuccess);
        }
    }
}
