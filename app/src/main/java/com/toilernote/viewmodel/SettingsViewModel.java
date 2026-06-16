package com.toilernote.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.toilernote.database.AppDatabase;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.repository.RecordRepository;
import com.toilernote.utils.TimeUtils;
import com.toilernote.utils.WorkHoursCalculator;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SettingsViewModel extends AndroidViewModel {

    private final RecordRepository repository;
    private final LiveData<UserPreference> userPreference;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        repository = new RecordRepository(application);
        userPreference = repository.getPreferenceLive();
    }

    public LiveData<UserPreference> getUserPreference() {
        return userPreference;
    }

    public void recalculateCurrentMonth(Runnable onComplete) {
        new Thread(() -> {
            UserPreference pref = repository.getPreference();
            if (pref == null) {
                if (onComplete != null) onComplete.run();
                return;
            }
            Calendar cal = Calendar.getInstance();
            String prefix = TimeUtils.getMonthPrefix(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
            List<DailyRecord> records = repository.getRecordsByMonthSync(prefix);
            for (DailyRecord record : records) {
                if ("REST".equals(record.getStatus())) continue;
                WorkHoursCalculator.calculate(record, pref);
                repository.updateRecordSync(record);
            }
            if (onComplete != null) onComplete.run();
        }).start();
    }

    public void savePreference(UserPreference preference) {
        new Thread(() -> {
            UserPreference old = repository.getPreference();
            repository.insertPreferenceSync(preference);
            if (shouldRecalculate(old, preference)) {
                recalculateCurrentMonth(null);
            }
        }).start();
    }

    private boolean shouldRecalculate(UserPreference old, UserPreference current) {
        if (old == null) return true;
        return !Objects.equals(old.getDefaultWorkStart(), current.getDefaultWorkStart())
                || !Objects.equals(old.getDefaultWorkEnd(), current.getDefaultWorkEnd())
                || old.isMidBreakEnabled() != current.isMidBreakEnabled()
                || !Objects.equals(old.getMidBreakStart(), current.getMidBreakStart())
                || !Objects.equals(old.getMidBreakEnd(), current.getMidBreakEnd())
                || old.isNightBreakEnabled() != current.isNightBreakEnabled()
                || !Objects.equals(old.getNightBreakStart(), current.getNightBreakStart())
                || !Objects.equals(old.getNightBreakEnd(), current.getNightBreakEnd());
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
