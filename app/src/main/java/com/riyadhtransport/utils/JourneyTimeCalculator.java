package com.riyadhtransport.utils;

import android.util.Log;
import com.riyadhtransport.models.Arrival;
import com.riyadhtransport.models.Route;
import com.riyadhtransport.models.RouteSegment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JourneyTimeCalculator {
    private static final String TAG = "JourneyTimeCalculator";
    
    public interface CalculationCallback {
        void onComplete(int newTotalMinutes);
        void onError(String message);
    }
    
    /**
     * Calculate the new total journey time with live arrival data
     */
    public static void calculateLiveJourneyTime(Route route, CalculationCallback callback) {
        if (route == null || route.getSegments() == null || route.getSegments().isEmpty()) {
            callback.onError("Invalid route");
            return;
        }
        
        List<RouteSegment> segments = route.getSegments();
        AtomicInteger cumulativeTravelTime = new AtomicInteger(0);
        AtomicInteger newTotalJourneyTime = new AtomicInteger(0);
        AtomicInteger processedSegments = new AtomicInteger(0);
        AtomicInteger totalSegments = new AtomicInteger(segments.size());
        
        Log.d(TAG, "Starting journey calculation for " + segments.size() + " segments");
        
        // Process each segment
        for (int i = 0; i < segments.size(); i++) {
            RouteSegment segment = segments.get(i);
            int segmentIndex = i;
            boolean isLastSegment = (i == segments.size() - 1);
            
            processSegment(segment, segmentIndex, isLastSegment, cumulativeTravelTime, 
                          newTotalJourneyTime, () -> {
                int processed = processedSegments.incrementAndGet();
                Log.d(TAG, "Processed segment " + processed + "/" + totalSegments.get());
                
                if (processed >= totalSegments.get()) {
                    int finalTime = newTotalJourneyTime.get();
                    Log.d(TAG, "Journey calculation complete: " + finalTime + " minutes");
                    callback.onComplete(finalTime);
                }
            });
        }
    }
    
    /**
     * Process a single segment
     */
    private static void processSegment(RouteSegment segment, int index, boolean isLastSegment,
                                       AtomicInteger cumulativeTravelTime,
                                       AtomicInteger newTotalJourneyTime,
                                       Runnable onComplete) {
        // Calculate segment ride time in minutes
        int segmentRideMinutes = (int) Math.ceil(segment.getDuration() / 60.0);
        
        Log.d(TAG, "Processing segment " + index + " (type: " + segment.getType() + 
              ", duration: " + segmentRideMinutes + " min)");
        
        // Handle walking segments
        if (segment.isWalking()) {
            Log.d(TAG, "Walking segment: adding " + segmentRideMinutes + " minutes to both counters");
            cumulativeTravelTime.addAndGet(segmentRideMinutes);
            newTotalJourneyTime.addAndGet(segmentRideMinutes);
            
            segment.setArrivalStatus("hidden");
            segment.setWaitMinutes(null);
            
            onComplete.run();
            return;
        }
        
        // Handle transit segments (bus/metro)
        if (segment.isBus() || segment.isMetro()) {
            segment.setArrivalStatus("checking");
            
            // Get station name
            String stationName = null;
            if (segment.getStations() != null && !segment.getStations().isEmpty()) {
                stationName = cleanStationName(segment.getStations().get(0));
            }
            
            if (stationName == null || stationName.isEmpty()) {
                Log.w(TAG, "No station name for segment " + index + ", using static time");
                useStaticTime(segment, segmentRideMinutes, cumulativeTravelTime, 
                             newTotalJourneyTime, onComplete);
                return;
            }
            
            // Get destination station for matching
            String destinationStation = null;
            if (segment.getStations() != null && segment.getStations().size() > 1) {
                destinationStation = cleanStationName(segment.getStations().get(segment.getStations().size() - 1));
            }
            
            int currentCumulativeTime = cumulativeTravelTime.get();
            
            Log.d(TAG, "Fetching live arrivals for " + stationName + 
                  " (cumulative time: " + currentCumulativeTime + " min)");
            
            // Fetch live arrivals
            LiveArrivalManager.getLiveArrivals(
                stationName, 
                segment.getType(), 
                segment.getLine(),
                destinationStation,
                new LiveArrivalManager.ArrivalCallback() {
                    @Override
                    public void onSuccess(List<Arrival> arrivals) {
                        Log.d(TAG, "Got " + arrivals.size() + " arrivals for segment " + index);
                        
                        // Find valid arrival
                        Arrival validArrival = LiveArrivalManager.findValidArrival(
                            arrivals, 
                            segment.getLine(), 
                            destinationStation,
                            currentCumulativeTime
                        );
                        
                        if (validArrival == null) {
                            Log.w(TAG, "No valid arrival found, using static time");
                            useStaticTime(segment, segmentRideMinutes, cumulativeTravelTime,
                                        newTotalJourneyTime, onComplete);
                        } else {
                            Log.d(TAG, "Valid arrival found: " + validArrival.getMinutesUntil() + 
                                  " min until departure");
                            
                            // Calculate wait time
                            int waitMinutes = validArrival.getMinutesUntil() - currentCumulativeTime;
                            
                            // Update segment with live data
                            segment.setWaitMinutes(waitMinutes);
                            segment.setNextArrivalMinutes(validArrival.getMinutesUntil());
                            segment.setRefinedTerminus(validArrival.getDestination());
                            
                            // Collect upcoming arrivals for display
                            List<Integer> upcomingArrivals = new ArrayList<>();
                            for (Arrival arr : arrivals) {
                                if (arr.getMinutesUntil() >= currentCumulativeTime) {
                                    upcomingArrivals.add(arr.getMinutesUntil());
                                    if (upcomingArrivals.size() >= 3) break;
                                }
                            }
                            segment.setUpcomingArrivals(upcomingArrivals);
                            
                            // Determine status based on arrival time
                            if (validArrival.getMinutesUntil() >= 59) {
                                segment.setArrivalStatus("normal");
                            } else {
                                segment.setArrivalStatus("live");
                            }
                            
                            // Add wait time + ride time to new total
                            newTotalJourneyTime.addAndGet(waitMinutes + segmentRideMinutes);
                            
                            // Add only ride time to cumulative (user boards after waiting)
                            cumulativeTravelTime.addAndGet(segmentRideMinutes);
                            
                            Log.d(TAG, "Segment " + index + " complete: wait=" + waitMinutes + 
                                  " min, ride=" + segmentRideMinutes + " min");
                            
                            onComplete.run();
                        }
                    }
                    
                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Error getting arrivals: " + message);
                        useStaticTime(segment, segmentRideMinutes, cumulativeTravelTime,
                                    newTotalJourneyTime, onComplete);
                    }
                });
        } else {
            // Unknown segment type, use static time
            useStaticTime(segment, segmentRideMinutes, cumulativeTravelTime,
                         newTotalJourneyTime, onComplete);
        }
    }
    
    /**
     * Use static time when live data is unavailable or connection is missed
     */
    private static void useStaticTime(RouteSegment segment, int segmentRideMinutes,
                                      AtomicInteger cumulativeTravelTime,
                                      AtomicInteger newTotalJourneyTime,
                                      Runnable onComplete) {
        Log.d(TAG, "Using static time: " + segmentRideMinutes + " minutes");
        
        segment.setArrivalStatus("hidden");
        segment.setWaitMinutes(null);
        
        cumulativeTravelTime.addAndGet(segmentRideMinutes);
        newTotalJourneyTime.addAndGet(segmentRideMinutes);
        
        onComplete.run();
    }
    
    /**
     * Clean station name by removing type suffixes like "(Bus)" or "(Metro)"
     */
    private static String cleanStationName(String stationName) {
        if (stationName == null) {
            return null;
        }
        
        // Remove common suffixes
        stationName = stationName.trim();
        if (stationName.endsWith(" (Bus)")) {
            return stationName.substring(0, stationName.length() - 6).trim();
        }
        if (stationName.endsWith(" (Metro)")) {
            return stationName.substring(0, stationName.length() - 8).trim();
        }
        
        return stationName;
    }
}
