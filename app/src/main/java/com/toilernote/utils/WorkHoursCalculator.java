package com.toilernote.utils;

import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;

public class WorkHoursCalculator {

    public static void calculate(DailyRecord record, UserPreference pref) {
        if (record == null || pref == null) return;

        String status = record.getStatus();
        if ("REST".equals(status)) {
            record.setWorkHours(0);
            record.setOvertimeHours(0);
            record.setLate(false);
            return;
        }

        int plannedStartMin = TimeUtils.timeToMinutes(
                record.getPlannedStart() != null ? record.getPlannedStart() : pref.getDefaultWorkStart());
        int plannedEndMin = TimeUtils.timeToMinutes(
                record.getPlannedEnd() != null ? record.getPlannedEnd() : pref.getDefaultWorkEnd());
        int actualStartMin = TimeUtils.timeToMinutes(record.getActualStart());
        int actualEndMin = TimeUtils.timeToMinutes(record.getActualEnd());

        int midBreak = record.getMidBreakMinutes();
        if (midBreak <= 0) {
            midBreak = TimeUtils.parseBreakDuration(pref.getDefaultMidBreak());
        }
        int nightBreak = record.getNightBreakMinutes();
        if (nightBreak <= 0 && !record.isFullDayOvertime()) {
            nightBreak = TimeUtils.parseBreakDuration(pref.getDefaultNightBreak());
        }

        // Late check
        record.setLate(actualStartMin > plannedStartMin);

        if (record.isFullDayOvertime()) {
            // Full day overtime
            record.setWorkHours(0);
            int overtimeMin = actualEndMin - actualStartMin - midBreak;
            record.setOvertimeHours(Math.max(0, overtimeMin / 60.0));
            return;
        }

        if ("LEAVE".equals(status)) {
            if (record.isFullDayLeave()) {
                // 全天请假
                int leaveStartMin = TimeUtils.timeToMinutes(record.getLeaveStart());
                int leaveEndMin = TimeUtils.timeToMinutes(record.getLeaveEnd());
                int leaveDuration = Math.max(0, leaveEndMin - leaveStartMin);
                int baseWorkMin = plannedEndMin - plannedStartMin - midBreak - leaveDuration;
                record.setWorkHours(Math.max(0, baseWorkMin / 60.0));
                record.setOvertimeHours(0);
                record.setLate(false);
            } else {
                // 非全天请假：按正常上班逻辑计算，扣除请假时间段
                int baseWorkMin = plannedEndMin - plannedStartMin - midBreak;
                int actualWorkMin = actualEndMin - actualStartMin - midBreak;

                int leaveDuration = 0;
                if (record.getLeaveStart() != null && record.getLeaveEnd() != null) {
                    int leaveStartMin = TimeUtils.timeToMinutes(record.getLeaveStart());
                    int leaveEndMin = TimeUtils.timeToMinutes(record.getLeaveEnd());
                    leaveDuration = Math.max(0, leaveEndMin - leaveStartMin);
                    actualWorkMin = Math.max(0, actualWorkMin - leaveDuration);
                }

                record.setLate(actualStartMin > plannedStartMin);

                if (actualWorkMin > baseWorkMin) {
                    record.setWorkHours(baseWorkMin / 60.0);
                    int overtimeMin = actualWorkMin - baseWorkMin - nightBreak;
                    record.setOvertimeHours(Math.max(0, overtimeMin / 60.0));
                } else {
                    record.setWorkHours(Math.max(0, actualWorkMin / 60.0));
                    record.setOvertimeHours(0);
                }
            }
            return;
        }

        // Normal work day
        int baseWorkMin = plannedEndMin - plannedStartMin - midBreak;
        int actualWorkMin = actualEndMin - actualStartMin - midBreak;

        int leaveDuration = 0;
        if (record.getLeaveStart() != null && record.getLeaveEnd() != null) {
            int leaveStartMin = TimeUtils.timeToMinutes(record.getLeaveStart());
            int leaveEndMin = TimeUtils.timeToMinutes(record.getLeaveEnd());
            leaveDuration = Math.max(0, leaveEndMin - leaveStartMin);
            actualWorkMin = Math.max(0, actualWorkMin - leaveDuration);
        }

        if (actualWorkMin > baseWorkMin) {
            record.setWorkHours(baseWorkMin / 60.0);
            int overtimeMin = actualWorkMin - baseWorkMin - nightBreak;
            record.setOvertimeHours(Math.max(0, overtimeMin / 60.0));
        } else {
            record.setWorkHours(Math.max(0, actualWorkMin / 60.0));
            record.setOvertimeHours(0);
        }
    }
}
