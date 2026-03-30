package com.example.deadlinedesk.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.deadlinedesk.data.Deadline;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, Deadline deadline) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("EXTRA_DEADLINE_ID", deadline.getId());
        intent.putExtra("EXTRA_DEADLINE_TITLE", deadline.getTitle());
        intent.putExtra("EXTRA_DEADLINE_MODULE", deadline.getModule());

        // Use the deadline ID as the request code to uniquely identify this alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                deadline.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule for 1 hour before the deadline
        long triggerTime = deadline.getDueDate() - (60 * 60 * 1000); 

        // Make sure the time hasn't passed
        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    // Fallback to non-exact alarm
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerTime, 60*1000, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // If targeting API 31+, exact alarms require SCHEDULE_EXACT_ALARM permission
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
}
