package com.riyadhtransport.wear.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.CurvedTextView;
import com.riyadhtransport.wear.R;
import com.riyadhtransport.wear.models.RouteInstruction;
import com.riyadhtransport.wear.utils.ScreenUtils;
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
        CurvedTextView stepNumberText;
        ImageView instructionIcon;
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
            
            // Scale content based on screen size
            scaleContent();
        }
        
        private void scaleContent() {
            // Get scaled sizes
            float stepTextSize = ScreenUtils.getScaledTextSize(itemView.getContext(), 11f);
            float instructionTextSize = ScreenUtils.getScaledTextSize(itemView.getContext(), 14f);
            float detailsTextSize = ScreenUtils.getScaledTextSize(itemView.getContext(), 11f);
            float durationTextSize = ScreenUtils.getScaledTextSize(itemView.getContext(), 16f);
            int iconSize = ScreenUtils.getScaledDimension(itemView.getContext(), 36);
            
            // Apply scaled sizes
            stepNumberText.setTextSize(stepTextSize);
            instructionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, instructionTextSize);
            detailsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, detailsTextSize);
            durationText.setTextSize(TypedValue.COMPLEX_UNIT_SP, durationTextSize);
            
            // Scale icon
            ViewGroup.LayoutParams iconParams = instructionIcon.getLayoutParams();
            iconParams.width = ScreenUtils.dpToPx(itemView.getContext(), iconSize);
            iconParams.height = ScreenUtils.dpToPx(itemView.getContext(), iconSize);
            instructionIcon.setLayoutParams(iconParams);
        }
        
        void bind(RouteInstruction instruction, int stepNumber) {
            stepNumberText.setText("Step " + stepNumber + " of " + instructions.size());
            instructionText.setText(instruction.getInstruction());
            detailsText.setText(instruction.getDetails());
            durationText.setText(instruction.getDuration() + " min");
            
            // Set Material You icon based on type
            if (instruction.isWalk()) {
                instructionIcon.setImageResource(R.drawable.ic_walk);
                instructionIcon.setVisibility(View.VISIBLE);
            } else if (instruction.isMetro()) {
                instructionIcon.setImageResource(R.drawable.ic_metro);
                instructionIcon.setVisibility(View.VISIBLE);
            } else if (instruction.isBus()) {
                instructionIcon.setImageResource(R.drawable.ic_bus);
                instructionIcon.setVisibility(View.VISIBLE);
            } else {
                instructionIcon.setVisibility(View.GONE);
            }
        }
    }
    
    class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView totalDurationText;
        TextView transitsText;
        TextView walkingTimeText;
        Button btnReturnToMenu;
        
        SummaryViewHolder(View itemView) {
            super(itemView);
            totalDurationText = itemView.findViewById(R.id.totalDurationText);
            transitsText = itemView.findViewById(R.id.transitsText);
            walkingTimeText = itemView.findViewById(R.id.walkingTimeText);
            btnReturnToMenu = itemView.findViewById(R.id.btnReturnToMenu);
            
            // Scale content based on screen size
            scaleContent();
            
            btnReturnToMenu.setOnClickListener(v -> {
                if (returnListener != null) {
                    returnListener.onReturnClick();
                }
            });
        }
        
        private void scaleContent() {
            // Get scaled sizes
            float titleSize = ScreenUtils.getScaledTextSize(itemView.getContext(), 16f);
            float durationSize = ScreenUtils.getScaledTextSize(itemView.getContext(), 28f);
            float infoSize = ScreenUtils.getScaledTextSize(itemView.getContext(), 12f);
            float buttonTextSize = ScreenUtils.getScaledTextSize(itemView.getContext(), 12f);
            int buttonHeight = ScreenUtils.getScaledDimension(itemView.getContext(), 40);
            
            // Apply scaled sizes
            TextView titleText = itemView.findViewById(R.id.summaryTitle);
            titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize);
            totalDurationText.setTextSize(TypedValue.COMPLEX_UNIT_SP, durationSize);
            transitsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, infoSize);
            walkingTimeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, infoSize);
            btnReturnToMenu.setTextSize(TypedValue.COMPLEX_UNIT_SP, buttonTextSize);
            
            // Scale button height
            ViewGroup.LayoutParams btnParams = btnReturnToMenu.getLayoutParams();
            btnParams.height = ScreenUtils.dpToPx(itemView.getContext(), buttonHeight);
            btnReturnToMenu.setLayoutParams(btnParams);
        }
        
        void bind(int duration) {
            totalDurationText.setText(duration + " min");
            
            // Calculate transit count and walking time
            int transitCount = 0;
            int walkingTime = 0;
            for (RouteInstruction instruction : instructions) {
                if (instruction.isWalk()) {
                    walkingTime += instruction.getDuration();
                } else {
                    transitCount++;
                }
            }
            
            transitsText.setText(transitCount + " transit" + (transitCount != 1 ? "s" : ""));
            walkingTimeText.setText(walkingTime + " min walking");
        }
    }
}
