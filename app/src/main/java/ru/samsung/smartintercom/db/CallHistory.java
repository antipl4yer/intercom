package ru.samsung.smartintercom.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CallHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "date_stamp")
    public long dateStamp;
}