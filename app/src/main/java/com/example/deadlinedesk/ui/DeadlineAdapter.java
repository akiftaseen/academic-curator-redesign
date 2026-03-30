package com.example.deadlinedesk.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeadlineAdapter extends RecyclerView.Adapter<DeadlineAdapter.DeadlineHolder> {

    private List<Deadline> deadlines = new ArrayList<>();
    private Context context;
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
        holder.tvModule.setText(currentDeadline.getModule());
        
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvDueTime.setText(timeSdf.format(new Date(currentDeadline.getDueDate())));

        String priority = currentDeadline.getPriority();
        if (priority == null) priority = "Normal";
        holder.tvPriorityLabel.setText(priority.toUpperCase());

        // Apply Priority Styling
        if (priority.equalsIgnoreCase("High")) {
            holder.priorityIndicatorContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.tertiary_container)));
            holder.ivPriorityIcon.setImageResource(R.drawable.ic_priority);
            holder.ivPriorityIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.tertiary)));
            holder.tvPriorityLabel.setBackgroundResource(R.drawable.bg_label_urgent);
            holder.tvPriorityLabel.setTextColor(ContextCompat.getColor(context, R.color.tertiary));
        } else if (priority.equalsIgnoreCase("Medium")) {
            holder.priorityIndicatorContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_fixed)));
            holder.ivPriorityIcon.setImageResource(R.drawable.ic_note);
            holder.ivPriorityIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
            holder.tvPriorityLabel.setBackgroundResource(R.drawable.bg_label_normal);
            holder.tvPriorityLabel.setTextColor(ContextCompat.getColor(context, R.color.outline));
        } else {
            holder.priorityIndicatorContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.surface_container_low)));
            holder.ivPriorityIcon.setImageResource(R.drawable.ic_calendar);
            holder.ivPriorityIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.on_surface_variant)));
            holder.tvPriorityLabel.setBackgroundResource(R.drawable.bg_label_normal);
            holder.tvPriorityLabel.setTextColor(ContextCompat.getColor(context, R.color.outline));
        }
        
        holder.cbDone.setChecked(currentDeadline.isDone());
        
        if (currentDeadline.isDone()) {
            holder.itemView.setAlpha(0.5f);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
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
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(140).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    class DeadlineHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvModule;
        private TextView tvDueTime;
        private TextView tvPriorityLabel;
        private CheckBox cbDone;
        private View priorityIndicatorContainer;
        private ImageView ivPriorityIcon;

        public DeadlineHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvModule = itemView.findViewById(R.id.tv_module);
            tvDueTime = itemView.findViewById(R.id.tv_due_time);
            tvPriorityLabel = itemView.findViewById(R.id.tv_priority_label);
            cbDone = itemView.findViewById(R.id.cb_done);
            priorityIndicatorContainer = itemView.findViewById(R.id.priority_indicator_container);
            ivPriorityIcon = itemView.findViewById(R.id.iv_priority_icon);
            ivPriorityIcon.setContentDescription(itemView.getContext().getString(R.string.priority_icon_desc));
        }
    }
    
    public interface OnItemClickListener {
        void onItemChecked(Deadline deadline);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
