package com.riyadhtransport.wear;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.riyadhtransport.wear.api.WearApiClient;
import com.riyadhtransport.wear.api.WearTransportService;
import com.riyadhtransport.wear.models.WearStation;
import com.riyadhtransport.wear.utils.ScreenUtils;
import com.riyadhtransport.wear.utils.WearLocationHelper;
import com.riyadhtransport.wear.views.CompassView;
import android.util.TypedValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationsNearMeActivity extends AppCompatActivity implements SensorEventListener {
    private CompassView compassView;
    private Button btnResetZoom;
    private ProgressBar progressBar;
    private TextView statusText;

    private WearLocationHelper locationHelper;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Location userLocation;
    private float currentAzimuth = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_near_me);

        locationHelper = new WearLocationHelper(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        initViews();
        setupCompassView();
        loadNearbyStations();
    }

    private void initViews() {
        compassView = findViewById(R.id.compassView);
        btnResetZoom = findViewById(R.id.btnResetZoom);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);

        btnResetZoom.setOnClickListener(v -> {
            compassView.resetZoom();
            btnResetZoom.setVisibility(View.GONE);
        });
        
        // Scale content based on screen size
        scaleContentForScreen();
    }
    
    private void scaleContentForScreen() {
        // Get scaled sizes
        float statusTextSize = ScreenUtils.getScaledTextSize(this, 14f);
        float buttonTextSize = ScreenUtils.getScaledTextSize(this, 12f);
        
        // Apply scaled sizes
        statusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, statusTextSize);
        btnResetZoom.setTextSize(TypedValue.COMPLEX_UNIT_SP, buttonTextSize);
    }

    private void setupCompassView() {
        compassView.setStationClickListener(new CompassView.StationClickListener() {
            @Override
            public void onStationClick(WearStation station) {
                // Open station details
                Intent intent = new Intent(StationsNearMeActivity.this, StationDetailsActivity.class);
                intent.putExtra("station_name", station.getDisplayName());
                intent.putExtra("station_type", station.getType());
                startActivity(intent);
            }

            @Override
            public void onClusterClick(List<WearStation> cluster, float clusterBearing) {
                // Show dialog with list of clustered stations
                showClusterDialog(cluster);
            }
        });
    }
    
    private void showClusterDialog(List<WearStation> stations) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_cluster_stations);
        
        androidx.wear.widget.WearableRecyclerView recyclerView = 
                dialog.findViewById(R.id.clusterStationsRecyclerView);
        androidx.wear.widget.WearableLinearLayoutManager layoutManager = 
                new androidx.wear.widget.WearableLinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setEdgeItemsCenteringEnabled(true);
        
        com.riyadhtransport.wear.adapters.ClusterStationAdapter adapter = 
                new com.riyadhtransport.wear.adapters.ClusterStationAdapter(station -> {
            dialog.dismiss();
            Intent intent = new Intent(StationsNearMeActivity.this, StationDetailsActivity.class);
            intent.putExtra("station_name", station.getDisplayName());
            intent.putExtra("station_type", station.getType());
            startActivity(intent);
        });
        
        adapter.setStations(stations);
        recyclerView.setAdapter(adapter);
        
        dialog.show();
    }

    private void loadNearbyStations() {
        statusText.setVisibility(View.VISIBLE);
        statusText.setText(R.string.loading_stations);
        progressBar.setVisibility(View.VISIBLE);

        locationHelper.getCurrentLocation(new WearLocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(Location location, boolean fromWatch) {
                userLocation = location;
                fetchNearbyStations(location);
            }

            @Override
            public void onLocationError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText(error);
                    Toast.makeText(StationsNearMeActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void fetchNearbyStations(Location location) {
        WearTransportService service = WearApiClient.getTransportService();

        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("lat", location.getLatitude());
        coordinates.put("lng", location.getLongitude());
        coordinates.put("radius", 1.5); // 1.5 km radius like main app

        service.getNearbyStations(coordinates).enqueue(new Callback<List<WearStation>>() {
            @Override
            public void onResponse(Call<List<WearStation>> call, Response<List<WearStation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<WearStation> stations = response.body();
                    if (stations.isEmpty()) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            statusText.setVisibility(View.VISIBLE);
                            statusText.setText(R.string.no_stations_nearby);
                        });
                    } else {
                        processStations(stations);
                    }
                } else {
                    // Use mock data if API fails
                    runOnUiThread(() -> {
                        statusText.setVisibility(View.VISIBLE);
                        statusText.setText("API error, using sample data");
                    });
                    useMockStations();
                }
            }

            @Override
            public void onFailure(Call<List<WearStation>> call, Throwable t) {
                // Use mock data if API fails
                runOnUiThread(() -> {
                    statusText.setVisibility(View.VISIBLE);
                    statusText.setText("Network error, using sample data");
                });
                useMockStations();
            }
        });
    }

    private void processStations(List<WearStation> stations) {
        if (userLocation == null) return;

        // Calculate bearing and normalized distance for each station
        float maxDistance = 0f;
        for (WearStation station : stations) {
            float bearing = WearLocationHelper.calculateBearing(
                userLocation.getLatitude(), userLocation.getLongitude(),
                station.getLatitude(), station.getLongitude()
            );
            float distance = WearLocationHelper.calculateDistance(
                userLocation.getLatitude(), userLocation.getLongitude(),
                station.getLatitude(), station.getLongitude()
            );

            station.setBearing(bearing);
            station.setDistance((double) distance);

            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }

        // Normalize distances (0.0 to 1.0)
        for (WearStation station : stations) {
            float normalizedDist = maxDistance > 0 ? station.getDistance().floatValue() / maxDistance : 0f;
            station.setNormalizedDistance(normalizedDist);
        }

        runOnUiThread(() -> {
            compassView.setStations(stations);
            progressBar.setVisibility(View.GONE);
            statusText.setVisibility(View.GONE);
        });
    }

    private void useMockStations() {
        // Create mock stations for demonstration
        if (userLocation == null) {
            userLocation = new Location("mock");
            userLocation.setLatitude(24.7136);
            userLocation.setLongitude(46.6753);
        }

        List<WearStation> mockStations = new java.util.ArrayList<>();

        // Add some mock stations around Riyadh
        WearStation station1 = new WearStation("olaya", "Olaya Metro Station", "metro", 24.6952, 46.6851);
        WearStation station2 = new WearStation("kafd", "KAFD Metro Station", "metro", 24.7661, 46.6373);
        WearStation station3 = new WearStation("malaz", "Malaz Bus Station", "bus", 24.6877, 46.7277);

        mockStations.add(station1);
        mockStations.add(station2);
        mockStations.add(station3);

        processStations(mockStations);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);

            // Convert to degrees
            currentAzimuth = (float) Math.toDegrees(orientation[0]);
            if (currentAzimuth < 0) {
                currentAzimuth += 360;
            }

            compassView.setUserDirection(currentAzimuth);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }
}
