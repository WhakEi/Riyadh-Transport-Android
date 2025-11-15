package com.riyadhtransport;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Make sure Log is imported
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.adapters.FavoritesAdapter;
import com.riyadhtransport.models.Favorite;
import com.riyadhtransport.utils.FavoritesManager;

// Imports for Wear OS Syncing
import com.google.android.gms.wearable.Node; // Import Node
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson; // Import Gson

import java.nio.charset.StandardCharsets; // Import this
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesRecycler;
    private View emptyView;
    private FavoritesAdapter adapter;

    private Gson gson = new Gson();

    // --- Use this TAG to filter in Logcat ---
    private static final String TAG = "MobileSync";

    // --- Constants must match the Wear OS app's DataSyncHelper ---
    private static final String FAVORITES_PATH = "/favorites";

    // ... (onCreate, onResume, etc. are unchanged) ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.favorites);
        }

        // Initialize views
        favoritesRecycler = findViewById(R.id.favorites_recycler);
        emptyView = findViewById(R.id.empty_view);

        // Setup RecyclerView
        adapter = new FavoritesAdapter(this::onFavoriteClick, this::onFavoriteRemove);
        favoritesRecycler.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecycler.setAdapter(adapter);

        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        List<Favorite> favorites = FavoritesManager.getFavorites(this);
        adapter.setFavorites(favorites);

        if (favorites.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            favoritesRecycler.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            favoritesRecycler.setVisibility(View.VISIBLE);
        }

        // --- Sync this list to the watch ---
        syncFavoritesToWear(favorites);
    }

    private void onFavoriteClick(Favorite favorite) {
        // Set as destination in route fragment and go back to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("set_destination", true);
        intent.putExtra("destination_name", favorite.getName());
        intent.putExtra("destination_lat", favorite.getLatitude());
        intent.putExtra("destination_lng", favorite.getLongitude());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void onFavoriteRemove(Favorite favorite) {
        FavoritesManager.removeFavorite(this, favorite);
        loadFavorites(); // This will re-load and re-sync
    }

    /**
     * Converts the list of favorites to JSON and sends it to the Wear OS app
     * using the more reliable MessageClient API.
     */
    private void syncFavoritesToWear(List<Favorite> favorites) {
        if (favorites == null) {
            return;
        }

        // --- Check for connected nodes (watches) first ---
        Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(nodes -> {
            if (nodes.isEmpty()) {
                Log.e(TAG, "Paired watch detected: FAILED - No nodes found.");
                return;
            }

            Log.d(TAG, "Paired watch detected: SUCCESS - " + nodes.size() + " nodes found.");

            // Convert the list to a JSON string, then to bytes
            String favoritesJson = gson.toJson(favorites);
            byte[] payload = favoritesJson.getBytes(StandardCharsets.UTF_8);

            // Loop through all connected nodes and send the message
            for (Node node : nodes) {
                Log.d(TAG, "Attempting to send message to node: " + node.getDisplayName());

                Wearable.getMessageClient(this).sendMessage(node.getId(), FAVORITES_PATH, payload)
                        .addOnSuccessListener(result ->
                            Log.d(TAG, "Message Sync: SUCCESS - Message sent to " + node.getDisplayName())
                        )
                        .addOnFailureListener(e ->
                            Log.e(TAG, "Message Sync: FAILED - " + e.getMessage())
                        );
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Paired watch detected: FAILED - Could not check nodes.", e);
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
