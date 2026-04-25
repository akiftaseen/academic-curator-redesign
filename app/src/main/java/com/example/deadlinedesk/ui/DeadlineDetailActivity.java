package com.example.deadlinedesk.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.calendar.DeviceCalendarSync;
import com.example.deadlinedesk.data.Deadline;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class DeadlineDetailActivity extends AppCompatActivity {

    private static final int REQ_CALENDAR_PERMISSIONS = 2001;

    private TextView tvTitle, tvModule, tvNotes, tvPriorityTag, tvReminderValue, tvDeadlineDateValue, tvDeadlineTimeValue;
    private MaterialButton btnMarkDone;
    private DeadlineViewModel deadlineViewModel;
    private Deadline currentDeadline;
    private boolean pendingCalendarInsert;

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
        tvNotes = findViewById(R.id.tv_notes);
        tvPriorityTag = findViewById(R.id.tv_priority_tag);
        tvReminderValue = findViewById(R.id.tv_reminder_value);
        tvDeadlineDateValue = findViewById(R.id.tv_deadline_date_value);
        tvDeadlineTimeValue = findViewById(R.id.tv_deadline_time_value);
        btnMarkDone = findViewById(R.id.btn_mark_done);
        ImageButton btnEdit = findViewById(R.id.btn_edit);
        ImageButton btnShare = findViewById(R.id.btn_share);
        MaterialButton btnAddToCalendar = findViewById(R.id.btn_add_to_calendar);

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
                        "Due: " + getFormattedDueDate(currentDeadline.getDueDate());
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
                intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_CALENDAR_EVENT_ID, currentDeadline.getCalendarEventId());
                startActivity(intent);
                finish();
            }
        });

        btnAddToCalendar.setOnClickListener(v -> {
            if (currentDeadline == null) {
                return;
            }

            if (hasCalendarPermissions() && syncDeadlineWithCalendar()) {
                return;
            }

            if (!hasCalendarPermissions()) {
                pendingCalendarInsert = true;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                        REQ_CALENDAR_PERMISSIONS);
                return;
            }

            launchCalendarInsertIntent();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CALENDAR_PERMISSIONS) {
            boolean granted = grantResults.length >= 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (granted && pendingCalendarInsert && insertIntoDeviceCalendar()) {
                pendingCalendarInsert = false;
                return;
            }

            pendingCalendarInsert = false;
            launchCalendarInsertIntent();
        }
    }

    private boolean hasCalendarPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean insertIntoDeviceCalendar() {
        return syncDeadlineWithCalendar();
    }

    private boolean syncDeadlineWithCalendar() {
        if (currentDeadline == null) {
            return false;
        }

        long syncedEventId = DeviceCalendarSync.upsertDeadlineEvent(this, currentDeadline);
        if (syncedEventId > 0L) {
            boolean isFirstSync = currentDeadline.getCalendarEventId() <= 0L;
            currentDeadline.setCalendarEventId(syncedEventId);
            deadlineViewModel.update(currentDeadline);
            Toast.makeText(this, getString(isFirstSync ? R.string.msg_calendar_added : R.string.msg_calendar_synced), Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void launchCalendarInsertIntent() {
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

        try {
            startActivity(calendarIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.msg_calendar_app_not_found, Toast.LENGTH_SHORT).show();
        }
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
            currentDeadline.setCalendarEventId(intent.getLongExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_CALENDAR_EVENT_ID, -1L));

            tvTitle.setText(currentDeadline.getTitle());
            tvModule.setText(currentDeadline.getModule());
            
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault());
            SimpleDateFormat timeOnlyFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            tvDeadlineDateValue.setText(dateOnlyFormat.format(new Date(currentDeadline.getDueDate())));
            tvDeadlineTimeValue.setText(timeOnlyFormat.format(new Date(currentDeadline.getDueDate())));

            tvNotes.setText(currentDeadline.getNotes());
            tvReminderValue.setText(getReminderLabel(currentDeadline.getReminderMinutes()));
            
            String priority = currentDeadline.getPriority();
            if (priority != null && priority.equalsIgnoreCase("Low")) {
                tvPriorityTag.setText(R.string.priority_low);
                tvPriorityTag.setBackgroundResource(R.drawable.bg_priority_tag_low);
                tvPriorityTag.setTextColor(ContextCompat.getColor(this, R.color.priority_low));
            } else if (priority != null && priority.equalsIgnoreCase("Medium")) {
                tvPriorityTag.setText(R.string.priority_medium);
                tvPriorityTag.setBackgroundResource(R.drawable.bg_priority_tag_medium);
                tvPriorityTag.setTextColor(ContextCompat.getColor(this, R.color.priority_medium));
            } else {
                tvPriorityTag.setText(R.string.priority_high);
                tvPriorityTag.setBackgroundResource(R.drawable.bg_priority_tag_high);
                tvPriorityTag.setTextColor(ContextCompat.getColor(this, R.color.priority_high));
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

    private String getFormattedDueDate(long dueDateMillis) {
        SimpleDateFormat dueDateFormat = new SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault());
        return dueDateFormat.format(new Date(dueDateMillis));
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
