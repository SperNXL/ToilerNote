package com.toilernote.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.toilernote.model.BackupData;

public class BackupJsonHelper {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    public static String toJson(BackupData data) {
        return GSON.toJson(data);
    }

    public static BackupData fromJson(String json) throws JsonParseException {
        return GSON.fromJson(json, BackupData.class);
    }

    public static boolean isValid(BackupData data) {
        return data != null
                && data.getSchemaVersion() == BackupData.CURRENT_SCHEMA_VERSION
                && data.getPreference() != null
                && data.getRecords() != null;
    }
}
