package com.riyadhtransport.wear;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;
import com.riyadhtransport.wear.adapters.DestinationWithHeaderAdapter;
import com.riyadhtransport.wear.models.RouteInstruction;
import com.riyadhtransport.wear.models.WearFavorite;
import com.riyadhtransport.wear.utils.DataSyncHelper;
import com.riyadhtransport.wear.utils.ScreenUtils;
import com.riyadhtransport.wear.utils.WearLocationHelper;
import com.riyadhtransport.wear.api.WearApiClient;
import android.util.TypedValue;
import java.io.Serializable; // Import this
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements DestinationWithHeaderAdapter.OnDestinationClickListener {
    private WearableRecyclerView destinationsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private DestinationWithHeaderAdapter adapter;
    private DataSyncHelper dataSyncHelper;
    private WearLocationHelper locationHelper;

    private Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dataSyncHelper = new DataSyncHelper(this);
        locationHelper = new WearLocationHelper(this);

        initViews();
        loadData();
        getUserLocation();
    }

    private void initViews() {
        destinationsRecyclerView = findViewById(R.id.destinationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);

        // Setup single recycler view with curved layout for round screens
        adapter = new DestinationWithHeaderAdapter(this);
        destinationsRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        destinationsRecyclerView.setEdgeItemsCenteringEnabled(true);
        destinationsRecyclerView.setAdapter(adapter);
        
        // Scale content based on screen size
        scaleContentForScreen();
    }
    
    private void scaleContentForScreen() {
        TextView titleText = findViewById(R.id.titleText);
        
        // Get scaled sizes
        float titleSize = ScreenUtils.getScaledTextSize(this, 16f);
        float emptyTextSize = ScreenUtils.getScaledTextSize(this, 14f);
        
        // Apply scaled sizes
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize);
        emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, emptyTextSize);
    }

    private void loadData() {
        List<WearFavorite> favorites = dataSyncHelper.getFavorites();
        List<WearFavorite> history = dataSyncHelper.getSearchHistory();

        if (favorites.isEmpty() && history.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            destinationsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            destinationsRecyclerView.setVisibility(View.VISIBLE);
            // History first, then favorites as per feedback
            adapter.setData(history, favorites);
        }
    }

    private void getUserLocation() {
        locationHelper.getCurrentLocation(new WearLocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(Location location, boolean fromWatch) {
                userLocation = location;
            }

            @Override
            public void onLocationError(String error) {
                runOnUiThread(() -> Toast.makeText(SearchActivity.this, error, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void onDestinationClick(WearFavorite destination) {
        if (userLocation == null) {
            Toast.makeText(this, getString(R.string.gps_unavailable), Toast.LENGTH_LONG).show();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        destinationsRecyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE); // Hide empty text

        // Call real route API
        findRoute(destination);
    }

private void findRoute(WearFavorite destination) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("start_lat", userLocation.getLatitude());
        requestBody.put("start_lng", userLocation.getLongitude());
        requestBody.put("end_lat", destination.getLatitude());
        requestBody.put("end_lng", destination.getLongitude());

        WearApiClient.getTransportService().getRouteFromCoords(requestBody)
                .enqueue(new retrofit2.Callback<Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<Map<String, Object>> call,
                                 retrofit2.Response<Map<String, Object>> response) {

                progressBar.setVisibility(View.GONE); // Hide progress bar

                if (response.isSuccessful() && response.body() != null) {

                    // --- FIX: Parse the response body ---
                    Map<String, Object> responseBody = response.body();
                    List<RouteInstruction> instructions = parseRouteFromMap(responseBody);
                    int totalDurationMinutes = getTotalDurationFromMap(responseBody);
                    // --- END FIX ---

                    if (instructions.isEmpty()) {
                        // This can happen if parsing fails or the route is empty
                        Toast.makeText(SearchActivity.this, "Route is empty or could not be parsed.", Toast.LENGTH_LONG).show();
                        destinationsRecyclerView.setVisibility(View.VISIBLE); // Show the list again
                        return;
                    }

                    // Start RouteInstructionsActivity
                    Intent intent = new Intent(SearchActivity.this, RouteInstructionsActivity.class);
                    intent.putExtra("total_duration", totalDurationMinutes);
                    intent.putExtra("instructions_list", (Serializable) instructions);

                    startActivity(intent);
                    destinationsRecyclerView.setVisibility(View.VISIBLE); // Show list again

                } else {
                    // API call was not successful (e.g., 404, 500)
                    Toast.makeText(SearchActivity.this,
                            "Error: Could not find a route.", Toast.LENGTH_LONG).show();
                    destinationsRecyclerView.setVisibility(View.VISIBLE); // Show the list again
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                // Network error (no internet, server down)
                progressBar.setVisibility(View.GONE);
                destinationsRecyclerView.setVisibility(View.VISIBLE); // Show the list again
                Toast.makeText(SearchActivity.this,
                        "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * NEW HELPER METHOD
     * Parses the route instructions from the API response.
     */
    private List<RouteInstruction> parseRouteFromMap(Map<String, Object> body) {
        List<RouteInstruction> instructions = new ArrayList<>();
        try {
            if (body.get("routes") instanceof List) {
                List<?> routes = (List<?>) body.get("routes");
                if (routes.isEmpty() || !(routes.get(0) instanceof Map)) return instructions;

                Map<?, ?> route = (Map<?, ?>) routes.get(0);
                if (!(route.get("segments") instanceof List)) return instructions;

                List<?> segments = (List<?>) route.get("segments");
                for (Object segObj : segments) {
                    if (segObj instanceof Map) {
                        Map<?, ?> segment = (Map<?, ?>) segObj;
                        RouteInstruction instruction = new RouteInstruction();

                        String type = (String) segment.get("type");
                        double durationSeconds = ((Number) segment.get("duration")).doubleValue();

                        instruction.setType(type);
                        // Convert duration from seconds (from API) to minutes (for app)
                        instruction.setDuration((int) Math.round(durationSeconds / 60.0));

                        if ("walk".equals(type)) {
                            Object toObj = segment.get("to");
                            String toName = (toObj instanceof String) ? (String) toObj : "your destination";
                            instruction.setInstruction("Walk to " + toName);

                            double distanceMeters = ((Number) segment.get("distance")).doubleValue();
                            instruction.setDetails(String.format(java.util.Locale.US, "%.0f m", distanceMeters));
                        } else {
                            // "bus" or "metro"
                            instruction.setInstruction("Take " + segment.get("line"));

                            List<String> stations = (List<String>) segment.get("stations");
                            if (stations != null && !stations.isEmpty()) {
                                instruction.setDetails("Get off at " + stations.get(stations.size() - 1));
                            } else {
                                instruction.setDetails("Unknown stop");
                            }
                        }
                        instructions.add(instruction);
                    }
                }
            }
        } catch (Exception e) {
            // Log.e("SearchActivity", "Failed to parse route JSON", e);
            // Return any instructions we managed to parse
        }
        return instructions;
    }

    /**
     * NEW HELPER METHOD
     * Gets total duration from the API response.
     */
    private int getTotalDurationFromMap(Map<String, Object> body) {
        try {
            if (body.get("routes") instanceof List) {
                List<?> routes = (List<?>) body.get("routes");
                if (routes.isEmpty() || !(routes.get(0) instanceof Map)) return 0;

                Map<?, ?> route = (Map<?, ?>) routes.get(0);
                if (route.get("total_time") instanceof Number) {
                    double totalSeconds = ((Number) route.get("total_time")).doubleValue();
                    // Convert from seconds to minutes
                    return (int) Math.round(totalSeconds / 60.0);
                }
            }
        } catch (Exception e) {
            // Log.e("SearchActivity", "Failed to parse total_time", e);
        }
        return 0;
    }

    //
    // The createMockRoute() method has been deleted as requested.
    //
}
