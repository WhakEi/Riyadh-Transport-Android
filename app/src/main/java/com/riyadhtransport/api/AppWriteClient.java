package com.riyadhtransport.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Client for AppWrite REST API using Retrofit
 */
public class AppWriteClient {
    // AppWrite configuration
    private static final String ENDPOINT = "https://fra.cloud.appwrite.io/v1/";
    public static final String PROJECT_ID = "68f141dd000f83849c21";
    public static final String DATABASE_ID = "68f146de0013ba3e183a";
    public static final String ALERTS_COLLECTION_ID = "emptt";

    private static Retrofit retrofit = null;
    private static AppWriteApiService apiService = null;

    /**
     * Get Retrofit client for AppWrite
     */
    private static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(ENDPOINT)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Get AppWrite API service
     */
    public static AppWriteApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(AppWriteApiService.class);
        }
        return apiService;
    }
}
