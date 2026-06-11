package com.toilernote.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_preferences")
public class UserPreference {

    @PrimaryKey
    @NonNull
    private Integer id = 1;

    private String nickname = "牛马一号";
    private String avatarUri;
    private String defaultWorkStart = "09:00";
    private String defaultWorkEnd = "18:00";
    private String defaultMidBreak = "12:00-14:00";
    private String defaultNightBreak = "18:00-19:00";
    private String midBreakStart = "12:00";
    private String midBreakEnd = "14:00";
    private String nightBreakStart = "18:00";
    private String nightBreakEnd = "19:00";
    private boolean isMidBreakEnabled = true;
    private boolean isNightBreakEnabled = true;
    private String workDayColor = "#6366F1";
    private String restDayColor = "#10B981";
    private String leaveDayColor = "#F59E0B";
    private String lateDayColor = "#EF4444";
    private Double hourlyRate;
    private String workWeekDays = "1,2,3,4,5";
    private String workDaysEffectiveDate;
    private double overtimeMultiplier = 1.5;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public String getDefaultWorkStart() {
        return defaultWorkStart;
    }

    public void setDefaultWorkStart(String defaultWorkStart) {
        this.defaultWorkStart = defaultWorkStart;
    }

    public String getDefaultWorkEnd() {
        return defaultWorkEnd;
    }

    public void setDefaultWorkEnd(String defaultWorkEnd) {
        this.defaultWorkEnd = defaultWorkEnd;
    }

    public String getDefaultMidBreak() {
        if (midBreakStart != null && midBreakEnd != null) {
            return midBreakStart + "-" + midBreakEnd;
        }
        return defaultMidBreak;
    }

    public void setDefaultMidBreak(String defaultMidBreak) {
        this.defaultMidBreak = defaultMidBreak;
    }

    public String getDefaultNightBreak() {
        if (nightBreakStart != null && nightBreakEnd != null) {
            return nightBreakStart + "-" + nightBreakEnd;
        }
        return defaultNightBreak;
    }

    public void setDefaultNightBreak(String defaultNightBreak) {
        this.defaultNightBreak = defaultNightBreak;
    }

    public String getMidBreakStart() {
        return midBreakStart;
    }

    public void setMidBreakStart(String midBreakStart) {
        this.midBreakStart = midBreakStart;
    }

    public String getMidBreakEnd() {
        return midBreakEnd;
    }

    public void setMidBreakEnd(String midBreakEnd) {
        this.midBreakEnd = midBreakEnd;
    }

    public String getNightBreakStart() {
        return nightBreakStart;
    }

    public void setNightBreakStart(String nightBreakStart) {
        this.nightBreakStart = nightBreakStart;
    }

    public String getNightBreakEnd() {
        return nightBreakEnd;
    }

    public void setNightBreakEnd(String nightBreakEnd) {
        this.nightBreakEnd = nightBreakEnd;
    }

    public boolean isMidBreakEnabled() {
        return isMidBreakEnabled;
    }

    public void setMidBreakEnabled(boolean midBreakEnabled) {
        isMidBreakEnabled = midBreakEnabled;
    }

    public boolean isNightBreakEnabled() {
        return isNightBreakEnabled;
    }

    public void setNightBreakEnabled(boolean nightBreakEnabled) {
        isNightBreakEnabled = nightBreakEnabled;
    }

    public String getWorkDayColor() {
        return workDayColor;
    }

    public void setWorkDayColor(String workDayColor) {
        this.workDayColor = workDayColor;
    }

    public String getRestDayColor() {
        return restDayColor;
    }

    public void setRestDayColor(String restDayColor) {
        this.restDayColor = restDayColor;
    }

    public String getLeaveDayColor() {
        return leaveDayColor;
    }

    public void setLeaveDayColor(String leaveDayColor) {
        this.leaveDayColor = leaveDayColor;
    }

    public String getLateDayColor() {
        return lateDayColor;
    }

    public void setLateDayColor(String lateDayColor) {
        this.lateDayColor = lateDayColor;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getWorkWeekDays() {
        return workWeekDays;
    }

    public void setWorkWeekDays(String workWeekDays) {
        this.workWeekDays = workWeekDays;
    }

    public String getWorkDaysEffectiveDate() {
        return workDaysEffectiveDate;
    }

    public void setWorkDaysEffectiveDate(String workDaysEffectiveDate) {
        this.workDaysEffectiveDate = workDaysEffectiveDate;
    }

    public double getOvertimeMultiplier() {
        return overtimeMultiplier;
    }

    public void setOvertimeMultiplier(double overtimeMultiplier) {
        this.overtimeMultiplier = overtimeMultiplier;
    }
}
