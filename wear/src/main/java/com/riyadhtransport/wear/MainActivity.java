package com.riyadhtransport.wear;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.riyadhtransport.wear.utils.DataSyncHelper;
import com.riyadhtransport.wear.utils.WearLocationHelper;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Button btnSearch;
    private Button btnStationsNearMe;

    private WearLocationHelper locationHelper;
    private DataSyncHelper dataSyncHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationHelper = new WearLocationHelper(this);
        dataSyncHelper = new DataSyncHelper(this);

        // --- FIX: Removed the sample data ---
        // dataSyncHelper.initializeSampleData();
        // Real data will be loaded by DataSyncHelper from storage.

        initViews();
        setupListeners();
        checkLocationPermission();
    }

    private void initViews() {
        btnSearch = findViewById(R.id.btnSearch);
        btnStationsNearMe = findViewById(R.id.btnStationsNearMe);
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> {
            if (checkAndRequestLocationPermission()) {
                startActivity(new Intent(this, SearchActivity.class));
            }
        });

        btnStationsNearMe.setOnClickListener(v -> {
            if (checkAndRequestLocationPermission()) {
                startActivity(new Intent(this, StationsNearMeActivity.class));
            }
        });
    }

    private void checkLocationPermission() {
        if (locationHelper.hasLocationPermission()) {
            // Check GPS status
            locationHelper.getCurrentLocation(new WearLocationHelper.LocationCallback() {
                @Override
                public void onLocationReceived(Location location, boolean fromWatch) {
                    runOnUiThread(() -> {
                        // xd
                    });
                }

                @Override
                public void onLocationError(String error) {
                    runOnUiThread(() -> {
                        // xd
                    });
                }
            });
        }
    }

    private boolean checkAndRequestLocationPermission() {
        if (!locationHelper.hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
            );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.location_permission_required), Toast.LENGTH_LONG).show();
            }
        }
    }
}
