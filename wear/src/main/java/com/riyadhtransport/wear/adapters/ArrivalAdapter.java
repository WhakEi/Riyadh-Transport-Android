package com.riyadhtransport.wear.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.wear.R;
import com.riyadhtransport.wear.models.WearArrival;
import java.util.ArrayList;
import java.util.List;

public class ArrivalAdapter extends RecyclerView.Adapter<ArrivalAdapter.ViewHolder> {
    private List<WearArrival> arrivals = new ArrayList<>();
    
    public void setArrivals(List<WearArrival> arrivals) {
        this.arrivals = arrivals;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_arrival, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WearArrival arrival = arrivals.get(position);
        holder.lineText.setText(arrival.getLine());
        holder.destinationText.setText(arrival.getDestination());
        holder.timeText.setText(arrival.getTime());
    }
    
    @Override
    public int getItemCount() {
        return arrivals.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lineText;
        TextView destinationText;
        TextView timeText;
        
        ViewHolder(View itemView) {
            super(itemView);
            lineText = itemView.findViewById(R.id.lineText);
            destinationText = itemView.findViewById(R.id.destinationText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}
