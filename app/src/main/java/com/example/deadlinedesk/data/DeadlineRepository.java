package com.example.deadlinedesk.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.Future;

public class DeadlineRepository {

    private DeadlineDao deadlineDao;
    private LiveData<List<Deadline>> allDeadlines;

    public DeadlineRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        deadlineDao = db.deadlineDao();
        allDeadlines = deadlineDao.getAllDeadlines();
    }

    public LiveData<List<Deadline>> getAllDeadlines() {
        return allDeadlines;
    }

    public LiveData<List<Deadline>> getDeadlinesByDate(long startOfDay, long endOfDay) {
        return deadlineDao.getDeadlinesByDate(startOfDay, endOfDay);
    }
    
    public LiveData<List<Deadline>> getUpcomingDeadlines(long now) {
        return deadlineDao.getUpcomingDeadlines(now);
    }

    public void insert(Deadline deadline, OnDeadlineInsertedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = deadlineDao.insert(deadline);
            if (listener != null) {
                deadline.setId((int) id);
                listener.onInserted(deadline);
            }
        });
    }

    public void update(Deadline deadline) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            deadlineDao.update(deadline);
        });
    }

    public void updateSync(Deadline deadline) {
        deadlineDao.update(deadline);
    }

    public void delete(Deadline deadline) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            deadlineDao.delete(deadline);
        });
    }

    public void deleteSync(Deadline deadline) {
        deadlineDao.delete(deadline);
    }

    public Deadline getDeadlineByIdSync(int id) {
        return deadlineDao.getDeadlineByIdSync(id);
    }

    public List<Deadline> getActiveDeadlinesSync(long now) {
        return deadlineDao.getActiveDeadlinesSync(now);
    }

    public interface OnDeadlineInsertedListener {
        void onInserted(Deadline deadline);
    }
}
