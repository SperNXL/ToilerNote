package com.toilernote.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.toilernote.entity.DailyRecord;

import java.util.List;

@Dao
public interface DailyRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyRecord record);

    @Update
    void update(DailyRecord record);

    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    DailyRecord getRecordByDate(String date);

    @Query("SELECT * FROM daily_records WHERE date = :date LIMIT 1")
    LiveData<DailyRecord> getRecordByDateLive(String date);

    @Query("SELECT * FROM daily_records WHERE date LIKE :monthPrefix || '%' ORDER BY date")
    LiveData<List<DailyRecord>> getRecordsByMonth(String monthPrefix);

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    LiveData<List<DailyRecord>> getAllRecords();

    @Query("DELETE FROM daily_records WHERE date LIKE :monthPrefix || '%'")
    void deleteRecordsByMonth(String monthPrefix);

    @Query("DELETE FROM daily_records")
    void deleteAll();
}
