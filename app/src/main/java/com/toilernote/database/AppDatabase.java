package com.toilernote.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.toilernote.dao.DailyRecordDao;
import com.toilernote.dao.UserPreferenceDao;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;

@Database(entities = {DailyRecord.class, UserPreference.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE daily_records ADD COLUMN customPlannedTime INTEGER NOT NULL DEFAULT 0");
        }
    };

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
                    ).addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
