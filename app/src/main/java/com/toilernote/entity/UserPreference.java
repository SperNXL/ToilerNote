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
    private String workDayColor = "#2196F3";
    private String restDayColor = "#4CAF50";
    private String leaveDayColor = "#FFEB3B";
    private String lateDayColor = "#F44336";
    private Double hourlyRate;
    private String workWeekDays = "1,2,3,4,5";
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
        return defaultMidBreak;
    }

    public void setDefaultMidBreak(String defaultMidBreak) {
        this.defaultMidBreak = defaultMidBreak;
    }

    public String getDefaultNightBreak() {
        return defaultNightBreak;
    }

    public void setDefaultNightBreak(String defaultNightBreak) {
        this.defaultNightBreak = defaultNightBreak;
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

    public double getOvertimeMultiplier() {
        return overtimeMultiplier;
    }

    public void setOvertimeMultiplier(double overtimeMultiplier) {
        this.overtimeMultiplier = overtimeMultiplier;
    }
}
