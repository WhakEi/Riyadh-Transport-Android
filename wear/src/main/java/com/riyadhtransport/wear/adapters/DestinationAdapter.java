package com.riyadhtransport.wear.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.wear.R;
import com.riyadhtransport.wear.models.WearFavorite;
import java.util.ArrayList;
import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {
    private List<WearFavorite> destinations = new ArrayList<>();
    private OnDestinationClickListener listener;
    
    public interface OnDestinationClickListener {
        void onDestinationClick(WearFavorite destination);
    }
    
    public DestinationAdapter(OnDestinationClickListener listener) {
        this.listener = listener;
    }
    
    public void setDestinations(List<WearFavorite> destinations) {
        this.destinations = destinations;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WearFavorite destination = destinations.get(position);
        holder.nameText.setText(destination.getName());
        holder.typeText.setText(destination.getType());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDestinationClick(destination);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return destinations.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView typeText;
        
        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            typeText = itemView.findViewById(R.id.typeText);
        }
    }
}
