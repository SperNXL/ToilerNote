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

@Database(entities = {DailyRecord.class, UserPreference.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE daily_records ADD COLUMN customPlannedTime INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE daily_records ADD COLUMN customMidBreak INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE daily_records ADD COLUMN midBreakStart TEXT");
            database.execSQL("ALTER TABLE daily_records ADD COLUMN midBreakEnd TEXT");
            database.execSQL("ALTER TABLE daily_records ADD COLUMN customNightBreak INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE daily_records ADD COLUMN nightBreakStart TEXT");
            database.execSQL("ALTER TABLE daily_records ADD COLUMN nightBreakEnd TEXT");
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
                    ).addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
