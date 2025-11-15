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
    private TextView pageIndicator;

    private RouteInstructionAdapter adapter;
    // private int totalDuration; // No longer needed as class field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_instructions);

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
                // This call is now valid
                updatePageIndicator(position);
            }
        });
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
        // This call is now valid
        updatePageIndicator(0);
    }

    /**
     * <-- FIX 2: Added this method back in
     * Updates the page indicator text (e.g., "1 / 3")
     */
    private void updatePageIndicator(int position) {
        int totalPages = adapter.getItemCount();
        pageIndicator.setText((position + 1) + " / " + totalPages);
    }
}
