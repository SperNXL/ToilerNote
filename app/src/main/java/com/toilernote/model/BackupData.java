package com.toilernote.model;

import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;

import java.util.List;

public class BackupData {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    private int schemaVersion;
    private String appVersion;
    private String exportAt;
    private UserPreference preference;
    private List<DailyRecord> records;

    public BackupData() {
    }

    public BackupData(String appVersion, String exportAt, UserPreference preference, List<DailyRecord> records) {
        this.schemaVersion = CURRENT_SCHEMA_VERSION;
        this.appVersion = appVersion;
        this.exportAt = exportAt;
        this.preference = preference;
        this.records = records;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getExportAt() {
        return exportAt;
    }

    public void setExportAt(String exportAt) {
        this.exportAt = exportAt;
    }

    public UserPreference getPreference() {
        return preference;
    }

    public void setPreference(UserPreference preference) {
        this.preference = preference;
    }

    public List<DailyRecord> getRecords() {
        return records;
    }

    public void setRecords(List<DailyRecord> records) {
        this.records = records;
    }
}
