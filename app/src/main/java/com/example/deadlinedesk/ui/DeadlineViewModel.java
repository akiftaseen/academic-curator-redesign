package com.example.deadlinedesk.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.deadlinedesk.calendar.DeviceCalendarSync;
import com.example.deadlinedesk.data.AppDatabase;
import com.example.deadlinedesk.data.Deadline;
import com.example.deadlinedesk.data.DeadlineRepository;
import com.example.deadlinedesk.receiver.ReminderScheduler;

import java.util.List;

public class DeadlineViewModel extends AndroidViewModel {

    private DeadlineRepository repository;
    private LiveData<List<Deadline>> allDeadlines;

    public DeadlineViewModel(@NonNull Application application) {
        super(application);
        repository = new DeadlineRepository(application);
        allDeadlines = repository.getAllDeadlines();
    }

    public LiveData<List<Deadline>> getAllDeadlines() {
        return allDeadlines;
    }

    public LiveData<List<Deadline>> getUpcomingDeadlines(long now) {
        return repository.getUpcomingDeadlines(now);
    }
    
    public LiveData<List<Deadline>> getDeadlinesByDate(long start, long end) {
        return repository.getDeadlinesByDate(start, end);
    }

    public void insert(Deadline deadline) {
        repository.insert(deadline, insertedDeadline -> {
            if (!insertedDeadline.isDone()) {
                ReminderScheduler.scheduleReminder(getApplication(), insertedDeadline);
            }

            long eventId = DeviceCalendarSync.upsertDeadlineEvent(getApplication(), insertedDeadline);
            if (eventId > 0L && eventId != insertedDeadline.getCalendarEventId()) {
                insertedDeadline.setCalendarEventId(eventId);
                repository.update(insertedDeadline);
            }
        });
    }

    public void update(Deadline deadline) {
        repository.update(deadline);
        if (deadline.isDone()) {
            ReminderScheduler.cancelReminder(getApplication(), deadline.getId());
        } else {
            ReminderScheduler.scheduleReminder(getApplication(), deadline);
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Deadline freshDeadline = repository.getDeadlineByIdSync(deadline.getId());
            if (freshDeadline == null) {
                return;
            }

            long syncedEventId = DeviceCalendarSync.upsertDeadlineEvent(getApplication(), freshDeadline);
            if (syncedEventId > 0L && syncedEventId != freshDeadline.getCalendarEventId()) {
                freshDeadline.setCalendarEventId(syncedEventId);
                repository.updateSync(freshDeadline);
            }
        });
    }

    public void delete(Deadline deadline) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Deadline storedDeadline = repository.getDeadlineByIdSync(deadline.getId());
            if (storedDeadline != null && storedDeadline.getCalendarEventId() > 0L) {
                DeviceCalendarSync.deleteDeadlineEvent(getApplication(), storedDeadline.getCalendarEventId());
            } else if (deadline.getCalendarEventId() > 0L) {
                DeviceCalendarSync.deleteDeadlineEvent(getApplication(), deadline.getCalendarEventId());
            }

            repository.deleteSync(deadline);
            ReminderScheduler.cancelReminder(getApplication(), deadline.getId());
        });
    }
}
