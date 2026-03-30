package com.example.deadlinedesk.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.deadlinedesk.DeadlineDeskApp;
import com.example.deadlinedesk.MainActivity;
import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.AppDatabase;
import com.example.deadlinedesk.data.Deadline;
import com.example.deadlinedesk.data.DeadlineDao;

public class ReminderReceiver extends BroadcastReceiver {
    
    private static final String ACTION_MARK_DONE = "com.example.deadlinedesk.ACTION_MARK_DONE";

    @Override
    public void onReceive(Context context, Intent intent) {
        
        if (ACTION_MARK_DONE.equals(intent.getAction())) {
            int id = intent.getIntExtra("EXTRA_DEADLINE_ID", -1);
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

        int deadlineId = intent.getIntExtra("EXTRA_DEADLINE_ID", -1);
        String title = intent.getStringExtra("EXTRA_DEADLINE_TITLE");
        String module = intent.getStringExtra("EXTRA_DEADLINE_MODULE");

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, deadlineId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent doneIntent = new Intent(context, ReminderReceiver.class);
        doneIntent.setAction(ACTION_MARK_DONE);
        doneIntent.putExtra("EXTRA_DEADLINE_ID", deadlineId);
        PendingIntent pDoneIntent = PendingIntent.getBroadcast(context, deadlineId, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DeadlineDeskApp.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(module != null && !module.isEmpty() ? module + ": " + title : title)
                .setContentText("Deadline is approaching!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_edit, "Mark Done", pDoneIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(deadlineId, builder.build());
        }
    }
}
