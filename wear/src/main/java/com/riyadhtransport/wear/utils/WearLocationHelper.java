package com.riyadhtransport.wear.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/**
 * Helper class for managing GPS location on Wear OS
 * Checks for built-in GPS first, then falls back to phone GPS
 */
public class WearLocationHelper {
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    
    public interface LocationCallback {
        void onLocationReceived(Location location, boolean fromWatch);
        void onLocationError(String error);
    }
    
    public WearLocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    /**
     * Check if GPS is available on the watch
     */
    public boolean hasBuiltInGPS() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            return locationManager.hasProvider(LocationManager.GPS_PROVIDER);
        }
        return false;
    }
    
    /**
     * Check if location permissions are granted
     */
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Get current location - tries watch GPS first, then phone GPS
     */
    public void getCurrentLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationError("Location permission not granted");
            return;
        }
        
        // Try to get location from watch GPS first
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.getToken()
        ).addOnSuccessListener(location -> {
            if (location != null) {
                // Successfully got location from watch
                callback.onLocationReceived(location, true);
            } else {
                // No location from watch, try phone GPS
                getPhoneLocation(callback);
            }
        }).addOnFailureListener(e -> {
            // Failed to get watch location, try phone
            getPhoneLocation(callback);
        });
    }
    
    /**
     * Get location from paired phone
     */
    private void getPhoneLocation(LocationCallback callback) {
        // Try to get last known location from phone
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }
        
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(location -> {
                if (location != null) {
                    callback.onLocationReceived(location, false);
                } else {
                    callback.onLocationError("No GPS available. Please ensure GPS is enabled on your watch or phone.");
                }
            })
            .addOnFailureListener(e -> {
                callback.onLocationError("Failed to get location: " + e.getMessage());
            });
    }
    
    /**
     * Calculate bearing from one location to another
     */
    public static float calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                   Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (float) ((bearing + 360) % 360);
    }
    
    /**
     * Calculate distance between two locations in meters
     */
    public static float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }
}
