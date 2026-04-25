package com.example.deadlinedesk.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.deadlinedesk.DeadlineDeskApp;
import com.example.deadlinedesk.MainActivity;
import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.AppDatabase;
import com.example.deadlinedesk.data.Deadline;
import com.example.deadlinedesk.data.DeadlineDao;
import com.example.deadlinedesk.ui.AddEditDeadlineActivity;
import com.example.deadlinedesk.ui.DeadlineDetailActivity;

public class ReminderReceiver extends BroadcastReceiver {
    
    private static final String ACTION_MARK_DONE = "com.example.deadlinedesk.ACTION_MARK_DONE";
    private static final String ACTION_SNOOZE = "com.example.deadlinedesk.ACTION_SNOOZE";

    @Override
    public void onReceive(Context context, Intent intent) {
        
        if (ACTION_MARK_DONE.equals(intent.getAction())) {
            int id = intent.getIntExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, -1);
            if (id != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    DeadlineDao dao = AppDatabase.getDatabase(context).deadlineDao();
                    Deadline deadline = dao.getDeadlineByIdSync(id);
                    if (deadline != null) {
                        deadline.setDone(true);
                        dao.update(deadline);
                    }
                });
            }
            NotificationManagerCompat.from(context).cancel(id);
            return;
        }

        if (ACTION_SNOOZE.equals(intent.getAction())) {
            int id = intent.getIntExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, -1);
            if (id != -1) {
                ReminderScheduler.snoozeReminder(context, intent, id);
                NotificationManagerCompat.from(context).cancel(id);
            }
            return;
        }

        int deadlineId = intent.getIntExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, -1);
        String title = intent.getStringExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_TITLE);
        String module = intent.getStringExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_MODULE);
        String priority = intent.getStringExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_PRIORITY);
        long dueDate = intent.getLongExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DUE, 0);
        String notes = intent.getStringExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_NOTES);
        int reminderMinutes = intent.getIntExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_REMINDER_MINUTES, 60);

        // Open Detail Activity when clicked
        Intent detailIntent = new Intent(context, DeadlineDetailActivity.class);
        detailIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, deadlineId);
        detailIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_TITLE, title);
        detailIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_MODULE, module);
        detailIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DUE, dueDate);
        detailIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_PRIORITY, priority);
        detailIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_NOTES, notes);
        detailIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_DONE, false);
        detailIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_REMINDER_MINUTES, reminderMinutes);
        
        PendingIntent contentIntent = PendingIntent.getActivity(context, deadlineId, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent doneIntent = new Intent(context, ReminderReceiver.class);
        doneIntent.setAction(ACTION_MARK_DONE);
        doneIntent.putExtra(AddEditDeadlineActivity.EXTRA_DEADLINE_ID, deadlineId);
        PendingIntent pDoneIntent = PendingIntent.getBroadcast(context, deadlineId, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtras(intent);
        PendingIntent pSnoozeIntent = PendingIntent.getBroadcast(context, deadlineId + 10_000, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = DeadlineDeskApp.CHANNEL_ID;
        String contentTitle = "Academic Priority Alert";
        if (priority != null && priority.equalsIgnoreCase("High")) {
            contentTitle = "URGENT: " + contentTitle;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentTitle(contentTitle)
            .setContentText((module != null ? module + ": " : "") + title + " is due in 1 hour.")
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText((module != null ? module + ": " : "") + title + " is due in 1 hour.\n"
                    + "Review your work before the final push."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.primary))
            .addAction(R.drawable.ic_done, context.getString(R.string.mark_as_done), pDoneIntent)
            .addAction(R.drawable.ic_snooze, context.getString(R.string.snooze_15_minutes), pSnoozeIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        boolean canPostNotifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        if (canPostNotifications) {
            notificationManager.notify(deadlineId, builder.build());
        }
    }
}
