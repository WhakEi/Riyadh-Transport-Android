package com.riyadhtransport.api;

import android.content.Context;
import io.appwrite.Client;
import io.appwrite.services.Databases;

public class AppWriteClient {
    private static Client client = null;
    private static Databases databases = null;
    
    // AppWrite configuration - Update these with your actual values
    private static final String ENDPOINT = "https://cloud.appwrite.io/v1";
    private static final String PROJECT_ID = "YOUR_PROJECT_ID"; // TODO: Replace with actual project ID
    private static final String DATABASE_ID = "YOUR_DATABASE_ID"; // TODO: Replace with actual database ID
    public static final String ALERTS_COLLECTION_ID = "YOUR_ALERTS_COLLECTION_ID"; // TODO: Replace with actual collection ID
    
    /**
     * Initialize AppWrite client
     */
    public static void init(Context context) {
        if (client == null) {
            client = new Client(context)
                    .setEndpoint(ENDPOINT)
                    .setProject(PROJECT_ID);
            
            databases = new Databases(client);
        }
    }
    
    /**
     * Get the databases service
     */
    public static Databases getDatabases(Context context) {
        if (databases == null) {
            init(context);
        }
        return databases;
    }
    
    /**
     * Get the database ID
     */
    public static String getDatabaseId() {
        return DATABASE_ID;
    }
}
