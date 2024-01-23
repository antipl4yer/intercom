package ru.samsung.smartintercom.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CallHistoryDao {

    @Query("select * from CallHistory order by date_stamp desc")
    List<CallHistory> getAll();

    @Insert
    void insert(CallHistory... history);

    @Delete
    void delete(CallHistory history);
}