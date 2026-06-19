package com.toilernote.viewmodel;

import android.app.Application;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.toilernote.database.AppDatabase;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.model.BackupData;
import com.toilernote.repository.RecordRepository;
import com.toilernote.utils.BackupJsonHelper;
import com.toilernote.utils.TimeUtils;
import com.toilernote.utils.WorkHoursCalculator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

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

    public void exportData(Uri uri, Consumer<Boolean> onResult) {
        new Thread(() -> {
            boolean success = false;
            try {
                UserPreference pref = repository.getPreference();
                List<DailyRecord> records = repository.getAllRecordsSync();
                String appVersion = getAppVersion();
                Calendar cal = Calendar.getInstance();
                String exportAt = String.format(java.util.Locale.getDefault(), "%d-%02d-%02d",
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                BackupData backup = new BackupData(appVersion, exportAt, pref, records);
                String json = BackupJsonHelper.toJson(backup);

                try (OutputStream os = getApplication().getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                        success = true;
                    }
                }
            } catch (Exception e) {
                handleExportError(e, uri != null ? uri.toString() : "");
            }
            final boolean result = success;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (onResult != null) onResult.accept(result);
            });
        }).start();
    }

    public void importData(Uri uri, Consumer<Boolean> onResult) {
        new Thread(() -> {
            boolean success = false;
            try {
                if (isFileTooLarge(uri)) {
                    throw new IllegalArgumentException("File exceeds 10MB limit");
                }

                StringBuilder sb = new StringBuilder();
                try (InputStream is = getApplication().getContentResolver().openInputStream(uri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }

                BackupData backup = BackupJsonHelper.fromJson(sb.toString());
                if (!BackupJsonHelper.isValid(backup)) {
                    throw new IllegalArgumentException("Invalid backup data");
                }

                UserPreference pref = backup.getPreference();
                if (pref == null) pref = new UserPreference();
                pref.setId(1);

                List<DailyRecord> records = backup.getRecords();
                for (DailyRecord record : records) {
                    if (!"REST".equals(record.getStatus())) {
                        WorkHoursCalculator.calculate(record, pref);
                    }
                }

                repository.deleteAllRecordsSync();
                repository.insertPreferenceSync(pref);
                repository.insertAllRecordsSync(records);
                success = true;
            } catch (Exception e) {
                handleImportError(e, uri != null ? uri.toString() : "");
            }
            final boolean result = success;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (onResult != null) onResult.accept(result);
            });
        }).start();
    }

    private boolean isFileTooLarge(Uri uri) {
        try (Cursor cursor = getApplication().getContentResolver().query(
                uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    long size = cursor.getLong(sizeIndex);
                    return size > 10 * 1024 * 1024L;
                }
            }
        }
        // 无法获取大小时按安全侧处理：认为不大，后续读流时若真超大再自然失败
        return false;
    }

    private void handleImportError(Throwable e, String uriStr) {
        String code;
        String detail = "URI=" + uriStr + ", message=" + e.getMessage();
        if (e instanceof FileNotFoundException) {
            code = "EI01";
        } else if (e instanceof SecurityException) {
            code = "EI02";
        } else if (e instanceof JsonSyntaxException || e instanceof JsonParseException) {
            code = "EI03";
        } else if (e instanceof IllegalArgumentException && detail.contains("schemaVersion")) {
            code = "EI04";
        } else if (e instanceof IllegalArgumentException && detail.contains("File exceeds")) {
            code = "EI07";
        } else if (e instanceof IllegalArgumentException) {
            code = "EI05";
        } else if (e instanceof IOException) {
            code = "EI06";
        } else {
            code = "EI08";
        }
        logError(code, e.getClass().getSimpleName(), detail, e);
    }

    private void handleExportError(Throwable e, String uriStr) {
        String code;
        String detail = "URI=" + uriStr + ", message=" + e.getMessage();
        if (e instanceof IOException && e.getMessage() != null
                && (e.getMessage().contains("ENOSPC") || e.getMessage().contains("No space"))) {
            code = "EX01";
        } else if (e instanceof SecurityException) {
            code = "EX02";
        } else if (e instanceof IOException) {
            code = "EX03";
        } else if (e instanceof JsonIOException || e instanceof JsonParseException) {
            code = "EX04";
        } else {
            code = "EX06";
        }
        logError(code, e.getClass().getSimpleName(), detail, e);
    }

    private void logError(String code, String type, String detail, Throwable throwable) {
        String tag = "DataManager";
        String log = "[" + code + "] " + type + ": " + detail;
        if (throwable != null) {
            Log.e(tag, log, throwable);
        } else {
            Log.e(tag, log);
        }
    }

    private String getAppVersion() {
        try {
            return getApplication().getPackageManager()
                    .getPackageInfo(getApplication().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
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
