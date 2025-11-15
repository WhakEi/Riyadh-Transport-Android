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

public class DestinationWithHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private List<Object> items = new ArrayList<>();
    private OnDestinationClickListener listener;

    public interface OnDestinationClickListener {
        void onDestinationClick(WearFavorite destination);
    }

    public DestinationWithHeaderAdapter(OnDestinationClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<WearFavorite> history, List<WearFavorite> favorites) {
        items.clear();

        // Add search history section
        if (!history.isEmpty()) {
            items.add("Search History");
            items.addAll(history);
        }

        // Add favorites section
        if (!favorites.isEmpty()) {
            items.add("Favorites");
            items.addAll(favorites);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_destination, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String header = (String) items.get(position);
            ((HeaderViewHolder) holder).bind(header);
        } else if (holder instanceof ItemViewHolder) {
            WearFavorite destination = (WearFavorite) items.get(position);
            ((ItemViewHolder) holder).bind(destination);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
        }

        void bind(String header) {
            headerText.setText(header);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;

        ItemViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            // Remove typeText as we're using section headers instead
        }

        void bind(WearFavorite destination) {
            nameText.setText(destination.getName());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDestinationClick(destination);
                }
            });
        }
    }
}
