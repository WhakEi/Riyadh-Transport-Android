package com.riyadhtransport.wear;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.riyadhtransport.wear.adapters.RouteInstructionAdapter;
import com.riyadhtransport.wear.models.RouteInstruction;
import java.util.ArrayList;
import java.util.List;

public class RouteInstructionsActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TextView pageIndicator;
    
    private RouteInstructionAdapter adapter;
    private int totalDuration;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_instructions);
        
        totalDuration = getIntent().getIntExtra("total_duration", 0);
        
        initViews();
        loadInstructions();
    }
    
    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        pageIndicator = findViewById(R.id.pageIndicator);
        
        adapter = new RouteInstructionAdapter(() -> {
            // Return to main menu
            finish();
        });
        
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updatePageIndicator(position);
            }
        });
    }
    
    private void loadInstructions() {
        // Create mock instructions - in production, these would come from the API
        List<RouteInstruction> instructions = new ArrayList<>();
        
        RouteInstruction walk1 = new RouteInstruction();
        walk1.setType("walk");
        walk1.setInstruction("Walk to metro station");
        walk1.setDetails("Head north on King Fahd Road");
        walk1.setDuration(5);
        instructions.add(walk1);
        
        RouteInstruction metro = new RouteInstruction();
        metro.setType("metro");
        metro.setInstruction("Take Blue Line");
        metro.setDetails("Direction: KAFD\nGet off at Olaya");
        metro.setDuration(12);
        instructions.add(metro);
        
        RouteInstruction walk2 = new RouteInstruction();
        walk2.setType("walk");
        walk2.setInstruction("Walk to destination");
        walk2.setDetails("0.3 km");
        walk2.setDuration(3);
        instructions.add(walk2);
        
        adapter.setInstructions(instructions, totalDuration > 0 ? totalDuration : 20);
        updatePageIndicator(0);
    }
    
    private void updatePageIndicator(int position) {
        int totalPages = adapter.getItemCount();
        pageIndicator.setText((position + 1) + " / " + totalPages);
    }
}
