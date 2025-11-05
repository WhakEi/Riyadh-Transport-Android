package com.riyadhtransport.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.R;
import com.riyadhtransport.models.LineAlert;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {
    
    private List<LineAlert> alerts;
    
    public AlertAdapter() {
        this.alerts = new ArrayList<>();
    }
    
    public void setAlerts(List<LineAlert> alerts) {
        this.alerts = alerts;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        LineAlert alert = alerts.get(position);
        holder.bind(alert);
    }
    
    @Override
    public int getItemCount() {
        return alerts.size();
    }
    
    static class AlertViewHolder extends RecyclerView.ViewHolder {
        private ImageView alertIcon;
        private TextView alertTitle;
        private TextView alertMessage;
        private TextView alertCreatedAt;
        private ImageButton expandButton;
        private LinearLayout expandableContent;
        private boolean isExpanded;
        
        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            alertIcon = itemView.findViewById(R.id.alert_icon);
            alertTitle = itemView.findViewById(R.id.alert_title);
            alertMessage = itemView.findViewById(R.id.alert_message);
            alertCreatedAt = itemView.findViewById(R.id.alert_created_at);
            expandButton = itemView.findViewById(R.id.expand_button);
            expandableContent = itemView.findViewById(R.id.expandable_content);
        }
        
        void bind(LineAlert alert) {
            // Set title (without square brackets and line number)
            alertTitle.setText(alert.getDisplayTitle());
            
            // Set message
            alertMessage.setText(alert.getMessage());
            
            // Format and set created at date
            if (alert.getCreatedAt() != null && !alert.getCreatedAt().isEmpty()) {
                try {
                    // Parse ISO 8601 date from AppWrite
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                    Date date = inputFormat.parse(alert.getCreatedAt());
                    if (date != null) {
                        alertCreatedAt.setText(outputFormat.format(date));
                    } else {
                        alertCreatedAt.setText("");
                    }
                } catch (Exception e) {
                    // If parsing fails, show the raw date
                    alertCreatedAt.setText(alert.getCreatedAt());
                }
            } else {
                alertCreatedAt.setText("");
            }
            
            // Set initial expansion state
            // Line-specific alerts are expanded by default, general alerts are collapsed
            isExpanded = alert.isLineSpecific();
            updateExpandState();
            
            // Set expand/collapse button click listener
            expandButton.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                updateExpandState();
            });
        }
        
        private void updateExpandState() {
            if (isExpanded) {
                expandableContent.setVisibility(View.VISIBLE);
                expandButton.setRotation(180f); // Arrow points up
            } else {
                expandableContent.setVisibility(View.GONE);
                expandButton.setRotation(0f); // Arrow points down
            }
        }
    }
}
