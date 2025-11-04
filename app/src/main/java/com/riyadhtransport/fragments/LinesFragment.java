package com.riyadhtransport.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.riyadhtransport.LineStationsActivity;
import com.riyadhtransport.R;
import com.riyadhtransport.adapters.LineAdapter;
import com.riyadhtransport.api.ApiClient;
import com.riyadhtransport.models.Line;
import com.riyadhtransport.utils.LineColorHelper;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger; // NEW: Import for counter
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LinesFragment extends Fragment {

    private static final String PREFS_NAME = "LinesCache";
    private static final long CACHE_DURATION = 7 * 24 * 60 * 60 * 1000L; // 1 week in milliseconds

    private TextInputEditText searchInput;
    private RecyclerView linesRecycler;
    private LineAdapter lineAdapter;
    private ProgressBar progressBar;
    private boolean linesLoaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        searchInput = view.findViewById(R.id.search_lines);
        linesRecycler = view.findViewById(R.id.lines_recycler);
        progressBar = view.findViewById(R.id.progress_bar);

        // Setup RecyclerView
        lineAdapter = new LineAdapter(line -> showLineDetails(line));
        linesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        linesRecycler.setAdapter(lineAdapter);

        // Setup search
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lineAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load lines data (with caching)
        if (!linesLoaded) {
            loadLines();
        }
    }

    // MODIFIED: Removed background refresh logic
    private void loadLines() {
        // Check cache first
        List<Line> cachedLines = loadFromCache();
        if (cachedLines != null && !cachedLines.isEmpty()) {
            lineAdapter.setLines(cachedLines); // Show cached data immediately
            linesLoaded = true;
            // We will NOT refresh in the background. We'll wait for the cache to expire.
        } else {
            // Cache miss or expired - fetch from API
            fetchLinesFromApi();
        }
    }

    private void fetchLinesFromApi() {
        progressBar.setVisibility(View.VISIBLE);
        List<Line> allLines = new ArrayList<>();

        // Load metro lines
        ApiClient.getApiService().getMetroLines().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String linesStr = response.body().get("lines").getAsString();
                    String[] metroLines = linesStr.split(",");

                    for (String lineId : metroLines) {
                        String lineName = LineColorHelper.getMetroLineName(requireContext(), lineId);
                        allLines.add(new Line(lineId, lineName, "metro"));
                    }

                    // After metro lines loaded, load bus lines
                    loadBusLines(allLines);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        getString(R.string.error_network),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBusLines(List<Line> allLines) {
        ApiClient.getApiService().getBusLines().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String linesStr = response.body().get("lines").getAsString();
                    String[] busLines = linesStr.split(",");

                    for (String lineId : busLines) {
                        allLines.add(new Line(lineId, getString(R.string.bus) + " " + lineId, "bus"));
                    }
                }
                // MODIFIED: Removed 'false' for foreground fetch
                fetchSummariesForAllLines(allLines);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                // MODIFIED: Removed 'false' for foreground fetch
                fetchSummariesForAllLines(allLines);
            }
        });
    }

    // MODIFIED: Removed isBackgroundUpdate flag
    private void fetchSummariesForAllLines(List<Line> allLines) {
        if (allLines.isEmpty()) {
             progressBar.setVisibility(View.GONE);
            return;
        }

        AtomicInteger counter = new AtomicInteger(allLines.size());

        for (Line line : allLines) {
            if (line.isMetro()) {
                // --- Fetch Metro Summary ---
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("line", line.getId());

                ApiClient.getApiService().viewMetro(requestBody).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject data = response.body();
                            List<String> stations = new ArrayList<>();
                            if (data.has("stations")) {
                                data.getAsJsonArray("stations").forEach(element ->
                                        stations.add(element.getAsString()));
                            }
                            if (stations.size() >= 2) {
                                String firstStation = stations.get(0);
                                String lastStation = stations.get(stations.size() - 1);
                                line.setRouteSummary(firstStation + " - " + lastStation);
                            }
                        }
                        if (counter.decrementAndGet() == 0) {
                            allLinesLoaded(allLines);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (counter.decrementAndGet() == 0) {
                            allLinesLoaded(allLines);
                        }
                    }
                });
            } else {
                // --- Fetch Bus Summary ---
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("line", line.getId());

                ApiClient.getApiService().viewBus(requestBody).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject data = response.body();
                            List<String> directions = new ArrayList<>();
                            for (Map.Entry<String, com.google.gson.JsonElement> entry : data.entrySet()) {
                                directions.add(entry.getKey());
                            }
                            if (directions.size() >= 2) {
                                line.setRouteSummary(directions.get(0) + " - " + directions.get(1));
                            } else if (directions.size() == 1) {
                                line.setRouteSummary(getString(R.string.ring_route_format, directions.get(0)));
                            }
                        }
                        if (counter.decrementAndGet() == 0) {
                            allLinesLoaded(allLines);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (counter.decrementAndGet() == 0) {
                            allLinesLoaded(allLines);
                        }
                    }
                });
            }
        }
    }

    // MODIFIED: Removed isBackgroundUpdate flag
    private void allLinesLoaded(List<Line> allLines) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                lineAdapter.setLines(allLines); // This will refresh the list with summaries
                linesLoaded = true;
                saveToCache(allLines); // Save the fresh data
            });
        }
    }


    private String getCacheKey(String baseName) {
        // Create language-specific cache keys
        String language = com.riyadhtransport.utils.LocaleHelper.getLanguageCode(requireContext());
        return baseName + "_" + language;
    }

    private List<Line> loadFromCache() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String keyLines = getCacheKey("cached_lines");
        String keyTimestamp = getCacheKey("cache_timestamp");

        long timestamp = prefs.getLong(keyTimestamp, 0);
        long currentTime = System.currentTimeMillis();

        // Check if cache is still valid (within 1 week)
        if (currentTime - timestamp > CACHE_DURATION) {
            return null;
        }

        String json = prefs.getString(keyLines, null);
        if (json == null) {
            return null;
        }

        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Line>>(){}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            return null;
        }
    }

    private void saveToCache(List<Line> lines) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String keyLines = getCacheKey("cached_lines");
        String keyTimestamp = getCacheKey("cache_timestamp");

        Gson gson = new Gson();
        String json = gson.toJson(lines);

        prefs.edit()
            .putString(keyLines, json)
            .putLong(keyTimestamp, System.currentTimeMillis())
            .apply();
    }

    public static void clearCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void showLineDetails(Line line) {
        // Fetch line data from backend
        if (line.isMetro()) {
            loadMetroLineDetails(line);
        } else {
            loadBusLineDetails(line);
        }
    }

    private void loadMetroLineDetails(Line line) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("line", line.getId());

        ApiClient.getApiService().viewMetro(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    List<String> stations = new ArrayList<>();
                    if (data.has("stations")) {
                        data.getAsJsonArray("stations").forEach(element ->
                                stations.add(element.getAsString()));
                    }

                    // Set route summary for metro: first - last station
                    // This is still useful in case the summary failed to load initially
                    if (stations.size() >= 2 && (line.getRouteSummary() == null || line.getRouteSummary().isEmpty())) {
                        String firstStation = stations.get(0);
                        String lastStation = stations.get(stations.size() - 1);
                        line.setRouteSummary(firstStation + " - " + lastStation);
                    }

                    showStationsList(line, stations);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        getString(R.string.error_network),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBusLineDetails(Line line) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("line", line.getId());

        ApiClient.getApiService().viewBus(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    List<String> directions = new ArrayList<>();
                    for (Map.Entry<String, com.google.gson.JsonElement> entry : data.entrySet()) {
                        directions.add(entry.getKey());
                    }

                    if (line.getRouteSummary() == null || line.getRouteSummary().isEmpty()) {
                        if (directions.size() >= 2) {
                            line.setRouteSummary(directions.get(0) + " - " + directions.get(1));
                        }
                        else if (directions.size() == 1) {
                            // MODIFIED: Use string resource for localization
                            line.setRouteSummary(getString(R.string.ring_route_format, directions.get(0)));
                        }
                    }

                    if (directions.size() == 1) {
                        // Ring route - single direction
                        List<String> stations = new ArrayList<>();
                        data.getAsJsonArray(directions.get(0)).forEach(element ->
                                stations.add(element.getAsString()));
                        showStationsList(line, stations);
                    } else if (directions.size() >= 2) {
                        // Bi-directional route - show direction selector
                        showDirectionSelector(line, data, directions);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        getString(R.string.error_network),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDirectionSelector(Line line, JsonObject lineData, List<String> directions) {
        String dir1 = directions.get(0);
        String dir2 = directions.get(1);

        // MODIFIED: Check language for RTL arrow
        String language = com.riyadhtransport.utils.LocaleHelper.getLanguageCode(requireContext());
        String arrow = " → ";
        if (language.equals("ar")) {
            arrow = " ← ";
        }

        // MODIFIED: Use the arrow variable and fix typo
        String[] options = {
                dir1 + arrow + dir2,
                dir2 + arrow + dir1
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_direction)
                .setItems(options, (dialog, which) -> {
                    String selectedDirection = which == 0 ? dir1 : dir2;
                    List<String> stations = new ArrayList<>();
                    lineData.getAsJsonArray(selectedDirection).forEach(element ->
                            stations.add(element.getAsString()));

                    // Open activity with direction info
                    Intent intent = new Intent(requireContext(), LineStationsActivity.class);
                    intent.putExtra("line_id", line.getId());
                    intent.putExtra("line_name", line.getName());
                    intent.putExtra("line_type", line.getType());
                    intent.putExtra("direction", selectedDirection);
                    intent.putStringArrayListExtra("stations", new ArrayList<>(stations));
                    startActivity(intent);
                })
                .show();
    }

    private void showStationsList(Line line, List<String> stations) {
        // Open dedicated activity to show stations list
        Intent intent = new Intent(requireContext(), LineStationsActivity.class);
        intent.putExtra("line_id", line.getId());
        intent.putExtra("line_name", line.getName());
        intent.putExtra("line_type", line.getType());
        intent.putStringArrayListExtra("stations", new ArrayList<>(stations));
        startActivity(intent);
    }
}
