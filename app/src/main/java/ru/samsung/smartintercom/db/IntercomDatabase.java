package ru.samsung.smartintercom.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CallHistory.class}, version = 1)
public abstract class IntercomDatabase extends RoomDatabase {

    public abstract CallHistoryDao callHistoryDao();

    private static IntercomDatabase _database;

    public static IntercomDatabase getDatabase(Context context) {

        if (_database == null) {
            _database = Room.databaseBuilder(context.getApplicationContext(), IntercomDatabase.class, "intercom")
                    .allowMainThreadQueries()
                    .build();

        }
        return _database;
    }
}