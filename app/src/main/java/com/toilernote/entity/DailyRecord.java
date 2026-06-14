package com.toilernote.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_records")
public class DailyRecord {

    @PrimaryKey
    @NonNull
    private String date;

    private String status;
    private boolean isFullDayOvertime;
    private boolean isFullDayLeave;
    private boolean customPlannedTime;
    private String plannedStart;
    private String actualStart;
    private String plannedEnd;
    private String actualEnd;
    private int midBreakMinutes;
    private int nightBreakMinutes;
    private String leaveStart;
    private String leaveEnd;
    private double workHours;
    private double overtimeHours;
    private boolean isLate;
    private String remark;

    public DailyRecord() {
        this.date = "";
    }

    @Ignore
    public DailyRecord(@NonNull String date, String status) {
        this.date = date;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFullDayOvertime() {
        return isFullDayOvertime;
    }

    public void setFullDayOvertime(boolean fullDayOvertime) {
        isFullDayOvertime = fullDayOvertime;
    }

    public boolean isFullDayLeave() {
        return isFullDayLeave;
    }

    public void setFullDayLeave(boolean fullDayLeave) {
        isFullDayLeave = fullDayLeave;
    }

    public boolean isCustomPlannedTime() {
        return customPlannedTime;
    }

    public void setCustomPlannedTime(boolean customPlannedTime) {
        this.customPlannedTime = customPlannedTime;
    }

    public String getPlannedStart() {
        return plannedStart;
    }

    public void setPlannedStart(String plannedStart) {
        this.plannedStart = plannedStart;
    }

    public String getActualStart() {
        return actualStart;
    }

    public void setActualStart(String actualStart) {
        this.actualStart = actualStart;
    }

    public String getPlannedEnd() {
        return plannedEnd;
    }

    public void setPlannedEnd(String plannedEnd) {
        this.plannedEnd = plannedEnd;
    }

    public String getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(String actualEnd) {
        this.actualEnd = actualEnd;
    }

    public int getMidBreakMinutes() {
        return midBreakMinutes;
    }

    public void setMidBreakMinutes(int midBreakMinutes) {
        this.midBreakMinutes = midBreakMinutes;
    }

    public int getNightBreakMinutes() {
        return nightBreakMinutes;
    }

    public void setNightBreakMinutes(int nightBreakMinutes) {
        this.nightBreakMinutes = nightBreakMinutes;
    }

    public String getLeaveStart() {
        return leaveStart;
    }

    public void setLeaveStart(String leaveStart) {
        this.leaveStart = leaveStart;
    }

    public String getLeaveEnd() {
        return leaveEnd;
    }

    public void setLeaveEnd(String leaveEnd) {
        this.leaveEnd = leaveEnd;
    }

    public double getWorkHours() {
        return workHours;
    }

    public void setWorkHours(double workHours) {
        this.workHours = workHours;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public boolean isLate() {
        return isLate;
    }

    public void setLate(boolean late) {
        isLate = late;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
