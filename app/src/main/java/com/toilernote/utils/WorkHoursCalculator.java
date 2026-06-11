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

        // 获取午休时间段（优先使用用户配置的实际午休时间）
        int midBreakStart, midBreakEnd;
        if (pref.getMidBreakStart() != null && pref.getMidBreakEnd() != null) {
            midBreakStart = TimeUtils.timeToMinutes(pref.getMidBreakStart());
            midBreakEnd = TimeUtils.timeToMinutes(pref.getMidBreakEnd());
        } else {
            midBreakStart = plannedStartMin + (plannedEndMin - plannedStartMin) / 2 - midBreak / 2;
            midBreakEnd = midBreakStart + midBreak;
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
                // 只扣除不在请假时间内的午休
                int effectiveMidBreak = getEffectiveBreak(midBreakStart, midBreakEnd, midBreak, leaveStartMin, leaveEndMin);
                int baseWorkMin = plannedEndMin - plannedStartMin - effectiveMidBreak - leaveDuration;
                record.setWorkHours(Math.max(0, baseWorkMin / 60.0));
                record.setOvertimeHours(0);
                record.setLate(false);
            } else {
                // 非全天请假：按正常上班逻辑计算，扣除请假时间段
                int leaveStartMin = 0, leaveEndMin = 0;
                int leaveDuration = 0;
                if (record.getLeaveStart() != null && record.getLeaveEnd() != null) {
                    leaveStartMin = TimeUtils.timeToMinutes(record.getLeaveStart());
                    leaveEndMin = TimeUtils.timeToMinutes(record.getLeaveEnd());
                    leaveDuration = Math.max(0, leaveEndMin - leaveStartMin);
                }

                // 只扣除不在请假时间内的午休，避免重复扣减
                int effectiveMidBreak = getEffectiveBreak(midBreakStart, midBreakEnd, midBreak, leaveStartMin, leaveEndMin);
                int baseWorkMin = plannedEndMin - plannedStartMin - effectiveMidBreak;
                int actualWorkMin = actualEndMin - actualStartMin - effectiveMidBreak - leaveDuration;

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
            // 只扣除不在请假时间内的午休
            int effectiveMidBreak = getEffectiveBreak(midBreakStart, midBreakEnd, midBreak, leaveStartMin, leaveEndMin);
            actualWorkMin = actualEndMin - actualStartMin - effectiveMidBreak - leaveDuration;
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

    /**
     * 计算有效午休时间（扣除与请假时间重叠的部分）
     * 假设午休位于工作时间的中间段
     */
    private static int getEffectiveBreak(int midBreakStart, int midBreakEnd, int midBreak,
                                         int leaveStartMin, int leaveEndMin) {
        if (leaveStartMin <= 0 && leaveEndMin <= 0) {
            return midBreak;
        }
        int overlap = Math.max(0, Math.min(midBreakEnd, leaveEndMin) - Math.max(midBreakStart, leaveStartMin));
        return Math.max(0, midBreak - overlap);
    }
}
