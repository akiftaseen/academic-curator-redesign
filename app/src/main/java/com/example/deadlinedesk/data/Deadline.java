package com.example.deadlinedesk.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "deadlines_table")
public class Deadline {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String module;
    private long dueDate;
    private String priority;
    private String notes;
    private boolean isDone;
    private int reminderMinutes; // New field for custom reminder time

    // Zero-argument constructor
    public Deadline() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }

    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }
}
