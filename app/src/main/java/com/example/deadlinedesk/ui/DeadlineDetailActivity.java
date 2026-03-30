package com.example.deadlinedesk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DeadlineDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvModule, tvDueDate, tvNotes, tvPriorityTag, tvReminderValue;
    private MaterialButton btnMarkDone;
    private ImageButton btnEdit, btnShare;
    private MaterialButton btnAddToCalendar;
    private DeadlineViewModel deadlineViewModel;
    private Deadline currentDeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deadline_detail);

        deadlineViewModel = new ViewModelProvider(this).get(DeadlineViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tv_title);
        tvModule = findViewById(R.id.tv_module);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvNotes = findViewById(R.id.tv_notes);
        tvPriorityTag = findViewById(R.id.tv_priority_tag);
        tvReminderValue = findViewById(R.id.tv_reminder_value);
        btnMarkDone = findViewById(R.id.btn_mark_done);
        btnEdit = findViewById(R.id.btn_edit);
        btnShare = findViewById(R.id.btn_share);

        btnAddToCalendar = findViewById(R.id.btn_add_to_calendar);

        loadDeadlineData();

        btnMarkDone.setOnClickListener(v -> {
            if (currentDeadline != null) {
                currentDeadline.setDone(!currentDeadline.isDone());
                deadlineViewModel.update(currentDeadline);
                updateMarkDoneButton();
                String message = getString(currentDeadline.isDone() ? R.string.msg_marked_done : R.string.msg_marked_pending);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        
        btnShare.setOnClickListener(v -> {
            if (currentDeadline != null) {
                String shareText = "Assignment: " + currentDeadline.getTitle() + "\n" +
                        "Module: " + currentDeadline.getModule() + "\n" +
                        "Due: " + tvDueDate.getText().toString();
                if (currentDeadline.getNotes() != null && !currentDeadline.getNotes().isEmpty()) {
                    shareText += "\nNotes: " + currentDeadline.getNotes();
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentDeadline.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share Assignment"));
            }
        });

        btnEdit.setOnClickListener(v -> {

            if (currentDeadline != null) {
                Intent intent = new Intent(this, AddEditDeadlineActivity.class);
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, currentDeadline.getId());
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_TITLE, currentDeadline.getTitle());
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_MODULE, currentDeadline.getModule());
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DUE, currentDeadline.getDueDate());
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_PRIORITY, currentDeadline.getPriority());
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_NOTES, currentDeadline.getNotes());
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DONE, currentDeadline.isDone());
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_REMINDER_MINUTES, currentDeadline.getReminderMinutes());
                startActivity(intent);
                finish();
            }
        });

        btnAddToCalendar.setOnClickListener(v -> {
            if (currentDeadline == null) {
                return;
            }

            Intent calendarIntent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, currentDeadline.getTitle())
                    .putExtra(CalendarContract.Events.DESCRIPTION, currentDeadline.getNotes())
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, currentDeadline.getModule())
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, currentDeadline.getDueDate())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, currentDeadline.getDueDate() + (60 * 60 * 1000L));

            if (calendarIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(calendarIntent);
            } else {
                Toast.makeText(this, R.string.msg_calendar_app_not_found, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDeadlineData() {
        Intent intent = getIntent();
        if (intent.hasExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID)) {
            currentDeadline = new Deadline();
            currentDeadline.setId(intent.getIntExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, -1));
            currentDeadline.setTitle(intent.getStringExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_TITLE));
            currentDeadline.setModule(intent.getStringExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_MODULE));
            currentDeadline.setDueDate(intent.getLongExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DUE, 0));
            currentDeadline.setPriority(intent.getStringExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_PRIORITY));
            currentDeadline.setNotes(intent.getStringExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_NOTES));
            currentDeadline.setDone(intent.getBooleanExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DONE, false));
            currentDeadline.setReminderMinutes(intent.getIntExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_REMINDER_MINUTES, 60));

            tvTitle.setText(currentDeadline.getTitle());
            tvModule.setText(currentDeadline.getModule());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault());
            tvDueDate.setText(sdf.format(new Date(currentDeadline.getDueDate())));
            
            tvNotes.setText(currentDeadline.getNotes());
            tvReminderValue.setText(getReminderLabel(currentDeadline.getReminderMinutes()));
            
            String priority = currentDeadline.getPriority();
            if (priority != null) {
                if (priority.equalsIgnoreCase("High")) {
                    tvPriorityTag.setText(R.string.priority_high);
                    tvPriorityTag.setBackgroundResource(R.drawable.bg_priority_tag_urgent);
                } else if (priority.equalsIgnoreCase("Medium")) {
                    tvPriorityTag.setText(R.string.priority_medium);
                    tvPriorityTag.setBackgroundResource(R.drawable.bg_priority_tag);
                } else {
                    tvPriorityTag.setText(R.string.priority_low);
                    tvPriorityTag.setBackgroundResource(R.drawable.bg_priority_tag);
                }
            }
            
            updateMarkDoneButton();
        }
    }

    private String getReminderLabel(int reminderMinutes) {
        if (reminderMinutes >= 1440) {
            int days = reminderMinutes / 1440;
            return getResources().getQuantityString(R.plurals.reminder_days_before, days, days);
        }

        int hours = Math.max(1, reminderMinutes / 60);
        return getResources().getQuantityString(R.plurals.reminder_hours_before, hours, hours);
    }

    private void updateMarkDoneButton() {
        if (currentDeadline.isDone()) {
            btnMarkDone.setText(R.string.mark_as_pending);
            btnMarkDone.setIconResource(R.drawable.ic_undo);
        } else {
            btnMarkDone.setText(R.string.mark_as_done);
            btnMarkDone.setIconResource(R.drawable.ic_done);
        }
    }
}
