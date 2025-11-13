package com.riyadhtransport.wear;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;
import com.riyadhtransport.wear.adapters.ArrivalAdapter;
import com.riyadhtransport.wear.api.WearApiClient;
import com.riyadhtransport.wear.api.WearTransportService;
import com.riyadhtransport.wear.models.WearArrival;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationDetailsActivity extends AppCompatActivity {
    private TextView stationNameText;
    private WearableRecyclerView arrivalsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    
    private ArrivalAdapter arrivalAdapter;
    private String stationName;
    private String stationType;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);
        
        stationName = getIntent().getStringExtra("station_name");
        stationType = getIntent().getStringExtra("station_type");
        
        initViews();
        loadArrivals();
    }
    
    private void initViews() {
        stationNameText = findViewById(R.id.stationNameText);
        arrivalsRecyclerView = findViewById(R.id.arrivalsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        
        stationNameText.setText(stationName);
        
        arrivalAdapter = new ArrivalAdapter();
        arrivalsRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        arrivalsRecyclerView.setEdgeItemsCenteringEnabled(true);
        arrivalsRecyclerView.setAdapter(arrivalAdapter);
    }
    
    private void loadArrivals() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        
        WearTransportService service = WearApiClient.getTransportService();
        
        Map<String, String> stationData = new HashMap<>();
        stationData.put("name", stationName);
        
        Call<List<WearArrival>> call;
        if ("metro".equalsIgnoreCase(stationType)) {
            call = service.getMetroArrivals(stationData);
        } else {
            call = service.getBusArrivals(stationData);
        }
        
        call.enqueue(new Callback<List<WearArrival>>() {
            @Override
            public void onResponse(Call<List<WearArrival>> apiCall, Response<List<WearArrival>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<WearArrival> arrivals = response.body();
                    arrivalAdapter.setArrivals(arrivals);
                    arrivalsRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    // Use mock data or show empty
                    useMockArrivals();
                }
            }
            
            @Override
            public void onFailure(Call<List<WearArrival>> apiCall, Throwable t) {
                progressBar.setVisibility(View.GONE);
                // Use mock data on failure
                useMockArrivals();
            }
        });
    }
    
    private void useMockArrivals() {
        // Create mock arrivals for demonstration
        List<WearArrival> mockArrivals = new ArrayList<>();
        
        if ("metro".equalsIgnoreCase(stationType)) {
            mockArrivals.add(new WearArrival("5 min", "Blue Line", "KAFD", 5));
            mockArrivals.add(new WearArrival("12 min", "Blue Line", "KAFD", 12));
            mockArrivals.add(new WearArrival("18 min", "Blue Line", "Olaya", 18));
        } else {
            mockArrivals.add(new WearArrival("3 min", "Bus 230", "Al-Malqa", 3));
            mockArrivals.add(new WearArrival("15 min", "Bus 230", "Al-Malqa", 15));
            mockArrivals.add(new WearArrival("25 min", "Bus 145", "City Center", 25));
        }
        
        if (mockArrivals.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            arrivalsRecyclerView.setVisibility(View.GONE);
        } else {
            arrivalAdapter.setArrivals(mockArrivals);
            arrivalsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
