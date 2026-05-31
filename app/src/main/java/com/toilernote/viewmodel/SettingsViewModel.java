package com.toilernote.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.toilernote.database.AppDatabase;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.utils.TimeUtils;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsViewModel extends AndroidViewModel {

    private final LiveData<UserPreference> userPreference;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        userPreference = AppDatabase.getInstance(application).userPreferenceDao().getPreferenceLive();
    }

    public LiveData<UserPreference> getUserPreference() {
        return userPreference;
    }

    public void savePreference(UserPreference preference) {
        new Thread(() -> AppDatabase.getInstance(getApplication()).userPreferenceDao().insert(preference)).start();
    }

    public void clearCurrentMonthData(int year, int month) {
        new Thread(() -> {
            String prefix = TimeUtils.getMonthPrefix(year, month);
            AppDatabase.getInstance(getApplication()).dailyRecordDao().deleteRecordsByMonth(prefix);
        }).start();
    }

    public void clearAllData() {
        new Thread(() -> AppDatabase.getInstance(getApplication()).dailyRecordDao().deleteAll()).start();
    }

    public void initRestDaysForMonth(UserPreference pref, int year, int month) {
        if (pref == null || pref.getWorkWeekDays() == null) return;
        new Thread(() -> {
            Set<Integer> workDays = new HashSet<>();
            String[] days = pref.getWorkWeekDays().split(",");
            for (String d : days) {
                try {
                    workDays.add(Integer.parseInt(d.trim()));
                } catch (NumberFormatException ignored) {
                }
            }

            int daysInMonth = TimeUtils.getDaysInMonth(year, month);
            for (int day = 1; day <= daysInMonth; day++) {
                String dateStr = TimeUtils.formatDate(year, month, day);
                int dayOfWeek = TimeUtils.getDayOfWeek(dateStr);
                int workDayIndex = dayOfWeek;

                if (!workDays.contains(workDayIndex)) {
                    DailyRecord existing = AppDatabase.getInstance(getApplication())
                            .dailyRecordDao().getRecordByDate(dateStr);
                    if (existing == null) {
                        DailyRecord record = new DailyRecord(dateStr, "REST");
                        AppDatabase.getInstance(getApplication()).dailyRecordDao().insert(record);
                    }
                }
            }
        }).start();
    }
}
