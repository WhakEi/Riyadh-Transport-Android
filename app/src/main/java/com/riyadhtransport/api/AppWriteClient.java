package com.riyadhtransport.api;

import android.content.Context;
import io.appwrite.Client;
import io.appwrite.services.Databases;

public class AppWriteClient {
    private static Client client = null;
    private static Databases databases = null;

    // AppWrite configuration - Update these with your actual values
    private static final String ENDPOINT = "https://fra.cloud.appwrite.io/v1";
    private static final String PROJECT_ID = "68f141dd000f83849c21";
    private static final String DATABASE_ID = "68f146de0013ba3e183a";
    public static final String ALERTS_COLLECTION_ID = "emptt";

    /**
     * Initialize AppWrite client
     */
    public static void init(Context context) {
        if (client == null) {
            // Create client with builder pattern to avoid ambiguity
            client = new Client(context.getApplicationContext());
            client = client.setEndpoint(ENDPOINT);
            client = client.setProject(PROJECT_ID);

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
