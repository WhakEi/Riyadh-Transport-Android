package com.riyadhtransport;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.riyadhtransport.adapters.RouteSegmentAdapter;
import com.riyadhtransport.adapters.AlertAdapter;
import com.riyadhtransport.models.Route;
import com.riyadhtransport.models.RouteSegment;
import com.riyadhtransport.models.LineAlert;
import com.riyadhtransport.utils.JourneyTimeCalculator;
import com.riyadhtransport.utils.AlertsManager;
import java.util.HashSet;
import java.util.Set;

public class RouteDetailsActivity extends AppCompatActivity {
    private static final String TAG = "RouteDetailsActivity";
    private static final long REFRESH_INTERVAL_MS = 60000; // 60 seconds
    
    private RecyclerView routeSegmentsRecycler;
    private RouteSegmentAdapter adapter;
    private TextView totalTimeText;
    private Route currentRoute;
    
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    
    private LinearLayout alertsContainer;
    private RecyclerView alertsRecycler;
    private AlertAdapter alertAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);
        
        // Initialize views
        routeSegmentsRecycler = findViewById(R.id.route_segments_recycler);
        totalTimeText = findViewById(R.id.total_time_text);
        alertsContainer = findViewById(R.id.alerts_container);
        alertsRecycler = findViewById(R.id.alerts_recycler);
        
        // Setup RecyclerView
        routeSegmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RouteSegmentAdapter();
        routeSegmentsRecycler.setAdapter(adapter);
        
        // Setup Alerts RecyclerView
        alertAdapter = new AlertAdapter();
        alertsRecycler.setLayoutManager(new LinearLayoutManager(this));
        alertsRecycler.setAdapter(alertAdapter);
        
        // Get route data from intent
        String routeJson = getIntent().getStringExtra("route_json");
        if (routeJson != null) {
            Gson gson = new Gson();
            currentRoute = gson.fromJson(routeJson, Route.class);
            
            if (currentRoute != null) {
                displayRoute();
                setupAutoRefresh();
                loadLineSpecificAlerts();
            }
        }
    }
    
    private void loadLineSpecificAlerts() {
        if (currentRoute == null || currentRoute.getSegments() == null) {
            return;
        }
        
        // Collect all line numbers from the route
        Set<String> lineNumbers = new HashSet<>();
        for (RouteSegment segment : currentRoute.getSegments()) {
            if (segment.isMetro() || segment.isBus()) {
                String line = segment.getLine();
                if (line != null && !line.isEmpty()) {
                    lineNumbers.add(line);
                }
            }
        }
        
        if (lineNumbers.isEmpty()) {
            alertsContainer.setVisibility(View.GONE);
            return;
        }
        
        // Fetch all alerts and filter for the lines in this route
        AlertsManager.getAlerts(this, new AlertsManager.AlertsCallback() {
            @Override
            public void onSuccess(java.util.List<LineAlert> allAlerts) {
                runOnUiThread(() -> {
                    java.util.List<LineAlert> relevantAlerts = new java.util.ArrayList<>();
                    for (LineAlert alert : allAlerts) {
                        if (alert.isLineSpecific()) {
                            // Check if this alert applies to any line in the route
                            for (String lineNumber : lineNumbers) {
                                if (alert.appliesToLine(lineNumber)) {
                                    relevantAlerts.add(alert);
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (!relevantAlerts.isEmpty()) {
                        alertAdapter.setAlerts(relevantAlerts);
                        alertsContainer.setVisibility(View.VISIBLE);
                    } else {
                        alertsContainer.setVisibility(View.GONE);
                    }
                });
            }
            
            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading alerts: " + message);
                runOnUiThread(() -> alertsContainer.setVisibility(View.GONE));
            }
        });
    }
    
    private void displayRoute() {
        if (currentRoute == null) return;
        
        adapter.setSegments(currentRoute.getSegments());
        updateTotalTime(currentRoute.getTotalMinutes());
    }
    
    private void updateTotalTime(int minutes) {
        if (totalTimeText != null) {
            totalTimeText.setText(getString(R.string.total_time) + ": " + 
                                 minutes + " " + getString(R.string.minutes));
        }
    }
    
    private void setupAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshLiveData();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        
        // Start the first refresh immediately
        refreshLiveData();
    }
    
    private void refreshLiveData() {
        if (currentRoute == null) return;
        
        Log.d(TAG, "Refreshing live arrival data...");
        
        JourneyTimeCalculator.calculateLiveJourneyTime(currentRoute, 
            new JourneyTimeCalculator.CalculationCallback() {
                @Override
                public void onComplete(int newTotalMinutes) {
                    runOnUiThread(() -> {
                        Log.d(TAG, "Journey time updated: " + newTotalMinutes + " minutes");
                        updateTotalTime(newTotalMinutes);
                        adapter.notifyDataSetChanged();
                    });
                }
                
                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error calculating journey time: " + message);
                }
            });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.post(refreshRunnable);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}
