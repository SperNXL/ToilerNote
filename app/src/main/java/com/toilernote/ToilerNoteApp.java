package com.toilernote;

import android.app.Application;
import android.content.SharedPreferences;

import com.toilernote.database.AppDatabase;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.utils.TimeUtils;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ToilerNoteApp extends Application {

    private static final String PREFS_NAME = "toiler_note_prefs";
    private static final String KEY_FIRST_INIT = "first_init_done";
    private static ToilerNoteApp instance;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        executorService.execute(() -> {
            UserPreference pref = AppDatabase.getInstance(this).userPreferenceDao().getPreference();
            if (pref == null) {
                pref = new UserPreference();
                AppDatabase.getInstance(this).userPreferenceDao().insert(pref);
            }
            initRestDaysIfNeeded(pref);
        });
    }

    private void initRestDaysIfNeeded(UserPreference pref) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_FIRST_INIT, false)) return;

        if (pref == null || pref.getWorkWeekDays() == null) return;

        Set<Integer> workDays = new HashSet<>();
        String[] days = pref.getWorkWeekDays().split(",");
        for (String d : days) {
            try {
                workDays.add(Integer.parseInt(d.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int daysInMonth = TimeUtils.getDaysInMonth(year, month);

        for (int day = 1; day <= daysInMonth; day++) {
            String dateStr = TimeUtils.formatDate(year, month, day);
            int dayOfWeek = TimeUtils.getDayOfWeek(dateStr);

            if (!workDays.contains(dayOfWeek)) {
                DailyRecord existing = AppDatabase.getInstance(this).dailyRecordDao().getRecordByDate(dateStr);
                if (existing == null) {
                    DailyRecord record = new DailyRecord(dateStr, "REST");
                    AppDatabase.getInstance(this).dailyRecordDao().insert(record);
                }
            }
        }

        prefs.edit().putBoolean(KEY_FIRST_INIT, true).apply();
    }

    public static ToilerNoteApp getInstance() {
        return instance;
    }

    public ExecutorService getExecutor() {
        return executorService;
    }
}
