package com.riyadhtransport.wear.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.wear.R;
import com.riyadhtransport.wear.models.WearStation;
import java.util.ArrayList;
import java.util.List;

public class ClusterStationAdapter extends RecyclerView.Adapter<ClusterStationAdapter.ViewHolder> {
    private List<WearStation> stations = new ArrayList<>();
    private OnStationClickListener listener;
    
    public interface OnStationClickListener {
        void onStationClick(WearStation station);
    }
    
    public ClusterStationAdapter(OnStationClickListener listener) {
        this.listener = listener;
    }
    
    public void setStations(List<WearStation> stations) {
        this.stations = stations;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cluster_station, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WearStation station = stations.get(position);
        holder.bind(station);
    }
    
    @Override
    public int getItemCount() {
        return stations.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView stationNameText;
        TextView stationTypeText;
        
        ViewHolder(View itemView) {
            super(itemView);
            stationNameText = itemView.findViewById(R.id.stationNameText);
            stationTypeText = itemView.findViewById(R.id.stationTypeText);
        }
        
        void bind(WearStation station) {
            stationNameText.setText(station.getDisplayName());
            stationTypeText.setText(station.getType());
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStationClick(station);
                }
            });
        }
    }
}
