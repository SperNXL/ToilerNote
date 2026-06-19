package com.toilernote.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.toilernote.database.AppDatabase;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.model.MonthStatistics;
import com.toilernote.repository.RecordRepository;
import com.toilernote.utils.TimeUtils;
import com.toilernote.utils.WorkHoursCalculator;

import java.util.Calendar;
import java.util.List;

public class CalendarViewModel extends AndroidViewModel {

    private final RecordRepository repository;
    private final Calendar currentMonth;
    private final MutableLiveData<Calendar> monthLiveData;
    private final LiveData<List<DailyRecord>> monthlyRecords;
    private final LiveData<UserPreference> userPreference;
    private final MutableLiveData<MonthStatistics> statistics;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        repository = new RecordRepository(application);
        currentMonth = Calendar.getInstance();
        monthLiveData = new MutableLiveData<>();
        monthLiveData.setValue(currentMonth);

        monthlyRecords = Transformations.switchMap(monthLiveData, cal -> {
            String prefix = TimeUtils.getMonthPrefix(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
            return AppDatabase.getInstance(application).dailyRecordDao().getRecordsByMonth(prefix);
        });

        userPreference = AppDatabase.getInstance(application).userPreferenceDao().getPreferenceLive();
        statistics = new MutableLiveData<>();
    }

    public LiveData<Calendar> getCurrentMonth() {
        return monthLiveData;
    }

    public LiveData<List<DailyRecord>> getMonthlyRecords() {
        return monthlyRecords;
    }

    public LiveData<UserPreference> getUserPreference() {
        return userPreference;
    }

    public LiveData<MonthStatistics> getStatistics() {
        return statistics;
    }

    public void previousMonth() {
        currentMonth.add(Calendar.MONTH, -1);
        monthLiveData.setValue((Calendar) currentMonth.clone());
    }

    public void nextMonth() {
        currentMonth.add(Calendar.MONTH, 1);
        monthLiveData.setValue((Calendar) currentMonth.clone());
    }

    public void saveRecord(DailyRecord record) {
        repository.insertRecord(record);
    }

    public void calculateStatistics(List<DailyRecord> records, UserPreference pref) {
        if (records == null || pref == null) return;
        MonthStatistics stats = new MonthStatistics();
        double totalWork = 0;
        double totalOt = 0;
        int workDays = 0;
        int leaveCount = 0;
        int lateCount = 0;

        for (DailyRecord r : records) {
            if ("WORK".equals(r.getStatus())) {
                workDays++;
                totalWork += r.getWorkHours();
                totalOt += r.getOvertimeHours();
                if (r.isLate()) lateCount++;
            } else if ("LEAVE".equals(r.getStatus())) {
                leaveCount++;
                if (!r.isFullDayLeave()) {
                    // 非全天请假：算上班天数，工时正常累加，迟到也算
                    workDays++;
                    totalWork += r.getWorkHours();
                    totalOt += r.getOvertimeHours();
                    if (r.isLate()) lateCount++;
                }
            }
        }

        stats.setWorkDays(workDays);
        stats.setTotalWorkHours(totalWork);
        stats.setTotalOvertimeHours(totalOt);
        stats.setLeaveCount(leaveCount);
        stats.setLateCount(lateCount);
        stats.setAverageDailyHours(workDays > 0 ? totalWork / workDays : 0);

        if (pref.getHourlyRate() != null) {
            double salary = totalWork * pref.getHourlyRate()
                    + totalOt * pref.getHourlyRate() * pref.getOvertimeMultiplier();
            stats.setEstimatedSalary(salary);
        }

        statistics.postValue(stats);
    }

    public DailyRecord getRecordByDate(String date) {
        return AppDatabase.getInstance(getApplication()).dailyRecordDao().getRecordByDate(date);
    }
}
