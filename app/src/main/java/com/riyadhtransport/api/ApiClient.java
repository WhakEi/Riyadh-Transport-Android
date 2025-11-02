package com.riyadhtransport.api;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.riyadhtransport.utils.LocaleHelper;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    // Update this URL to point to your backend server
    // For testing with local server, use: http://10.0.2.2:5000/ (Android emulator)
    // For production, use your actual server URL
    private static final String BASE_URL = "http://mainserver.inirl.net:5000/";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/";
    
    private static Retrofit retrofit = null;
    private static Retrofit nominatimRetrofit = null;
    private static TransportApiService apiService = null;
    private static NominatimService nominatimService = null;
    private static Context appContext = null;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
        // Use Log.i for better visibility in logcat
        android.util.Log.i("RiyadhTransport", "ApiClient.init() called");
        android.util.Log.i("RiyadhTransport", "ApiClient: appContext=" + (appContext != null ? "NOT NULL" : "NULL"));
        if (appContext != null) {
            boolean isAr = LocaleHelper.isArabic(appContext);
            android.util.Log.i("RiyadhTransport", "ApiClient.init: isArabic=" + isAr);
        }
    }
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Create OkHttpClient with timeout settings and Arabic locale interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String url = chain.request().url().toString();
                        android.util.Log.i("RiyadhTransport", "=== API Request Interceptor ===");
                        android.util.Log.i("RiyadhTransport", "Original URL: " + url);
                        android.util.Log.i("RiyadhTransport", "appContext: " + (appContext != null ? "NOT NULL" : "NULL"));
                        
                        // Add /ar/ prefix to endpoints when app is in Arabic
                        if (appContext != null) {
                            boolean isArabic = LocaleHelper.isArabic(appContext);
                            android.util.Log.i("RiyadhTransport", "isArabic check result: " + isArabic);
                            
                            if (isArabic) {
                                // Only modify if it's our backend and doesn't already have /ar/
                                if (url.startsWith(BASE_URL) && !url.contains("/ar/")) {
                                    String path = url.substring(BASE_URL.length());
                                    String newUrl = BASE_URL + "ar/" + path;
                                    android.util.Log.w("RiyadhTransport", "REWRITING URL: " + url + " -> " + newUrl);
                                    return chain.proceed(
                                            chain.request().newBuilder()
                                                    .url(newUrl)
                                                    .build()
                                    );
                                } else if (url.contains("/ar/")) {
                                    android.util.Log.i("RiyadhTransport", "URL already has /ar/, skipping rewrite");
                                }
                            } else {
                                android.util.Log.i("RiyadhTransport", "English mode - not rewriting URL");
                            }
                        } else {
                            android.util.Log.e("RiyadhTransport", "ERROR: appContext is NULL!");
                        }
                        android.util.Log.i("RiyadhTransport", "=== End Interceptor ===");
                        return chain.proceed(chain.request());
                    })
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    public static TransportApiService getApiService() {
        // Always recreate the client to ensure locale changes are picked up
        android.util.Log.i("RiyadhTransport", "ApiClient.getApiService() called");
        android.util.Log.i("RiyadhTransport", "ApiClient: appContext=" + (appContext != null ? "NOT NULL" : "NULL"));
        if (appContext != null) {
            boolean isAr = LocaleHelper.isArabic(appContext);
            android.util.Log.i("RiyadhTransport", "ApiClient.getApiService: isArabic=" + isAr);
        }
        retrofit = null;
        apiService = null;
        apiService = getClient().create(TransportApiService.class);
        return apiService;
    }

    private static Retrofit getNominatimClient() {
        if (nominatimRetrofit == null) {
            // Create OkHttpClient with timeout settings
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        // Add User-Agent header as required by Nominatim
                        return chain.proceed(
                                chain.request()
                                        .newBuilder()
                                        .header("User-Agent", "RiyadhTransportApp/1.0")
                                        .build()
                        );
                    })
                    .build();

            // Create Retrofit instance for Nominatim
            nominatimRetrofit = new Retrofit.Builder()
                    .baseUrl(NOMINATIM_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return nominatimRetrofit;
    }

    public static NominatimService getNominatimService() {
        if (nominatimService == null) {
            nominatimService = getNominatimClient().create(NominatimService.class);
        }
        return nominatimService;
    }

    // Method to update base URL if needed
    public static void setBaseUrl(String url) {
        retrofit = null;
        apiService = null;
        // Will be recreated with new URL on next call
    }
}
