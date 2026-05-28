package com.toilernote.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.toilernote.database.AppDatabase;
import com.toilernote.dao.DailyRecordDao;
import com.toilernote.dao.UserPreferenceDao;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;

import java.util.List;

public class RecordRepository {

    private final DailyRecordDao dailyRecordDao;
    private final UserPreferenceDao userPreferenceDao;

    public RecordRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dailyRecordDao = db.dailyRecordDao();
        this.userPreferenceDao = db.userPreferenceDao();
    }

    // DailyRecord operations
    public void insertRecord(DailyRecord record) {
        AsyncTask.execute(() -> dailyRecordDao.insert(record));
    }

    public void updateRecord(DailyRecord record) {
        AsyncTask.execute(() -> dailyRecordDao.update(record));
    }

    public DailyRecord getRecordByDate(String date) {
        return dailyRecordDao.getRecordByDate(date);
    }

    public LiveData<DailyRecord> getRecordByDateLive(String date) {
        return dailyRecordDao.getRecordByDateLive(date);
    }

    public LiveData<List<DailyRecord>> getRecordsByMonth(String monthPrefix) {
        return dailyRecordDao.getRecordsByMonth(monthPrefix);
    }

    public LiveData<List<DailyRecord>> getAllRecords() {
        return dailyRecordDao.getAllRecords();
    }

    public void deleteRecordsByMonth(String monthPrefix) {
        AsyncTask.execute(() -> dailyRecordDao.deleteRecordsByMonth(monthPrefix));
    }

    public void deleteAllRecords() {
        AsyncTask.execute(() -> dailyRecordDao.deleteAll());
    }

    // UserPreference operations
    public void insertPreference(UserPreference preference) {
        AsyncTask.execute(() -> userPreferenceDao.insert(preference));
    }

    public void updatePreference(UserPreference preference) {
        AsyncTask.execute(() -> userPreferenceDao.update(preference));
    }

    public UserPreference getPreference() {
        return userPreferenceDao.getPreference();
    }

    public LiveData<UserPreference> getPreferenceLive() {
        return userPreferenceDao.getPreferenceLive();
    }
}
