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
import com.riyadhtransport.wear.utils.WearLocationHelper;
import java.util.ArrayList;
import java.util.List;

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
        
        // In a real implementation, we would call the route API here
        // For now, create mock route instructions
        createMockRoute(destination);
    }
    
    private void createMockRoute(WearFavorite destination) {
        // Mock route instructions for demonstration
        List<RouteInstruction> instructions = new ArrayList<>();
        
        RouteInstruction walk1 = new RouteInstruction();
        walk1.setType("walk");
        walk1.setInstruction("Walk to nearest metro station");
        walk1.setDetails("Head north on King Fahd Road");
        walk1.setDuration(5);
        instructions.add(walk1);
        
        RouteInstruction metro = new RouteInstruction();
        metro.setType("metro");
        metro.setInstruction("Take Blue Line");
        metro.setDetails("Direction: KAFD, Get off at Olaya");
        metro.setDuration(12);
        instructions.add(metro);
        
        RouteInstruction walk2 = new RouteInstruction();
        walk2.setType("walk");
        walk2.setInstruction("Walk to destination");
        walk2.setDetails(destination.getName());
        walk2.setDuration(3);
        instructions.add(walk2);
        
        // Calculate total duration
        int totalDuration = 0;
        for (RouteInstruction instruction : instructions) {
            totalDuration += instruction.getDuration();
        }
        
        // Start RouteInstructionsActivity
        Intent intent = new Intent(this, RouteInstructionsActivity.class);
        intent.putExtra("total_duration", totalDuration);
        // In production, we would pass the instructions as a serializable list
        startActivity(intent);
        
        progressBar.setVisibility(View.GONE);
        destinationsRecyclerView.setVisibility(View.VISIBLE);
    }
}
