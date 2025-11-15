package com.riyadhtransport.wear;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast; // <-- FIX 1: Added missing import
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.riyadhtransport.wear.adapters.RouteInstructionAdapter;
import com.riyadhtransport.wear.models.RouteInstruction;
import java.io.Serializable; // Make sure this is here for casting
import java.util.ArrayList;
import java.util.List;

public class RouteInstructionsActivity extends AppCompatActivity {
    private ViewPager2 viewPager;

    private RouteInstructionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_instructions);

        initViews();
        loadInstructions();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);

        adapter = new RouteInstructionAdapter(() -> {
            // Return to main menu
            finish();
        });

        viewPager.setAdapter(adapter);
    }

    private void loadInstructions() {
        // --- Load real data from Intent ---
        List<RouteInstruction> instructions = (List<RouteInstruction>) getIntent().getSerializableExtra("instructions_list");
        int totalDurationFromIntent = getIntent().getIntExtra("total_duration", 0);

        // Check if data is valid
        if (instructions == null || instructions.isEmpty()) {
            // This Toast call is now valid
            Toast.makeText(this, "No instructions found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter.setInstructions(instructions, totalDurationFromIntent);
    }
}
