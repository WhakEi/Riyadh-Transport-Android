package com.riyadhtransport;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.adapters.LineAdapter;
import com.riyadhtransport.models.Arrival;
import com.riyadhtransport.models.Line;
import com.riyadhtransport.utils.LineColorHelper;
import com.riyadhtransport.utils.LiveArrivalManager;
import java.util.ArrayList;
import java.util.List;

public class StationLinesActivity extends AppCompatActivity {
    
    private static final String TAG = "StationLinesActivity";
    private static final long REFRESH_INTERVAL_MS = 60000; // 60 seconds
    
    private TextView stationNameView;
    private RecyclerView linesRecycler;
    private LineAdapter adapter;
    private String stationName;
    private List<Line> allLines;
    
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_lines);
        
        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Get station info from intent
        stationName = getIntent().getStringExtra("station_name");
        ArrayList<String> metroLines = getIntent().getStringArrayListExtra("metro_lines");
        ArrayList<String> busLines = getIntent().getStringArrayListExtra("bus_lines");
        
        // Initialize views
        stationNameView = findViewById(R.id.station_name);
        linesRecycler = findViewById(R.id.lines_list);
        
        // Set station name
        stationNameView.setText(stationName);
        
        // Setup RecyclerView
        adapter = new LineAdapter(this::onLineClick);
        linesRecycler.setLayoutManager(new LinearLayoutManager(this));
        linesRecycler.setAdapter(adapter);
        
        // Build lines list
        allLines = new ArrayList<>();
        
        if (metroLines != null) {
            for (String lineId : metroLines) {
                String lineName = LineColorHelper.getMetroLineName(this, lineId);
                allLines.add(new Line(lineId, lineName, "metro"));
            }
        }
        
        if (busLines != null) {
            for (String lineId : busLines) {
                allLines.add(new Line(lineId, getString(R.string.bus) + " " + lineId, "bus"));
            }
        }
        
        adapter.setLines(allLines);
        
        // Start live arrival updates
        setupAutoRefresh();
    }
    
    private void setupAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshLiveArrivals();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        
        // Start the first refresh immediately
        refreshLiveArrivals();
    }
    
    private void refreshLiveArrivals() {
        if (stationName == null || allLines == null) return;
        
        Log.d(TAG, "Refreshing live arrivals for station: " + stationName);
        
        // Fetch arrivals for each line
        for (Line line : allLines) {
            line.setArrivalStatus("checking");
            
            String segmentType = line.isMetro() ? "metro" : "bus";
            
            LiveArrivalManager.getLiveArrivals(
                stationName,
                segmentType,
                line.getId(),
                null, // No specific destination filter
                new LiveArrivalManager.ArrivalCallback() {
                    @Override
                    public void onSuccess(List<Arrival> arrivals) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Got " + arrivals.size() + " arrivals for line " + line.getId());
                            
                            // Extract arrival times
                            List<Integer> upcomingArrivals = new ArrayList<>();
                            for (Arrival arrival : arrivals) {
                                if (upcomingArrivals.size() >= 3) break;
                                upcomingArrivals.add(arrival.getMinutesUntil());
                            }
                            
                            line.setUpcomingArrivals(upcomingArrivals);
                            
                            // Set status based on arrival time
                            if (!upcomingArrivals.isEmpty()) {
                                int firstArrival = upcomingArrivals.get(0);
                                if (firstArrival >= 59) {
                                    line.setArrivalStatus("normal");
                                } else {
                                    line.setArrivalStatus("live");
                                }
                            } else {
                                line.setArrivalStatus("hidden");
                            }
                            
                            adapter.notifyDataSetChanged();
                        });
                    }
                    
                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error getting arrivals for line " + line.getId() + ": " + message);
                            line.setArrivalStatus("hidden");
                            adapter.notifyDataSetChanged();
                        });
                    }
                });
        }
    }
    
    private void onLineClick(Line line) {
        // Open LineStationsActivity to show stations on this line
        Intent intent = new Intent(this, LineStationsActivity.class);
        intent.putExtra("line_id", line.getId());
        intent.putExtra("line_name", line.getName());
        intent.putExtra("line_type", line.getType());
        startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
