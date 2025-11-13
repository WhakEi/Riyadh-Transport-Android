package com.riyadhtransport.wear.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.riyadhtransport.wear.models.WearStation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom view that displays nearby stations on a compass layout
 * Stations closer to user are closer to center, farther stations are closer to edge
 */
public class CompassView extends View {
    private Paint compassPaint;
    private Paint stationPaint;
    private Paint clusterPaint;
    private Paint userPaint;
    private Paint textPaint;
    
    private List<WearStation> stations = new ArrayList<>();
    private float userDirection = 0f; // User's facing direction in degrees
    private float zoomLevel = 1.0f; // 1.0 = full view, higher = zoomed in
    private float centerX, centerY, radius;
    
    private static final float CLUSTER_THRESHOLD = 50f; // Distance in pixels to cluster stations
    private Map<WearStation, List<WearStation>> clusters = new HashMap<>();
    
    public interface StationClickListener {
        void onStationClick(WearStation station);
        void onClusterClick(List<WearStation> cluster);
    }
    
    private StationClickListener clickListener;
    
    public CompassView(Context context) {
        super(context);
        init();
    }
    
    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Compass circle paint
        compassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        compassPaint.setStyle(Paint.Style.STROKE);
        compassPaint.setStrokeWidth(4f);
        compassPaint.setColor(Color.GRAY);
        
        // Station marker paint
        stationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stationPaint.setStyle(Paint.Style.FILL);
        stationPaint.setColor(Color.parseColor("#2196F3")); // Blue for metro
        
        // Cluster marker paint
        clusterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clusterPaint.setStyle(Paint.Style.FILL);
        clusterPaint.setColor(Color.parseColor("#FFC107")); // Amber for clusters
        
        // User location paint
        userPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userPaint.setStyle(Paint.Style.FILL);
        userPaint.setColor(Color.parseColor("#4CAF50")); // Green for user
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(24f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - 40f; // Leave margin for station markers
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (centerX == 0 || centerY == 0) return;
        
        // Draw compass circles (3 rings)
        canvas.drawCircle(centerX, centerY, radius * 0.33f, compassPaint);
        canvas.drawCircle(centerX, centerY, radius * 0.66f, compassPaint);
        canvas.drawCircle(centerX, centerY, radius, compassPaint);
        
        // Draw cardinal directions
        drawCardinalDirections(canvas);
        
        // Cluster stations
        updateClusters();
        
        // Draw stations
        for (Map.Entry<WearStation, List<WearStation>> entry : clusters.entrySet()) {
            WearStation station = entry.getKey();
            List<WearStation> cluster = entry.getValue();
            
            float[] pos = calculateStationPosition(station);
            
            if (cluster.size() > 1) {
                // Draw cluster
                canvas.drawCircle(pos[0], pos[1], 20f, clusterPaint);
                textPaint.setTextSize(20f);
                canvas.drawText(String.valueOf(cluster.size()), pos[0], pos[1] + 7f, textPaint);
            } else {
                // Draw single station
                Paint paint = station.isMetro() ? stationPaint : new Paint(stationPaint);
                if (station.isBus()) {
                    paint.setColor(Color.parseColor("#FF5722")); // Red for bus
                }
                canvas.drawCircle(pos[0], pos[1], 15f, paint);
                
                // Draw station name (abbreviated)
                textPaint.setTextSize(16f);
                String name = station.getDisplayName();
                if (name.length() > 10) {
                    name = name.substring(0, 8) + "...";
                }
                canvas.drawText(name, pos[0], pos[1] + 30f, textPaint);
            }
        }
        
        // Draw user location at center with direction indicator
        drawUserLocation(canvas);
    }
    
    private void drawCardinalDirections(Canvas canvas) {
        textPaint.setTextSize(28f);
        float textRadius = radius + 25f;
        
        // Adjust for user direction rotation
        canvas.drawText("N", centerX, centerY - textRadius, textPaint);
        canvas.drawText("S", centerX, centerY + textRadius + 10f, textPaint);
        canvas.drawText("E", centerX + textRadius, centerY + 10f, textPaint);
        canvas.drawText("W", centerX - textRadius, centerY + 10f, textPaint);
    }
    
    private void drawUserLocation(Canvas canvas) {
        // Draw circle for user
        canvas.drawCircle(centerX, centerY, 12f, userPaint);
        
        // Draw direction indicator (arrow pointing forward)
        Path arrow = new Path();
        arrow.moveTo(centerX, centerY - 25f);
        arrow.lineTo(centerX - 8f, centerY - 10f);
        arrow.lineTo(centerX + 8f, centerY - 10f);
        arrow.close();
        
        canvas.save();
        canvas.rotate(userDirection, centerX, centerY);
        canvas.drawPath(arrow, userPaint);
        canvas.restore();
    }
    
    private float[] calculateStationPosition(WearStation station) {
        // Calculate position based on bearing and normalized distance
        float bearing = station.getBearing();
        float normalizedDist = station.getNormalizedDistance() / zoomLevel;
        
        // Convert bearing to radians (adjust for user direction)
        float angle = (float) Math.toRadians(bearing - userDirection);
        
        // Calculate position
        float distance = normalizedDist * radius;
        float x = centerX + distance * (float) Math.sin(angle);
        float y = centerY - distance * (float) Math.cos(angle);
        
        return new float[]{x, y};
    }
    
    private void updateClusters() {
        clusters.clear();
        List<WearStation> processed = new ArrayList<>();
        
        for (WearStation station : stations) {
            if (processed.contains(station)) continue;
            
            List<WearStation> cluster = new ArrayList<>();
            cluster.add(station);
            processed.add(station);
            
            float[] pos1 = calculateStationPosition(station);
            
            for (WearStation other : stations) {
                if (processed.contains(other)) continue;
                
                float[] pos2 = calculateStationPosition(other);
                float distance = (float) Math.sqrt(
                    Math.pow(pos2[0] - pos1[0], 2) + Math.pow(pos2[1] - pos1[1], 2)
                );
                
                if (distance < CLUSTER_THRESHOLD) {
                    cluster.add(other);
                    processed.add(other);
                }
            }
            
            clusters.put(station, cluster);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && clickListener != null) {
            float x = event.getX();
            float y = event.getY();
            
            // Check if any station/cluster was clicked
            for (Map.Entry<WearStation, List<WearStation>> entry : clusters.entrySet()) {
                float[] pos = calculateStationPosition(entry.getKey());
                float distance = (float) Math.sqrt(Math.pow(x - pos[0], 2) + Math.pow(y - pos[1], 2));
                
                if (distance < 30f) { // Touch radius
                    if (entry.getValue().size() > 1) {
                        clickListener.onClusterClick(entry.getValue());
                    } else {
                        clickListener.onStationClick(entry.getKey());
                    }
                    return true;
                }
            }
        }
        
        return super.onTouchEvent(event);
    }
    
    // Public methods
    
    public void setStations(List<WearStation> stations) {
        this.stations = new ArrayList<>(stations);
        invalidate();
    }
    
    public void setUserDirection(float direction) {
        this.userDirection = direction;
        invalidate();
    }
    
    public void setZoomLevel(float zoom) {
        this.zoomLevel = Math.max(1.0f, zoom);
        invalidate();
    }
    
    public void zoomIn() {
        setZoomLevel(zoomLevel * 1.5f);
    }
    
    public void resetZoom() {
        setZoomLevel(1.0f);
    }
    
    public void setStationClickListener(StationClickListener listener) {
        this.clickListener = listener;
    }
}
