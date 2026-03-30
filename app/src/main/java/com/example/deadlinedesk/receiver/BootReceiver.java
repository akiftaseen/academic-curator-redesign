package com.example.deadlinedesk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.deadlinedesk.data.AppDatabase;
import com.example.deadlinedesk.data.Deadline;
import com.example.deadlinedesk.data.DeadlineDao;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                DeadlineDao dao = AppDatabase.getDatabase(context).deadlineDao();
                List<Deadline> activeDeadlines = dao.getActiveDeadlinesSync(System.currentTimeMillis());
                for (Deadline deadline : activeDeadlines) {
                    ReminderScheduler.scheduleReminder(context, deadline);
                }
            });
        }
    }
}
