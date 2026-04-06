package com.example.deadlinedesk.ui;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AddEditDeadlineActivity extends AppCompatActivity {

    public static final String EXTRA_DEADLINE_ID = "EXTRA_DEADLINE_ID";
    public static final String EXTRA_DEADLINE_TITLE = "EXTRA_DEADLINE_TITLE";
    public static final String EXTRA_DEADLINE_MODULE = "EXTRA_DEADLINE_MODULE";
    public static final String EXTRA_DEADLINE_DUE = "EXTRA_DEADLINE_DUE";
    public static final String EXTRA_DEADLINE_PRIORITY = "EXTRA_DEADLINE_PRIORITY";
    public static final String EXTRA_DEADLINE_NOTES = "EXTRA_DEADLINE_NOTES";
    public static final String EXTRA_DEADLINE_DONE = "EXTRA_DEADLINE_DONE";
    public static final String EXTRA_DEADLINE_REMINDER_MINUTES = "EXTRA_DEADLINE_REMINDER_MINUTES";
    public static final String EXTRA_PRESELECTED_DATE_MILLIS = "EXTRA_PRESELECTED_DATE_MILLIS";

    private EditText etTitle, etModule, etNotes;
    private AutoCompleteTextView spinnerReminder;
    private MaterialButtonToggleGroup priorityGroup;
    private MaterialButtonToggleGroup timeFormatGroup;
    private CheckBox cbDone;
    private Button btnSave, btnDelete;
    private TextView tvSelectedDate, tvSelectedTime, tvToolbarTitle;
    private View containerDatePicker, containerTimePicker;

    private Calendar calendar;
    private DeadlineViewModel deadlineViewModel;
    private int deadlineId = -1;
    private boolean use24HourPicker;

    private final int[] reminderValues = {60, 180, 1440, 2880}; // minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_deadline);

        deadlineViewModel = new ViewModelProvider(this).get(DeadlineViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        etTitle = findViewById(R.id.et_title);
        etModule = findViewById(R.id.et_module);
        etNotes = findViewById(R.id.et_notes);
        priorityGroup = findViewById(R.id.group_priority);
        timeFormatGroup = findViewById(R.id.group_time_format);
        spinnerReminder = findViewById(R.id.spinner_reminder);
        cbDone = findViewById(R.id.cb_is_done);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        containerDatePicker = findViewById(R.id.container_date_picker);
        containerTimePicker = findViewById(R.id.container_time_picker);

        calendar = Calendar.getInstance();
        use24HourPicker = DateFormat.is24HourFormat(this);

        if (timeFormatGroup != null) {
            timeFormatGroup.check(use24HourPicker ? R.id.btn_time_24h : R.id.btn_time_12h);
            timeFormatGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) {
                    return;
                }
                use24HourPicker = checkedId == R.id.btn_time_24h;
                updateDateTimeTexts();
            });
        }

        if (priorityGroup != null) {
            priorityGroup.check(R.id.btn_priority_high);
        }

        String[] rmOptions = getResources().getStringArray(R.array.reminder_options);
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_option, rmOptions);
        reminderAdapter.setDropDownViewResource(R.layout.item_dropdown_option);
        spinnerReminder.setAdapter(reminderAdapter);
        spinnerReminder.setKeyListener(null);
        spinnerReminder.setText(rmOptions[0], false);

        // Check if editing or adding
        if (getIntent().hasExtra(EXTRA_DEADLINE_ID)) {
            tvToolbarTitle.setText(R.string.edit_assignment);
            deadlineId = getIntent().getIntExtra(EXTRA_DEADLINE_ID, -1);
            etTitle.setText(getIntent().getStringExtra(EXTRA_DEADLINE_TITLE));
            etModule.setText(getIntent().getStringExtra(EXTRA_DEADLINE_MODULE));
            etNotes.setText(getIntent().getStringExtra(EXTRA_DEADLINE_NOTES));
            cbDone.setChecked(getIntent().getBooleanExtra(EXTRA_DEADLINE_DONE, false));
            
            String priority = getIntent().getStringExtra(EXTRA_DEADLINE_PRIORITY);
            if (priority != null) {
                if (priorityGroup != null) {
                    if ("Low".equalsIgnoreCase(priority)) {
                        priorityGroup.check(R.id.btn_priority_low);
                    } else if ("Medium".equalsIgnoreCase(priority)) {
                        priorityGroup.check(R.id.btn_priority_medium);
                    } else {
                        priorityGroup.check(R.id.btn_priority_high);
                    }
                }
            }

            int reminderMinutes = getIntent().getIntExtra(EXTRA_DEADLINE_REMINDER_MINUTES, reminderValues[0]);
            int remSel = getReminderSelection(reminderMinutes);
            spinnerReminder.setText(rmOptions[remSel], false);

            long dueDate = getIntent().getLongExtra(EXTRA_DEADLINE_DUE, System.currentTimeMillis());
            calendar.setTimeInMillis(dueDate);
            updateDateTimeTexts();
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            tvToolbarTitle.setText(R.string.new_assignment);
            long preselectedDate = getIntent().getLongExtra(EXTRA_PRESELECTED_DATE_MILLIS, -1L);
            if (preselectedDate > 0L) {
                Calendar selectedDay = Calendar.getInstance();
                selectedDay.setTimeInMillis(preselectedDate);
                calendar.set(Calendar.YEAR, selectedDay.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, selectedDay.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay.get(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 18);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }
            updateDateTimeTexts();
        }

        containerDatePicker.setOnClickListener(v -> showDatePicker());
        containerTimePicker.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> saveDeadline());
        
        btnDelete.setOnClickListener(v -> {
            if (deadlineId != -1) {
                Deadline deadline = new Deadline();
                deadline.setId(deadlineId);
                deadlineViewModel.delete(deadline);
                Toast.makeText(this, R.string.msg_assignment_deleted, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.label_date)
            .setSelection(getUtcDateSelection())
                .setTheme(R.style.ThemeOverlay_DeadlineDesk_MaterialCalendar)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) {
                return;
            }

            Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utcCalendar.setTimeInMillis(selection);
            calendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH));
            updateDateTimeTexts();
        });

        datePicker.show(getSupportFragmentManager(), "due_date_picker");
    }

    private void showTimePicker() {
        openKeyboardTimePicker(use24HourPicker);
    }

    private void openKeyboardTimePicker(boolean use24Hour) {
        int clockFormat = use24Hour ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H;

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTitleText(R.string.label_time)
                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                .setTimeFormat(clockFormat)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .setTheme(R.style.ThemeOverlay_DeadlineDesk_MaterialTimePicker)
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
            updateDateTimeTexts();
        });

        timePicker.show(getSupportFragmentManager(), "due_time_picker");
    }

    private void updateDateTimeTexts() {
        java.text.DateFormat dateFormat = DateFormat.getMediumDateFormat(this);
        java.text.DateFormat timeFormat = new SimpleDateFormat(use24HourPicker ? "HH:mm" : "hh:mm a", Locale.getDefault());
        tvSelectedDate.setText(dateFormat.format(calendar.getTime()));
        tvSelectedTime.setText(timeFormat.format(calendar.getTime()));
    }

    private long getUtcDateSelection() {
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        utcCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        utcCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        utcCalendar.set(Calendar.HOUR_OF_DAY, 0);
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);
        return utcCalendar.getTimeInMillis();
    }

    private int getReminderSelection(int reminderMinutes) {
        for (int i = 0; i < reminderValues.length; i++) {
            if (reminderValues[i] == reminderMinutes) {
                return i;
            }
        }
        return 0;
    }

    private void saveDeadline() {
        String title = etTitle.getText().toString().trim();
        String module = etModule.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String priority = getSelectedPriority();
        
        int reminderPos = 0;
        String remText = spinnerReminder.getText().toString();
        String[] remOptions = getResources().getStringArray(R.array.reminder_options);
        for(int i=0; i<remOptions.length; i++) {
            if(remOptions[i].equals(remText)) { reminderPos = i; break; }
        }
        int reminderMinutes = reminderValues[reminderPos];

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.msg_enter_title, Toast.LENGTH_SHORT).show();
            return;
        }

        Deadline deadline = new Deadline();
        deadline.setTitle(title);
        deadline.setModule(module);
        deadline.setDueDate(calendar.getTimeInMillis());
        deadline.setPriority(priority);
        deadline.setNotes(notes);
        deadline.setDone(cbDone.isChecked());
        deadline.setReminderMinutes(reminderMinutes);

        if (deadlineId != -1) {
            deadline.setId(deadlineId);
            deadlineViewModel.update(deadline);
            Toast.makeText(this, R.string.msg_assignment_updated, Toast.LENGTH_SHORT).show();
        } else {
            deadlineViewModel.insert(deadline);
            Toast.makeText(this, R.string.msg_assignment_saved, Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private String getSelectedPriority() {
        if (priorityGroup == null) {
            return "High";
        }

        int checkedButtonId = priorityGroup.getCheckedButtonId();
        if (checkedButtonId == R.id.btn_priority_low) {
            return "Low";
        }
        if (checkedButtonId == R.id.btn_priority_medium) {
            return "Medium";
        }
        return "High";
    }
}
