package com.riyadhtransport.wear.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.riyadhtransport.wear.R;
import com.riyadhtransport.wear.models.RouteInstruction;
import java.util.ArrayList;
import java.util.List;

public class RouteInstructionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_INSTRUCTION = 0;
    private static final int VIEW_TYPE_SUMMARY = 1;
    
    private List<RouteInstruction> instructions = new ArrayList<>();
    private int totalDuration;
    private OnReturnClickListener returnListener;
    
    public interface OnReturnClickListener {
        void onReturnClick();
    }
    
    public RouteInstructionAdapter(OnReturnClickListener listener) {
        this.returnListener = listener;
    }
    
    public void setInstructions(List<RouteInstruction> instructions, int totalDuration) {
        this.instructions = instructions;
        this.totalDuration = totalDuration;
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemViewType(int position) {
        return position < instructions.size() ? VIEW_TYPE_INSTRUCTION : VIEW_TYPE_SUMMARY;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SUMMARY) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.slide_summary, parent, false);
            return new SummaryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.slide_instruction, parent, false);
            return new InstructionViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof InstructionViewHolder) {
            RouteInstruction instruction = instructions.get(position);
            ((InstructionViewHolder) holder).bind(instruction, position + 1);
        } else if (holder instanceof SummaryViewHolder) {
            ((SummaryViewHolder) holder).bind(totalDuration);
        }
    }
    
    @Override
    public int getItemCount() {
        return instructions.size() + 1; // +1 for summary slide
    }
    
    class InstructionViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumberText;
        TextView instructionIcon;
        TextView instructionText;
        TextView detailsText;
        TextView durationText;
        
        InstructionViewHolder(View itemView) {
            super(itemView);
            stepNumberText = itemView.findViewById(R.id.stepNumberText);
            instructionIcon = itemView.findViewById(R.id.instructionIcon);
            instructionText = itemView.findViewById(R.id.instructionText);
            detailsText = itemView.findViewById(R.id.detailsText);
            durationText = itemView.findViewById(R.id.durationText);
        }
        
        void bind(RouteInstruction instruction, int stepNumber) {
            stepNumberText.setText("Step " + stepNumber + " of " + instructions.size());
            instructionText.setText(instruction.getInstruction());
            detailsText.setText(instruction.getDetails());
            durationText.setText(instruction.getDuration() + " min");
            
            // Set icon based on type
            if (instruction.isWalk()) {
                instructionIcon.setText("🚶");
            } else if (instruction.isMetro()) {
                instructionIcon.setText("🚇");
            } else if (instruction.isBus()) {
                instructionIcon.setText("🚌");
            }
        }
    }
    
    class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView totalDurationText;
        Button btnReturnToMenu;
        
        SummaryViewHolder(View itemView) {
            super(itemView);
            totalDurationText = itemView.findViewById(R.id.totalDurationText);
            btnReturnToMenu = itemView.findViewById(R.id.btnReturnToMenu);
            
            btnReturnToMenu.setOnClickListener(v -> {
                if (returnListener != null) {
                    returnListener.onReturnClick();
                }
            });
        }
        
        void bind(int duration) {
            totalDurationText.setText(duration + " min");
        }
    }
}
