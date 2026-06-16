package com.toilernote.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.repository.RecordRepository;

import java.util.function.Consumer;

public class RecordEditViewModel extends AndroidViewModel {

    private final RecordRepository repository;
    private final LiveData<UserPreference> userPreference;

    public RecordEditViewModel(@NonNull Application application) {
        super(application);
        repository = new RecordRepository(application);
        userPreference = repository.getPreferenceLive();
    }

    public LiveData<UserPreference> getUserPreference() {
        return userPreference;
    }

    public LiveData<DailyRecord> getRecordByDate(String date) {
        return repository.getRecordByDateLive(date);
    }

    public void getRecordByDateSync(String date, Consumer<DailyRecord> callback) {
        new Thread(() -> {
            DailyRecord record = repository.getRecordByDate(date);
            new Handler(Looper.getMainLooper()).post(() -> callback.accept(record));
        }).start();
    }

    public void saveRecord(DailyRecord record) {
        repository.insertRecord(record);
    }
}
