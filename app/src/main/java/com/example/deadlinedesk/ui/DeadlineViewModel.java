package com.example.deadlinedesk.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

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
        });
    }

    public void update(Deadline deadline) {
        repository.update(deadline);
        if (deadline.isDone()) {
            ReminderScheduler.cancelReminder(getApplication(), deadline.getId());
        } else {
            ReminderScheduler.scheduleReminder(getApplication(), deadline);
        }
    }

    public void delete(Deadline deadline) {
        repository.delete(deadline);
        ReminderScheduler.cancelReminder(getApplication(), deadline.getId());
    }
}
