package com.example.deadlinedesk.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeadlineAdapter extends RecyclerView.Adapter<DeadlineAdapter.DeadlineHolder> {

    private static final int MODULE_TAG_MAX_CHARS = 16;

    private List<Deadline> deadlines = new ArrayList<>();
    private final Context context;
    private OnItemClickListener listener;

    public DeadlineAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public DeadlineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadline, parent, false);
        bindPressFeedback(itemView);
        return new DeadlineHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeadlineHolder holder, int position) {
        Deadline currentDeadline = deadlines.get(position);
        holder.tvTitle.setText(currentDeadline.getTitle());
        holder.tvModule.setText(formatModuleTag(currentDeadline.getModule()));
        
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvDueTime.setText(timeSdf.format(new Date(currentDeadline.getDueDate())));

        String priority = currentDeadline.getPriority();
        if (priority == null) priority = "High";

        int priorityColor = getPriorityColor(priority);
        int priorityContainerColor = getPriorityContainerColor(priority);

        holder.cardView.setStrokeColor(ColorStateList.valueOf(priorityColor));
        holder.priorityIndicatorContainer.setBackgroundTintList(ColorStateList.valueOf(priorityContainerColor));
        holder.tvPrioritySymbol.setText(getPrioritySymbol(priority));
        holder.tvPrioritySymbol.setTextColor(priorityColor);
        holder.tvPriorityLabel.setText(priority.toUpperCase(Locale.getDefault()));
        applyPriorityBadgeStyle(holder.tvPriorityLabel, priority);

        holder.cbDone.setChecked(currentDeadline.isDone());
        
        if (currentDeadline.isDone()) {
            holder.itemView.setAlpha(1.0f);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvModule.setPaintFlags(holder.tvModule.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvDueTime.setPaintFlags(holder.tvDueTime.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvModule.setPaintFlags(holder.tvModule.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvDueTime.setPaintFlags(holder.tvDueTime.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }
        
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && buttonView.isPressed()) {
                currentDeadline.setDone(isChecked);
                listener.onItemChecked(currentDeadline);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DeadlineDetailActivity.class);
            intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, currentDeadline.getId());
            intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_TITLE, currentDeadline.getTitle());
            intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_MODULE, currentDeadline.getModule());
            intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DUE, currentDeadline.getDueDate());
            intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_PRIORITY, currentDeadline.getPriority());
            intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_NOTES, currentDeadline.getNotes());
            intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DONE, currentDeadline.isDone());
            intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_REMINDER_MINUTES, currentDeadline.getReminderMinutes());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return deadlines.size();
    }

    public Deadline getDeadlineAt(int position) {
        return deadlines.get(position);
    }

    public void setDeadlines(List<Deadline> deadlines) {
        this.deadlines = deadlines;
        notifyDataSetChanged();
    }

    private void bindPressFeedback(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.985f).scaleY(0.985f).setDuration(90).start();
                    return false;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    v.animate().scaleX(1f).scaleY(1f).setDuration(140).start();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(140).start();
                    return true;
                default:
                    return false;
            }
        });
    }

    public static class DeadlineHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvTitle;
        private final TextView tvModule;
        private final TextView tvDueTime;
        private final TextView tvPriorityLabel;
        private final TextView tvPrioritySymbol;
        private final CheckBox cbDone;
        private final View priorityIndicatorContainer;

        public DeadlineHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_deadline);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvModule = itemView.findViewById(R.id.tv_module);
            tvDueTime = itemView.findViewById(R.id.tv_due_time);
            tvPriorityLabel = itemView.findViewById(R.id.tv_priority_label);
            tvPrioritySymbol = itemView.findViewById(R.id.tv_priority_symbol);
            cbDone = itemView.findViewById(R.id.cb_done);
            priorityIndicatorContainer = itemView.findViewById(R.id.priority_indicator_container);
        }
    }

    private int getPriorityColor(String priority) {
        if (priority != null && priority.equalsIgnoreCase("Low")) {
            return ContextCompat.getColor(context, R.color.priority_low);
        }
        if (priority != null && priority.equalsIgnoreCase("Medium")) {
            return ContextCompat.getColor(context, R.color.priority_medium);
        }
        return ContextCompat.getColor(context, R.color.priority_high);
    }

    private int getPriorityContainerColor(String priority) {
        if (priority != null && priority.equalsIgnoreCase("Low")) {
            return ContextCompat.getColor(context, R.color.priority_low_container);
        }
        if (priority != null && priority.equalsIgnoreCase("Medium")) {
            return ContextCompat.getColor(context, R.color.priority_medium_container);
        }
        return ContextCompat.getColor(context, R.color.priority_high_container);
    }

    private String getPrioritySymbol(String priority) {
        if (priority != null && priority.equalsIgnoreCase("Low")) {
            return "!";
        }
        if (priority != null && priority.equalsIgnoreCase("Medium")) {
            return "!!";
        }
        return "!!!";
    }

    private void applyPriorityBadgeStyle(TextView badgeView, String priority) {
        if (priority != null && priority.equalsIgnoreCase("Low")) {
            badgeView.setBackgroundResource(R.drawable.bg_label_low);
            badgeView.setTextColor(ContextCompat.getColor(context, R.color.priority_low));
            return;
        }
        if (priority != null && priority.equalsIgnoreCase("Medium")) {
            badgeView.setBackgroundResource(R.drawable.bg_label_medium);
            badgeView.setTextColor(ContextCompat.getColor(context, R.color.priority_medium));
            return;
        }

        badgeView.setBackgroundResource(R.drawable.bg_label_high);
        badgeView.setTextColor(ContextCompat.getColor(context, R.color.priority_high));
    }

    private String formatModuleTag(String module) {
        if (module == null) {
            return "";
        }

        String normalized = module.trim();
        if (normalized.length() <= MODULE_TAG_MAX_CHARS) {
            return normalized;
        }

        return normalized.substring(0, MODULE_TAG_MAX_CHARS - 3) + "...";
    }

    public interface OnItemClickListener {
        void onItemChecked(Deadline deadline);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
