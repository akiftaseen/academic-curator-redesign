package com.example.deadlinedesk.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        return new DeadlineHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeadlineHolder holder, int position) {
        Deadline currentDeadline = deadlines.get(position);
        holder.tvTitle.setText(currentDeadline.getTitle());
        holder.tvModule.setText(currentDeadline.getModule());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.tvDueDate.setText(sdf.format(new Date(currentDeadline.getDueDate())));
        
        holder.cbDone.setChecked(currentDeadline.isDone());
        
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && buttonView.isPressed()) {
                currentDeadline.setDone(isChecked);
                listener.onItemChecked(currentDeadline);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditDeadlineActivity.class);
            intent.putExtra("EXTRA_DEADLINE_ID", currentDeadline.getId());
            intent.putExtra("EXTRA_DEADLINE_TITLE", currentDeadline.getTitle());
            intent.putExtra("EXTRA_DEADLINE_MODULE", currentDeadline.getModule());
            intent.putExtra("EXTRA_DEADLINE_DUE", currentDeadline.getDueDate());
            intent.putExtra("EXTRA_DEADLINE_PRIORITY", currentDeadline.getPriority());
            intent.putExtra("EXTRA_DEADLINE_NOTES", currentDeadline.getNotes());
            intent.putExtra("EXTRA_DEADLINE_DONE", currentDeadline.isDone());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return deadlines.size();
    }

    public void setDeadlines(List<Deadline> deadlines) {
        this.deadlines = deadlines;
        notifyDataSetChanged();
    }

    class DeadlineHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvModule;
        private TextView tvDueDate;
        private CheckBox cbDone;

        public DeadlineHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvModule = itemView.findViewById(R.id.tv_module);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            cbDone = itemView.findViewById(R.id.cb_done);
        }
    }
    
    public interface OnItemClickListener {
        void onItemChecked(Deadline deadline);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
