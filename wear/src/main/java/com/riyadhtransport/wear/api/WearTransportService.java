package com.riyadhtransport.wear.api;

import com.riyadhtransport.wear.models.WearArrival;
import com.riyadhtransport.wear.models.WearStation;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface WearTransportService {
    /**
     * Get nearby stations based on user location
     */
    @POST("nearbystations")
    Call<List<WearStation>> getNearbyStations(@Body Map<String, Object> coordinates);
    
    /**
     * Get metro arrivals for a station
     */
    @POST("metro_arrivals")
    Call<Map<String, Object>> getMetroArrivals(@Body Map<String, String> stationData);
    
    /**
     * Get bus arrivals for a station
     */
    @POST("bus_arrivals")
    Call<Map<String, Object>> getBusArrivals(@Body Map<String, String> stationData);
    
    /**
     * Get route from coordinates
     */
    @POST("route_from_coords")
    Call<Map<String, Object>> getRouteFromCoords(@Body Map<String, Object> routeData);
    
    /**
     * Get all stations
     */
    @GET("api/stations")
    Call<List<WearStation>> getAllStations();
}
