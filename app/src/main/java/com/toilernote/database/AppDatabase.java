package com.toilernote.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.toilernote.dao.DailyRecordDao;
import com.toilernote.dao.UserPreferenceDao;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;

@Database(entities = {DailyRecord.class, UserPreference.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract DailyRecordDao dailyRecordDao();
    public abstract UserPreferenceDao userPreferenceDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "toiler_note_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
