package com.toilernote.model;

public class MonthStatistics {
    private int workDays;
    private double totalWorkHours;
    private double totalOvertimeHours;
    private int leaveCount;
    private int lateCount;
    private double averageDailyHours;
    private double estimatedSalary;

    public MonthStatistics() {
    }

    public int getWorkDays() {
        return workDays;
    }

    public void setWorkDays(int workDays) {
        this.workDays = workDays;
    }

    public double getTotalWorkHours() {
        return totalWorkHours;
    }

    public void setTotalWorkHours(double totalWorkHours) {
        this.totalWorkHours = totalWorkHours;
    }

    public double getTotalOvertimeHours() {
        return totalOvertimeHours;
    }

    public void setTotalOvertimeHours(double totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public int getLeaveCount() {
        return leaveCount;
    }

    public void setLeaveCount(int leaveCount) {
        this.leaveCount = leaveCount;
    }

    public int getLateCount() {
        return lateCount;
    }

    public void setLateCount(int lateCount) {
        this.lateCount = lateCount;
    }

    public double getAverageDailyHours() {
        return averageDailyHours;
    }

    public void setAverageDailyHours(double averageDailyHours) {
        this.averageDailyHours = averageDailyHours;
    }

    public double getEstimatedSalary() {
        return estimatedSalary;
    }

    public void setEstimatedSalary(double estimatedSalary) {
        this.estimatedSalary = estimatedSalary;
    }
}
