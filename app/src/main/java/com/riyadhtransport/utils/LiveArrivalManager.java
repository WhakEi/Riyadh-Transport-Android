package com.riyadhtransport.utils;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riyadhtransport.api.ApiClient;
import com.riyadhtransport.models.Arrival;
import com.riyadhtransport.models.ArrivalResponse;
import com.riyadhtransport.models.RefinedTerminusResponse;
import com.riyadhtransport.models.StationDeparture;
import com.riyadhtransport.models.StationIdResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveArrivalManager {
    private static final String TAG = "LiveArrivalManager";
    private static final int MAX_WAIT_MINUTES = 45;
    
    public interface ArrivalCallback {
        void onSuccess(List<Arrival> arrivals);
        void onError(String message);
    }
    
    /**
     * Get live arrivals for a station. First tries the primary APIs, then falls back to RPT.
     */
    public static void getLiveArrivals(String stationName, String segmentType, 
                                        String lineNumber, String finalDestination,
                                        ArrivalCallback callback) {
        Log.d(TAG, "Getting live arrivals for: " + stationName + ", type: " + segmentType);
        
        // Try primary API first
        Map<String, String> request = new HashMap<>();
        request.put("station_name", stationName);
        
        Call<Map<String, Object>> call;
        if ("metro".equalsIgnoreCase(segmentType)) {
            call = ApiClient.getApiService().getMetroArrivals(request);
        } else {
            call = ApiClient.getApiService().getBusArrivals(request);
        }
        
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> body = response.body();
                        if (body.containsKey("arrivals")) {
                            // Parse arrivals
                            Gson gson = new Gson();
                            String arrivalsJson = gson.toJson(body.get("arrivals"));
                            List<Arrival> arrivals = gson.fromJson(arrivalsJson, 
                                new TypeToken<List<Arrival>>(){}.getType());
                            
                            if (arrivals != null && !arrivals.isEmpty()) {
                                Log.d(TAG, "Primary API success: " + arrivals.size() + " arrivals");
                                callback.onSuccess(arrivals);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing primary API response", e);
                    }
                }
                
                // Primary API failed, use fallback
                Log.d(TAG, "Primary API failed, using fallback");
                useFallbackApi(stationName, lineNumber, finalDestination, callback);
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Primary API call failed", t);
                useFallbackApi(stationName, lineNumber, finalDestination, callback);
            }
        });
    }
    
    /**
     * Fallback method using giveMeId and RPT station details API
     */
    private static void useFallbackApi(String stationName, String lineNumber, 
                                        String finalDestination, ArrivalCallback callback) {
        Log.d(TAG, "Using fallback API for: " + stationName);
        
        // Normalize line number (convert "Blue Line" to "1", etc.)
        String normalizedLineNumber = normalizeMetroLine(lineNumber);
        Log.d(TAG, "Normalized line number: " + lineNumber + " -> " + normalizedLineNumber);
        
        // Step 1: Get station ID
        Map<String, String> request = new HashMap<>();
        request.put("station_name", stationName);
        
        ApiClient.getApiService().getStationId(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Gson gson = new Gson();
                        String json = gson.toJson(response.body());
                        StationIdResponse stationIdResp = gson.fromJson(json, StationIdResponse.class);
                        
                        if (stationIdResp.getMatches() != null && !stationIdResp.getMatches().isEmpty()) {
                            String stationId = stationIdResp.getMatches().get(0).getStationId();
                            Log.d(TAG, "Got station ID: " + stationId);
                            
                            // Step 2: Get station departures from RPT (use normalized line number)
                            getRptDepartures(stationId, normalizedLineNumber, finalDestination, callback);
                        } else {
                            callback.onError("No station matches found");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing station ID response", e);
                        callback.onError("Failed to parse station ID");
                    }
                } else {
                    callback.onError("Failed to get station ID");
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Failed to get station ID", t);
                callback.onError("Network error getting station ID");
            }
        });
    }
    
    /**
     * Get departures from RPT station details API
     */
    private static void getRptDepartures(String stationId, String lineNumber, 
                                          String finalDestination, ArrivalCallback callback) {
        Log.d(TAG, "Getting RPT departures for station ID: " + stationId);
        
        Map<String, String> fields = new HashMap<>();
        String fieldName = "_com_rcrc_stations_RcrcStationDetailsPortlet_INSTANCE_53WVbOYPfpUF_busStopId";
        fields.put(fieldName, stationId);
        
        ApiClient.getRptStationService().getStationDepartures(fields).enqueue(
            new Callback<List<StationDeparture>>() {
                @Override
                public void onResponse(Call<List<StationDeparture>> call, 
                                       Response<List<StationDeparture>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<StationDeparture> departures = response.body();
                        Log.d(TAG, "Got " + departures.size() + " departures from RPT");
                        
                        // Convert departures to arrivals
                        convertDeparturesToArrivals(departures, lineNumber, finalDestination, callback);
                    } else {
                        Log.e(TAG, "RPT API failed with code: " + response.code());
                        callback.onError("Failed to get departures from RPT");
                    }
                }
                
                @Override
                public void onFailure(Call<List<StationDeparture>> call, Throwable t) {
                    Log.e(TAG, "RPT API call failed", t);
                    callback.onError("Network error getting departures");
                }
            });
    }
    
    /**
     * Convert RPT departures to Arrival objects with refined terminus
     */
    private static void convertDeparturesToArrivals(List<StationDeparture> departures,
                                                     String lineNumber, String finalDestination,
                                                     ArrivalCallback callback) {
        List<Arrival> arrivals = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        Date now = new Date();
        
        // Filter departures by line number if provided
        List<StationDeparture> filteredDepartures = new ArrayList<>();
        for (StationDeparture dep : departures) {
            if (lineNumber == null || lineNumber.isEmpty() || 
                lineNumber.equals(dep.getNumber())) {
                filteredDepartures.add(dep);
            }
        }
        
        if (filteredDepartures.isEmpty()) {
            callback.onError("No departures found for line " + lineNumber);
            return;
        }
        
        // Get refined terminus for the first departure
        StationDeparture firstDep = filteredDepartures.get(0);
        refineTerminusAndComplete(firstDep.getNumber(), firstDep.getDestination(), 
            new TerminusCallback() {
                @Override
                public void onSuccess(String refinedTerminus) {
                    // Convert all departures to arrivals
                    for (StationDeparture dep : filteredDepartures) {
                        try {
                            Date departureTime = dateFormat.parse(dep.getActualDepartureTimePlanned());
                            if (departureTime != null) {
                                long diffMs = departureTime.getTime() - now.getTime();
                                int minutesUntil = (int) Math.max(0, diffMs / 60000);
                                
                                Arrival arrival = new Arrival();
                                arrival.setLine(dep.getNumber());
                                arrival.setDestination(refinedTerminus != null ? refinedTerminus : dep.getDestination());
                                arrival.setMinutesUntil(minutesUntil);
                                
                                arrivals.add(arrival);
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing departure time", e);
                        }
                    }
                    
                    Log.d(TAG, "Converted " + arrivals.size() + " arrivals");
                    callback.onSuccess(arrivals);
                }
                
                @Override
                public void onError(String message) {
                    // Use original destinations if refinement fails
                    for (StationDeparture dep : filteredDepartures) {
                        try {
                            Date departureTime = dateFormat.parse(dep.getActualDepartureTimePlanned());
                            if (departureTime != null) {
                                long diffMs = departureTime.getTime() - now.getTime();
                                int minutesUntil = (int) Math.max(0, diffMs / 60000);
                                
                                Arrival arrival = new Arrival();
                                arrival.setLine(dep.getNumber());
                                arrival.setDestination(dep.getDestination());
                                arrival.setMinutesUntil(minutesUntil);
                                
                                arrivals.add(arrival);
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing departure time", e);
                        }
                    }
                    
                    callback.onSuccess(arrivals);
                }
            });
    }
    
    private interface TerminusCallback {
        void onSuccess(String refinedTerminus);
        void onError(String message);
    }
    
    /**
     * Refine terminus name using the refineTerminus API
     */
    private static void refineTerminusAndComplete(String lineNumber, String apiDestination, 
                                                   TerminusCallback callback) {
        Map<String, String> request = new HashMap<>();
        request.put("line_number", lineNumber);
        request.put("api_destination", apiDestination);
        
        ApiClient.getApiService().refineTerminus(request).enqueue(
            new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, 
                                       Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            Gson gson = new Gson();
                            String json = gson.toJson(response.body());
                            RefinedTerminusResponse resp = gson.fromJson(json, 
                                RefinedTerminusResponse.class);
                            
                            if (resp.getRefinedTerminus() != null) {
                                Log.d(TAG, "Refined terminus: " + resp.getRefinedTerminus());
                                callback.onSuccess(resp.getRefinedTerminus());
                                return;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing refined terminus", e);
                        }
                    }
                    callback.onError("Failed to refine terminus");
                }
                
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "Failed to refine terminus", t);
                    callback.onError("Network error");
                }
            });
    }
    
    /**
     * Find the next valid arrival that the user can catch
     */
    public static Arrival findValidArrival(List<Arrival> arrivals, String line, 
                                           String destination, int cumulativeTravelMinutes) {
        if (arrivals == null || arrivals.isEmpty()) {
            Log.d(TAG, "findValidArrival: No arrivals provided");
            return null;
        }
        
        Log.d(TAG, "findValidArrival: Checking " + arrivals.size() + " arrivals, line=" + line + 
              ", destination=" + destination + ", cumulativeTime=" + cumulativeTravelMinutes);
        
        Arrival fallbackArrival = null;
        
        for (Arrival arrival : arrivals) {
            // Check if arrival matches line and destination
            // Use flexible line matching to handle "Yellow Line" vs "4" or "1" vs "Blue Line"
            boolean lineMatches = line == null || line.isEmpty() || 
                                  linesMatch(line, arrival.getLine());
            boolean destMatches = destination == null || destination.isEmpty() ||
                                  destination.equalsIgnoreCase(arrival.getDestination()) ||
                                  arrival.getDestination().contains(destination) ||
                                  destination.contains(arrival.getDestination());
            
            // Check if user can catch this arrival
            boolean canCatch = arrival.getMinutesUntil() >= cumulativeTravelMinutes;
            
            // Check if wait time is reasonable
            int waitTime = arrival.getMinutesUntil() - cumulativeTravelMinutes;
            boolean reasonableWait = waitTime <= MAX_WAIT_MINUTES;
            
            Log.d(TAG, "  Arrival: line=" + arrival.getLine() + ", dest=" + arrival.getDestination() + 
                  ", minutesUntil=" + arrival.getMinutesUntil() + 
                  " | lineMatches=" + lineMatches + ", destMatches=" + destMatches + 
                  ", canCatch=" + canCatch + ", reasonableWait=" + reasonableWait);
            
            // Store first catchable arrival with matching line as fallback
            if (lineMatches && canCatch && reasonableWait && fallbackArrival == null) {
                fallbackArrival = arrival;
            }
            
            if (lineMatches && destMatches && canCatch && reasonableWait) {
                Log.d(TAG, "  -> Valid arrival found with matching destination!");
                return arrival;
            }
        }
        
        // If no perfect match, use fallback (matching line but not destination)
        if (fallbackArrival != null) {
            Log.d(TAG, "  -> Using fallback arrival (line matches but destination may not)");
            return fallbackArrival;
        }
        
        Log.d(TAG, "findValidArrival: No valid arrival found");
        return null;
    }
    
    /**
     * Check if two line identifiers match, handling different formats
     * e.g., "Yellow Line" matches "4", "Blue Line" matches "1", etc.
     */
    private static boolean linesMatch(String line1, String line2) {
        if (line1 == null || line2 == null) {
            return false;
        }
        
        // Direct match
        if (line1.equalsIgnoreCase(line2)) {
            return true;
        }
        
        // Normalize line names to numbers
        String normalized1 = normalizeMetroLine(line1);
        String normalized2 = normalizeMetroLine(line2);
        
        return normalized1.equals(normalized2);
    }
    
    /**
     * Normalize metro line names to their numeric IDs
     * Blue Line / 1 -> "1"
     * Red Line / 2 -> "2"
     * Orange Line / 3 -> "3"
     * Yellow Line / 4 -> "4"
     * Green Line / 5 -> "5"
     * Purple Line / 6 -> "6"
     */
    private static String normalizeMetroLine(String line) {
        if (line == null) {
            return "";
        }
        
        String cleanLine = line.trim();
        
        // Remove "Line " prefix if present
        if (cleanLine.startsWith("Line ")) {
            cleanLine = cleanLine.substring(5).trim();
        }
        
        // Map color names to numbers
        if (cleanLine.equalsIgnoreCase("Blue Line") || cleanLine.equalsIgnoreCase("Blue")) {
            return "1";
        } else if (cleanLine.equalsIgnoreCase("Red Line") || cleanLine.equalsIgnoreCase("Red")) {
            return "2";
        } else if (cleanLine.equalsIgnoreCase("Orange Line") || cleanLine.equalsIgnoreCase("Orange")) {
            return "3";
        } else if (cleanLine.equalsIgnoreCase("Yellow Line") || cleanLine.equalsIgnoreCase("Yellow")) {
            return "4";
        } else if (cleanLine.equalsIgnoreCase("Green Line") || cleanLine.equalsIgnoreCase("Green")) {
            return "5";
        } else if (cleanLine.equalsIgnoreCase("Purple Line") || cleanLine.equalsIgnoreCase("Purple")) {
            return "6";
        }
        
        // If already a number, return as-is
        return cleanLine;
    }
}
