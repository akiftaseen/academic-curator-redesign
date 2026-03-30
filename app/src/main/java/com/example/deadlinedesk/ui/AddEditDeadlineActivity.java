package com.example.deadlinedesk.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditDeadlineActivity extends AppCompatActivity {

    public static final String EXTRA_DEADLINE_ID = "EXTRA_DEADLINE_ID";
    public static final String EXTRA_DEADLINE_TITLE = "EXTRA_DEADLINE_TITLE";
    public static final String EXTRA_DEADLINE_MODULE = "EXTRA_DEADLINE_MODULE";
    public static final String EXTRA_DEADLINE_DUE = "EXTRA_DEADLINE_DUE";
    public static final String EXTRA_DEADLINE_PRIORITY = "EXTRA_DEADLINE_PRIORITY";
    public static final String EXTRA_DEADLINE_NOTES = "EXTRA_DEADLINE_NOTES";
    public static final String EXTRA_DEADLINE_DONE = "EXTRA_DEADLINE_DONE";
    public static final String EXTRA_DEADLINE_REMINDER_MINUTES = "EXTRA_DEADLINE_REMINDER_MINUTES";

    private EditText etTitle, etModule, etNotes;
    private AutoCompleteTextView spinnerPriority, spinnerReminder;
    private CheckBox cbDone;
    private Button btnSave, btnDelete;
    private TextView tvSelectedDate, tvSelectedTime, tvToolbarTitle;
    private View containerDatePicker, containerTimePicker;

    private Calendar calendar;
    private DeadlineViewModel deadlineViewModel;
    private int deadlineId = -1;

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
        spinnerPriority = findViewById(R.id.spinner_priority);
        spinnerReminder = findViewById(R.id.spinner_reminder);
        cbDone = findViewById(R.id.cb_is_done);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvSelectedTime = findViewById(R.id.tv_selected_time);
        containerDatePicker = findViewById(R.id.container_date_picker);
        containerTimePicker = findViewById(R.id.container_time_picker);

        calendar = Calendar.getInstance();

        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, priorities);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setText(priorities[0], false); // Default value

        String[] rmOptions = getResources().getStringArray(R.array.reminder_options);
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, rmOptions);
        spinnerReminder.setAdapter(reminderAdapter);
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
                for (int i = 0; i < priorities.length; i++) {
                    if (priorities[i].equalsIgnoreCase(priority)) {
                        spinnerPriority.setText(priorities[i], false);
                        break;
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
        new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTimeTexts();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            .show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this,
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateDateTimeTexts();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
            .show();
    }

    private void updateDateTimeTexts() {
        SimpleDateFormat dateSdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvSelectedDate.setText(dateSdf.format(calendar.getTime()));
        tvSelectedTime.setText(timeSdf.format(calendar.getTime()));
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
        String priority = spinnerPriority.getText().toString();
        
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
}
