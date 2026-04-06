package com.example.deadlinedesk.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.deadlinedesk.data.Deadline;
import com.example.deadlinedesk.ui.AddEditDeadlineActivity;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, Deadline deadline) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, deadline.getId());
        intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_TITLE, deadline.getTitle());
        intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_MODULE, deadline.getModule());
        intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_PRIORITY, deadline.getPriority());
        intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DUE, deadline.getDueDate());
        intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_NOTES, deadline.getNotes());
        intent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_REMINDER_MINUTES, deadline.getReminderMinutes());

        // Use the deadline ID as the request code to uniquely identify this alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                deadline.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule based on reminderMinutes, default to 60 if not set
        int minutes = deadline.getReminderMinutes() > 0 ? deadline.getReminderMinutes() : 60;
        long triggerTime = deadline.getDueDate() - ((long) minutes * 60 * 1000); 

        // Make sure the time hasn't passed
        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    // Fallback to inexact alarm that can still run while idle.
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }

    public static void cancelReminder(Context context, int deadlineId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                deadlineId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    public static void snoozeReminder(Context context, Intent originalIntent, int deadlineId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent snoozedIntent = new Intent(originalIntent);
        snoozedIntent.setAction(null);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                deadlineId,
                snoozedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + (15 * 60 * 1000L);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
