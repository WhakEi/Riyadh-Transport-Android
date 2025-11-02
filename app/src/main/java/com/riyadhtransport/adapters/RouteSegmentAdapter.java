package com.riyadhtransport.adapters;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.R;
import com.riyadhtransport.models.RouteSegment;
import com.riyadhtransport.utils.LineColorHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class RouteSegmentAdapter extends RecyclerView.Adapter<RouteSegmentAdapter.SegmentViewHolder> {

    private List<RouteSegment> segments;

    public RouteSegmentAdapter() {
        this.segments = new ArrayList<>();
    }

    public void setSegments(List<RouteSegment> segments) {
        this.segments = segments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_segment, parent, false);
        return new SegmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SegmentViewHolder holder, int position) {
        RouteSegment segment = segments.get(position);
        boolean isLastSegment = (position == segments.size() - 1);
        holder.bind(segment, isLastSegment);
    }

    @Override
    public int getItemCount() {
        return segments.size();
    }

    static class SegmentViewHolder extends RecyclerView.ViewHolder {
        ImageView segmentIcon;
        TextView segmentType;
        TextView segmentDuration;
        TextView segmentDetails;
        ViewGroup arrivalTimesContainer;
        ImageView arrivalIcon;
        TextView arrivalTime1;
        TextView arrivalTime2;
        TextView arrivalTime3;
        
        private android.os.Handler animationHandler;
        private Runnable animationRunnable;
        private int currentAnimationFrame = 0;

        SegmentViewHolder(@NonNull View itemView) {
            super(itemView);
            segmentIcon = itemView.findViewById(R.id.segment_icon);
            segmentType = itemView.findViewById(R.id.segment_type);
            segmentDuration = itemView.findViewById(R.id.segment_duration);
            segmentDetails = itemView.findViewById(R.id.segment_details);
            arrivalTimesContainer = itemView.findViewById(R.id.arrival_times_container);
            arrivalIcon = itemView.findViewById(R.id.arrival_icon);
            arrivalTime1 = itemView.findViewById(R.id.arrival_time_1);
            arrivalTime2 = itemView.findViewById(R.id.arrival_time_2);
            arrivalTime3 = itemView.findViewById(R.id.arrival_time_3);
            
            animationHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        
        void stopAnimation() {
            if (animationRunnable != null) {
                animationHandler.removeCallbacks(animationRunnable);
            }
        }

        void bind(RouteSegment segment, boolean isLastSegment) {
            String typeText;
            String detailsText;
            int iconRes;
            int color;

            if (segment.isWalking()) {
                // Walking segment
                iconRes = R.drawable.ic_walk;
                color = LineColorHelper.getWalkColor(itemView.getContext());

                // Get destination
                String destination = getDestinationName(segment, isLastSegment);

                if (isLastSegment) {
                    typeText = itemView.getContext().getString(R.string.walk_to_destination);
                } else {
                    // Determine if destination is a bus stop or metro station based on next segment
                    String stationType = itemView.getContext().getString(R.string.metro_station); // default
                    typeText = itemView.getContext().getString(R.string.walk_to_station,
                            destination, stationType);
                }

                double distanceKm = segment.getDistance() != null ? segment.getDistance() / 1000.0 : 0;
                detailsText = String.format("%.2f %s", distanceKm,
                        itemView.getContext().getString(R.string.kilometers));

            } else if (segment.isMetro()) {
                // Metro segment
                iconRes = R.drawable.ic_metro;
                String lineName = LineColorHelper.getMetroLineName(
                        itemView.getContext(), segment.getLine());
                color = LineColorHelper.getMetroLineColor(itemView.getContext(), segment.getLine());

                String destination = getDestinationStation(segment);
                typeText = itemView.getContext().getString(R.string.take_metro,
                        lineName, destination);
                detailsText = destination + " (" + itemView.getContext().getString(R.string.metro_station) + ")";

            } else {
                // Bus segment
                iconRes = R.drawable.ic_bus;
                color = LineColorHelper.getBusLineColor(itemView.getContext());

                String startStation = getStartStation(segment);
                String destination = getDestinationStation(segment);

                typeText = itemView.getContext().getString(R.string.take_bus,
                        segment.getLine(), destination);
                detailsText = startStation + " (" + itemView.getContext().getString(R.string.bus_stop) + ") → " + destination;
            }

            // Set icon and color
            segmentIcon.setImageResource(iconRes);
            segmentIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            // Set text with color
            segmentType.setText(typeText);
            segmentType.setTextColor(color);
            segmentDetails.setText(detailsText);

            // Set duration
            int minutes = (int) Math.ceil(segment.getDuration() / 60.0);
            segmentDuration.setText(minutes + " " + itemView.getContext().getString(R.string.minutes));
            
            // Handle live arrival times for transit segments
            if (segment.isBus() || segment.isMetro()) {
                updateLiveArrivals(segment);
            } else {
                stopAnimation();
                arrivalTimesContainer.setVisibility(View.GONE);
            }
            
            // Update segment type text if terminus is available
            if (segment.getRefinedTerminus() != null && !segment.getRefinedTerminus().isEmpty()) {
                String destination = getDestinationStation(segment);
                if (segment.isBus()) {
                    typeText = itemView.getContext().getString(R.string.take_bus_towards,
                            segment.getLine(), segment.getRefinedTerminus(), destination);
                } else if (segment.isMetro()) {
                    String lineName = LineColorHelper.getMetroLineName(
                            itemView.getContext(), segment.getLine());
                    typeText = itemView.getContext().getString(R.string.take_metro_towards,
                            lineName, segment.getRefinedTerminus(), destination);
                }
                segmentType.setText(typeText);
            }
        }
        
        private void updateLiveArrivals(RouteSegment segment) {
            String status = segment.getArrivalStatus();
            
            if (status == null || "hidden".equals(status)) {
                stopAnimation();
                arrivalTimesContainer.setVisibility(View.GONE);
                return;
            }
            
            arrivalTimesContainer.setVisibility(View.VISIBLE);
            
            if ("checking".equals(status)) {
                // Show checking status
                stopAnimation();
                arrivalIcon.setImageResource(R.drawable.ic_clock);
                arrivalTime1.setText(itemView.getContext().getString(R.string.checking_live_data));
                arrivalTime2.setVisibility(View.GONE);
                arrivalTime3.setVisibility(View.GONE);
                return;
            }
            
            List<Integer> upcomingArrivals = segment.getUpcomingArrivals();
            if (upcomingArrivals == null || upcomingArrivals.isEmpty()) {
                stopAnimation();
                arrivalTimesContainer.setVisibility(View.GONE);
                return;
            }
            
            if ("normal".equals(status)) {
                // Show as normal time (>59 minutes) with clock icon, black text
                stopAnimation();
                arrivalIcon.setImageResource(R.drawable.ic_clock);
                
                // Format as time (e.g., "9:59 PM")
                if (!upcomingArrivals.isEmpty()) {
                    String timeText = formatAsTime(upcomingArrivals.get(0));
                    arrivalTime1.setText(timeText);
                    arrivalTime1.setTextColor(0xFF000000); // Black
                    arrivalTime1.setVisibility(View.VISIBLE);
                }
                arrivalTime2.setVisibility(View.GONE);
                arrivalTime3.setVisibility(View.GONE);
                
            } else if ("live".equals(status)) {
                // Show live arrivals with animation
                startLiveAnimation();
                
                // Display up to 3 arrival times
                for (int i = 0; i < upcomingArrivals.size() && i < 3; i++) {
                    int minutes = upcomingArrivals.get(i);
                    String timeText;
                    
                    if (minutes == 0) {
                        timeText = itemView.getContext().getString(R.string.arriving_now);
                    } else {
                        timeText = itemView.getContext().getString(R.string.arriving_in_minutes, minutes);
                    }
                    
                    TextView timeView = i == 0 ? arrivalTime1 : (i == 1 ? arrivalTime2 : arrivalTime3);
                    timeView.setText(timeText);
                    timeView.setTextColor(0xFF4CAF50); // Green
                    timeView.setVisibility(View.VISIBLE);
                }
                
                // Hide unused time views
                if (upcomingArrivals.size() < 2) arrivalTime2.setVisibility(View.GONE);
                if (upcomingArrivals.size() < 3) arrivalTime3.setVisibility(View.GONE);
            }
        }
        
        private void startLiveAnimation() {
            stopAnimation();
            
            // Determine if RTL (Arabic)
            boolean isRtl = itemView.getContext().getResources().getConfiguration()
                    .getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
            
            final int[] frames = isRtl ? 
                new int[]{R.drawable.lr3, R.drawable.lr2, R.drawable.lr1} :
                new int[]{R.drawable.lt3, R.drawable.lt2, R.drawable.lt1};
            
            animationRunnable = new Runnable() {
                @Override
                public void run() {
                    arrivalIcon.setImageResource(frames[currentAnimationFrame]);
                    currentAnimationFrame = (currentAnimationFrame + 1) % 3;
                    animationHandler.postDelayed(this, 500); // 0.5 seconds
                }
            };
            
            animationHandler.post(animationRunnable);
        }
        
        private String formatAsTime(int minutesFromNow) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.MINUTE, minutesFromNow);
            
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("h:mm a", 
                    java.util.Locale.getDefault());
            return timeFormat.format(cal.getTime());
        }

        private String getDestinationName(RouteSegment segment, boolean isLastSegment) {
            if (isLastSegment) {
                return itemView.getContext().getString(R.string.your_destination);
            }

            List<String> stations = segment.getStations();
            if (stations != null && !stations.isEmpty()) {
                return stations.get(stations.size() - 1);
            }

            return itemView.getContext().getString(R.string.next_stop);
        }

        private String getStartStation(RouteSegment segment) {
            List<String> stations = segment.getStations();
            if (stations != null && !stations.isEmpty()) {
                return stations.get(0);
            }
            return itemView.getContext().getString(R.string.current_location);
        }

        private String getDestinationStation(RouteSegment segment) {
            List<String> stations = segment.getStations();
            if (stations != null && !stations.isEmpty()) {
                return stations.get(stations.size() - 1);
            }
            return itemView.getContext().getString(R.string.destination);
        }
    }
}
