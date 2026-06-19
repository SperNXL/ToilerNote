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

    private final AppDatabase db;
    private final DailyRecordDao dailyRecordDao;
    private final UserPreferenceDao userPreferenceDao;

    public RecordRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
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

    public void updateRecordSync(DailyRecord record) {
        dailyRecordDao.update(record);
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

    public List<DailyRecord> getRecordsByMonthSync(String monthPrefix) {
        return dailyRecordDao.getRecordsByMonthSync(monthPrefix);
    }

    public LiveData<List<DailyRecord>> getAllRecords() {
        return dailyRecordDao.getAllRecords();
    }

    public List<DailyRecord> getAllRecordsSync() {
        return dailyRecordDao.getAllRecordsSync();
    }

    public void insertAllRecordsSync(List<DailyRecord> records) {
        dailyRecordDao.insertAll(records);
    }

    public void deleteAllRecordsSync() {
        dailyRecordDao.deleteAll();
    }

    /**
     * 原子化导入数据：先插入偏好设置，再删除全部记录，最后插入新记录。
     * 三步操作在同一个数据库事务中执行，任何一步失败都会回滚，避免数据丢失。
     */
    public void importDataSync(UserPreference preference, List<DailyRecord> records) {
        db.runInTransaction(() -> {
            userPreferenceDao.insert(preference);
            dailyRecordDao.deleteAll();
            dailyRecordDao.insertAll(records);
        });
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

    public void insertPreferenceSync(UserPreference preference) {
        userPreferenceDao.insert(preference);
    }

    public void updatePreference(UserPreference preference) {
        AsyncTask.execute(() -> userPreferenceDao.update(preference));
    }

    public void updatePreferenceSync(UserPreference preference) {
        userPreferenceDao.update(preference);
    }

    public UserPreference getPreference() {
        return userPreferenceDao.getPreference();
    }

    public LiveData<UserPreference> getPreferenceLive() {
        return userPreferenceDao.getPreferenceLive();
    }
}
