package com.example.deadlinedesk.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DeadlineDao {
    @Insert
    long insert(Deadline deadline);

    @Update
    void update(Deadline deadline);

    @Delete
    void delete(Deadline deadline);

    @Query("SELECT * FROM deadlines_table ORDER BY dueDate ASC")
    LiveData<List<Deadline>> getAllDeadlines();

    @Query("SELECT * FROM deadlines_table WHERE dueDate >= :startOfDay AND dueDate < :endOfDay ORDER BY dueDate ASC")
    LiveData<List<Deadline>> getDeadlinesByDate(long startOfDay, long endOfDay);

    @Query("SELECT * FROM deadlines_table WHERE dueDate >= :now ORDER BY dueDate ASC")
    LiveData<List<Deadline>> getUpcomingDeadlines(long now);
    
    @Query("SELECT * FROM deadlines_table WHERE id = :id LIMIT 1")
    Deadline getDeadlineByIdSync(int id);

    @Query("SELECT * FROM deadlines_table WHERE dueDate >= :now AND isDone = 0")
    List<Deadline> getActiveDeadlinesSync(long now);
}
