package com.riyadhtransport.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.R;
import com.riyadhtransport.models.Line;
import com.riyadhtransport.utils.LineColorHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class LineAdapter extends RecyclerView.Adapter<LineAdapter.LineViewHolder> {

    private List<Line> lines;
    private List<Line> filteredLines;
    private OnLineClickListener listener;

    public interface OnLineClickListener {
        void onLineClick(Line line);
    }

    public LineAdapter(OnLineClickListener listener) {
        this.lines = new ArrayList<>();
        this.filteredLines = new ArrayList<>();
        this.listener = listener;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
        this.filteredLines = new ArrayList<>(lines);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredLines.clear();
        if (query == null || query.isEmpty()) {
            filteredLines.addAll(lines);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Line line : lines) {
                if (line.getId().toLowerCase().contains(lowerQuery) ||
                    line.getName().toLowerCase().contains(lowerQuery)) {
                    filteredLines.add(line);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_line, parent, false);
        return new LineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        Line line = filteredLines.get(position);
        holder.bind(line, listener);
    }

    @Override
    public int getItemCount() {
        return filteredLines.size();
    }

    static class LineViewHolder extends RecyclerView.ViewHolder {
        ImageView lineIcon;
        TextView lineName;
        TextView lineType;
        View colorIndicator;
        ViewGroup arrivalTimesContainer;
        ImageView arrivalIcon;
        TextView arrivalTime1;
        TextView arrivalTime2;
        TextView arrivalTime3;
        
        private android.os.Handler animationHandler;
        private Runnable animationRunnable;
        private int currentAnimationFrame = 0;

        LineViewHolder(@NonNull View itemView) {
            super(itemView);
            lineIcon = itemView.findViewById(R.id.line_icon);
            lineName = itemView.findViewById(R.id.line_name);
            lineType = itemView.findViewById(R.id.line_type);
            colorIndicator = itemView.findViewById(R.id.color_indicator);
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

        void bind(Line line, OnLineClickListener listener) {
            // Show destination as main text if available, otherwise show line name
            if (line.getDestination() != null && !line.getDestination().isEmpty()) {
                lineName.setText(line.getDestination());
            } else {
                lineName.setText(line.getName());
            }

            if (line.isMetro()) {
                lineIcon.setImageResource(R.drawable.ic_metro);
                
                // Show route summary if available, otherwise show line name as subtitle
                if (line.getRouteSummary() != null && !line.getRouteSummary().isEmpty()) {
                    lineType.setText(line.getRouteSummary());
                } else {
                    lineType.setText(line.getName());
                }

                // Set color based on metro line
                int color = LineColorHelper.getMetroLineColor(itemView.getContext(), line.getId());
                colorIndicator.setBackgroundColor(color);
                lineIcon.setColorFilter(color);
            } else {
                lineIcon.setImageResource(R.drawable.ic_bus);
                
                // Show route summary if available, otherwise show line number as subtitle
                if (line.getRouteSummary() != null && !line.getRouteSummary().isEmpty()) {
                    lineType.setText(line.getRouteSummary());
                } else {
                    lineType.setText(line.getId());
                }

                // Set bus color
                int color = LineColorHelper.getBusLineColor(itemView.getContext());
                colorIndicator.setBackgroundColor(color);
                lineIcon.setColorFilter(color);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLineClick(line);
                }
            });
            
            // Update live arrivals
            updateLiveArrivals(line);
        }
        
        private void updateLiveArrivals(Line line) {
            String status = line.getArrivalStatus();
            
            if (status == null || "hidden".equals(status)) {
                stopAnimation();
                arrivalTimesContainer.setVisibility(View.GONE);
                return;
            }
            
            arrivalTimesContainer.setVisibility(View.VISIBLE);
            
            if ("checking".equals(status)) {
                stopAnimation();
                arrivalIcon.setImageResource(R.drawable.ic_clock);
                arrivalTime1.setText(itemView.getContext().getString(R.string.checking_live_data));
                arrivalTime2.setVisibility(View.GONE);
                arrivalTime3.setVisibility(View.GONE);
                return;
            }
            
            List<Integer> upcomingArrivals = line.getUpcomingArrivals();
            if (upcomingArrivals == null || upcomingArrivals.isEmpty()) {
                stopAnimation();
                arrivalTimesContainer.setVisibility(View.GONE);
                return;
            }
            
            if ("normal".equals(status)) {
                stopAnimation();
                arrivalIcon.setImageResource(R.drawable.ic_clock);
                
                if (!upcomingArrivals.isEmpty()) {
                    String timeText = formatAsTime(upcomingArrivals.get(0));
                    arrivalTime1.setText(timeText);
                    arrivalTime1.setTextColor(0xFF000000); // Black
                    arrivalTime1.setVisibility(View.VISIBLE);
                }
                arrivalTime2.setVisibility(View.GONE);
                arrivalTime3.setVisibility(View.GONE);
                
            } else if ("live".equals(status)) {
                startLiveAnimation();
                
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
                
                if (upcomingArrivals.size() < 2) arrivalTime2.setVisibility(View.GONE);
                if (upcomingArrivals.size() < 3) arrivalTime3.setVisibility(View.GONE);
            }
        }
        
        private void startLiveAnimation() {
            stopAnimation();
            
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
                    animationHandler.postDelayed(this, 500);
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
    }
}
