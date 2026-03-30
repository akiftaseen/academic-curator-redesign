package com.example.deadlinedesk.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditDeadlineActivity extends AppCompatActivity {

    private EditText etTitle, etModule, etNotes;
    private Spinner spinnerPriority;
    private CheckBox cbDone;
    private Button btnPickDate, btnSave, btnDelete;
    private TextView tvSelectedDate;

    private Calendar calendar;
    private DeadlineViewModel deadlineViewModel;
    private int deadlineId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_deadline);

        deadlineViewModel = new ViewModelProvider(this).get(DeadlineViewModel.class);

        etTitle = findViewById(R.id.et_title);
        etModule = findViewById(R.id.et_module);
        etNotes = findViewById(R.id.et_notes);
        spinnerPriority = findViewById(R.id.spinner_priority);
        cbDone = findViewById(R.id.cb_is_done);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        tvSelectedDate = findViewById(R.id.tv_selected_date);

        calendar = Calendar.getInstance();

        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);

        // Check if editing or adding
        if (getIntent().hasExtra("EXTRA_DEADLINE_ID")) {
            setTitle("Edit Deadline");
            deadlineId = getIntent().getIntExtra("EXTRA_DEADLINE_ID", -1);
            etTitle.setText(getIntent().getStringExtra("EXTRA_DEADLINE_TITLE"));
            etModule.setText(getIntent().getStringExtra("EXTRA_DEADLINE_MODULE"));
            etNotes.setText(getIntent().getStringExtra("EXTRA_DEADLINE_NOTES"));
            cbDone.setChecked(getIntent().getBooleanExtra("EXTRA_DEADLINE_DONE", false));
            
            String priority = getIntent().getStringExtra("EXTRA_DEADLINE_PRIORITY");
            if (priority != null) {
                for (int i = 0; i < priorities.length; i++) {
                    if (priorities[i].equalsIgnoreCase(priority)) {
                        spinnerPriority.setSelection(i);
                        break;
                    }
                }
            }

            long dueDate = getIntent().getLongExtra("EXTRA_DEADLINE_DUE", System.currentTimeMillis());
            calendar.setTimeInMillis(dueDate);
            updateDateText();
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            setTitle("Add Deadline");
            updateDateText();
        }

        btnPickDate.setOnClickListener(v -> showDateTimePicker());

        btnSave.setOnClickListener(v -> saveDeadline());
        
        btnDelete.setOnClickListener(v -> {
            if (deadlineId != -1) {
                Deadline deadline = new Deadline();
                deadline.setId(deadlineId);
                deadlineViewModel.delete(deadline);
                Toast.makeText(this, "Deadline deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateDateText();
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveDeadline() {
        String title = etTitle.getText().toString().trim();
        String module = etModule.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String priority = spinnerPriority.getSelectedItem().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        Deadline deadline = new Deadline();
        deadline.setTitle(title);
        deadline.setModule(module);
        deadline.setDueDate(calendar.getTimeInMillis());
        deadline.setPriority(priority);
        deadline.setNotes(notes);
        deadline.setDone(cbDone.isChecked());

        if (deadlineId != -1) {
            deadline.setId(deadlineId);
            deadlineViewModel.update(deadline);
            Toast.makeText(this, "Deadline updated", Toast.LENGTH_SHORT).show();
        } else {
            deadlineViewModel.insert(deadline);
            Toast.makeText(this, "Deadline saved", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
