package com.example.deadlinedesk.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingGroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int MODULE_TAG_MAX_CHARS = 16;

    private final Context context;
    private final List<Row> rows = new ArrayList<>();
    private OnItemClickListener listener;

    public UpcomingGroupedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View headerView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_due_day_header, parent, false);
            return new HeaderHolder(headerView);
        }

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadline, parent, false);
        bindPressFeedback(itemView);
        return new DeadlineHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (row.isHeader) {
            ((HeaderHolder) holder).tvHeader.setText(row.headerText);
            return;
        }

        Deadline currentDeadline = row.deadline;
        DeadlineHolder deadlineHolder = (DeadlineHolder) holder;

        deadlineHolder.tvTitle.setText(currentDeadline.getTitle());
        deadlineHolder.tvModule.setText(formatModuleTag(currentDeadline.getModule()));

        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        deadlineHolder.tvDueTime.setText(timeSdf.format(new Date(currentDeadline.getDueDate())));

        String priority = currentDeadline.getPriority();
        if (priority == null) {
            priority = "High";
        }

        int priorityColor = getPriorityColor(priority);
        int priorityContainerColor = getPriorityContainerColor(priority);

        deadlineHolder.cardView.setStrokeColor(ColorStateList.valueOf(priorityColor));
        deadlineHolder.priorityIndicatorContainer.setBackgroundTintList(ColorStateList.valueOf(priorityContainerColor));
        deadlineHolder.tvPrioritySymbol.setText(getPrioritySymbol(priority));
        deadlineHolder.tvPrioritySymbol.setTextColor(priorityColor);
        deadlineHolder.tvPriorityLabel.setText(priority.toUpperCase(Locale.getDefault()));
        applyPriorityBadgeStyle(deadlineHolder.tvPriorityLabel, priority);

        deadlineHolder.cbDone.setOnCheckedChangeListener(null);
        deadlineHolder.cbDone.setChecked(currentDeadline.isDone());

        if (currentDeadline.isDone()) {
            deadlineHolder.itemView.setAlpha(1.0f);
            deadlineHolder.tvTitle.setPaintFlags(deadlineHolder.tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            deadlineHolder.tvModule.setPaintFlags(deadlineHolder.tvModule.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            deadlineHolder.tvDueTime.setPaintFlags(deadlineHolder.tvDueTime.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            deadlineHolder.itemView.setAlpha(1.0f);
            deadlineHolder.tvTitle.setPaintFlags(deadlineHolder.tvTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            deadlineHolder.tvModule.setPaintFlags(deadlineHolder.tvModule.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            deadlineHolder.tvDueTime.setPaintFlags(deadlineHolder.tvDueTime.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        }

        deadlineHolder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && buttonView.isPressed()) {
                currentDeadline.setDone(isChecked);
                listener.onItemChecked(currentDeadline);
            }
        });

        deadlineHolder.itemView.setOnClickListener(v -> {
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
        return rows.size();
    }

    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    public boolean isTaskPosition(int adapterPosition) {
        return adapterPosition >= 0
                && adapterPosition < rows.size()
                && !rows.get(adapterPosition).isHeader
                && rows.get(adapterPosition).deadline != null;
    }

    public Deadline getDeadlineAtAdapterPosition(int adapterPosition) {
        if (!isTaskPosition(adapterPosition)) {
            return null;
        }
        return rows.get(adapterPosition).deadline;
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class Row {
        final boolean isHeader;
        final String headerText;
        final Deadline deadline;

        private Row(boolean isHeader, String headerText, Deadline deadline) {
            this.isHeader = isHeader;
            this.headerText = headerText;
            this.deadline = deadline;
        }

        public static Row header(String text) {
            return new Row(true, text, null);
        }

        public static Row item(Deadline deadline) {
            return new Row(false, null, deadline);
        }
    }

    public interface OnItemClickListener {
        void onItemChecked(Deadline deadline);
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView tvHeader;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tv_due_day_header);
        }
    }

    static class DeadlineHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final TextView tvTitle;
        final TextView tvModule;
        final TextView tvDueTime;
        final TextView tvPriorityLabel;
        final TextView tvPrioritySymbol;
        final CheckBox cbDone;
        final View priorityIndicatorContainer;

        DeadlineHolder(@NonNull View itemView) {
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
}

