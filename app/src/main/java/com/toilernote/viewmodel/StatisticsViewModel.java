package com.toilernote.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.toilernote.database.AppDatabase;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private final LiveData<UserPreference> userPreference;
    private Calendar currentMonth;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        userPreference = AppDatabase.getInstance(application).userPreferenceDao().getPreferenceLive();
        currentMonth = Calendar.getInstance();
    }

    public LiveData<UserPreference> getUserPreference() {
        return userPreference;
    }

    public LiveData<List<DailyRecord>> getMonthlyRecords(int year, int month) {
        String prefix = TimeUtils.getMonthPrefix(year, month);
        return AppDatabase.getInstance(getApplication()).dailyRecordDao().getRecordsByMonth(prefix);
    }

    public List<DailyRecord> getWorkRecords(List<DailyRecord> records) {
        List<DailyRecord> workRecords = new ArrayList<>();
        if (records == null) return workRecords;
        for (DailyRecord r : records) {
            if ("WORK".equals(r.getStatus())) {
                workRecords.add(r);
            }
        }
        return workRecords;
    }

    public double[] getCumulativeData(List<DailyRecord> records) {
        double[] data = new double[2];
        if (records == null) return data;
        for (DailyRecord r : records) {
            if ("WORK".equals(r.getStatus())) {
                data[0] += r.getWorkHours();
                data[1] += r.getOvertimeHours();
            }
        }
        return data;
    }

    public int getConsecutiveWorkDays(List<DailyRecord> records, int year, int month) {
        if (records == null) return 0;
        int daysInMonth = TimeUtils.getDaysInMonth(year, month);
        int maxConsecutive = 0;
        int currentConsecutive = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            String date = TimeUtils.formatDate(year, month, day);
            boolean isWork = false;
            for (DailyRecord r : records) {
                if (date.equals(r.getDate()) && "WORK".equals(r.getStatus())) {
                    isWork = true;
                    break;
                }
            }
            if (isWork) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 0;
            }
        }
        return maxConsecutive;
    }
}
