package com.riyadhtransport;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.riyadhtransport.adapters.RouteSegmentAdapter;
import com.riyadhtransport.models.Route;
import com.riyadhtransport.utils.JourneyTimeCalculator;

public class RouteDetailsActivity extends AppCompatActivity {
    private static final String TAG = "RouteDetailsActivity";
    private static final long REFRESH_INTERVAL_MS = 60000; // 60 seconds
    
    private RecyclerView routeSegmentsRecycler;
    private RouteSegmentAdapter adapter;
    private TextView totalTimeText;
    private Route currentRoute;
    
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);
        
        // Initialize views
        routeSegmentsRecycler = findViewById(R.id.route_segments_recycler);
        totalTimeText = findViewById(R.id.total_time_text);
        
        // Setup RecyclerView
        routeSegmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RouteSegmentAdapter();
        routeSegmentsRecycler.setAdapter(adapter);
        
        // Get route data from intent
        String routeJson = getIntent().getStringExtra("route_json");
        if (routeJson != null) {
            Gson gson = new Gson();
            currentRoute = gson.fromJson(routeJson, Route.class);
            
            if (currentRoute != null) {
                displayRoute();
                setupAutoRefresh();
            }
        }
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
