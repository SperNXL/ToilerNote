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

        // 防御：实际上下班时间无效时，不计算工时，避免产生负数或极大值
        if (!isValidTime(record.getActualStart()) || !isValidTime(record.getActualEnd())) {
            record.setWorkHours(0);
            record.setOvertimeHours(0);
            record.setLate(false);
            return;
        }

        int effectiveActualStartMin = Math.max(actualStartMin, plannedStartMin);

        BreakInfo midBreak = resolveMidBreak(record, pref, plannedStartMin, plannedEndMin);
        BreakInfo nightBreak = resolveNightBreak(record, pref, plannedStartMin, plannedEndMin);

        // Late check
        record.setLate(actualStartMin > plannedStartMin);

        if (record.isFullDayOvertime()) {
            // Full day overtime：早到部分不计入加班；
            // 只扣除与加班时段重叠的午休和晚休部分
            record.setWorkHours(0);
            int effectiveMidBreak = getBreakOverlap(midBreak.start, midBreak.end,
                    effectiveActualStartMin, actualEndMin);
            int effectiveNightBreak = getBreakOverlap(nightBreak.start, nightBreak.end,
                    effectiveActualStartMin, actualEndMin);
            int overtimeMin = actualEndMin - effectiveActualStartMin - effectiveMidBreak - effectiveNightBreak;
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
                int effectiveMidBreak = getEffectiveBreak(midBreak.start, midBreak.end, midBreak.duration, leaveStartMin, leaveEndMin);
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
                int effectiveMidBreak = getEffectiveBreak(midBreak.start, midBreak.end, midBreak.duration, leaveStartMin, leaveEndMin);
                int baseWorkMin = plannedEndMin - plannedStartMin - effectiveMidBreak;
                int actualWorkMin = actualEndMin - effectiveActualStartMin - effectiveMidBreak - leaveDuration;

                record.setLate(actualStartMin > plannedStartMin);

                if (actualWorkMin > baseWorkMin) {
                    record.setWorkHours(baseWorkMin / 60.0);
                    int overtimeMin = actualWorkMin - baseWorkMin - nightBreak.duration;
                    record.setOvertimeHours(Math.max(0, overtimeMin / 60.0));
                } else {
                    record.setWorkHours(Math.max(0, actualWorkMin / 60.0));
                    record.setOvertimeHours(0);
                }
            }
            return;
        }

        // Normal work day
        int baseWorkMin = plannedEndMin - plannedStartMin - midBreak.duration;
        int actualWorkMin = actualEndMin - effectiveActualStartMin - midBreak.duration;

        int leaveDuration = 0;
        if (record.getLeaveStart() != null && record.getLeaveEnd() != null) {
            int leaveStartMin = TimeUtils.timeToMinutes(record.getLeaveStart());
            int leaveEndMin = TimeUtils.timeToMinutes(record.getLeaveEnd());
            leaveDuration = Math.max(0, leaveEndMin - leaveStartMin);
            // 只扣除不在请假时间内的午休
            int effectiveMidBreak = getEffectiveBreak(midBreak.start, midBreak.end, midBreak.duration, leaveStartMin, leaveEndMin);
            actualWorkMin = actualEndMin - effectiveActualStartMin - effectiveMidBreak - leaveDuration;
        }

        if (actualWorkMin > baseWorkMin) {
            record.setWorkHours(baseWorkMin / 60.0);
            int overtimeMin = actualWorkMin - baseWorkMin - nightBreak.duration;
            record.setOvertimeHours(Math.max(0, overtimeMin / 60.0));
        } else {
            record.setWorkHours(Math.max(0, actualWorkMin / 60.0));
            record.setOvertimeHours(0);
        }
    }

    public static BreakInfo resolveMidBreak(DailyRecord record, UserPreference pref,
                                            int plannedStartMin, int plannedEndMin) {
        // 1. 记录自定义了中间休息
        if (record.isCustomMidBreak() && isValidRange(record.getMidBreakStart(), record.getMidBreakEnd())) {
            return fromRange(record.getMidBreakStart(), record.getMidBreakEnd());
        }
        // 2. 旧数据：按分钟数视为自定义，从计划工作时间中点推算
        if (!record.isCustomMidBreak() && record.getMidBreakMinutes() > 0) {
            return fromDurationMidpoint(record.getMidBreakMinutes(), plannedStartMin, plannedEndMin);
        }
        // 3. 设置开启且有有效时间段
        if (pref.isMidBreakEnabled() && isValidRange(pref.getMidBreakStart(), pref.getMidBreakEnd())) {
            return fromRange(pref.getMidBreakStart(), pref.getMidBreakEnd());
        }
        // 4. 设置开启但无有效时间段：用默认时长从中点推算
        if (pref.isMidBreakEnabled()) {
            int duration = TimeUtils.parseBreakDuration(pref.getDefaultMidBreak());
            if (duration > 0) {
                return fromDurationMidpoint(duration, plannedStartMin, plannedEndMin);
            }
        }
        // 5. 未启用
        return new BreakInfo(0, 0, 0);
    }

    public static BreakInfo resolveNightBreak(DailyRecord record, UserPreference pref,
                                              int plannedStartMin, int plannedEndMin) {
        // 1. 记录自定义了晚上休息
        if (record.isCustomNightBreak() && isValidRange(record.getNightBreakStart(), record.getNightBreakEnd())) {
            return fromRange(record.getNightBreakStart(), record.getNightBreakEnd());
        }
        // 2. 旧数据：按分钟数视为自定义，从计划工作时间中点推算
        if (!record.isCustomNightBreak() && record.getNightBreakMinutes() > 0) {
            return fromDurationMidpoint(record.getNightBreakMinutes(), plannedStartMin, plannedEndMin);
        }
        // 3. 设置开启且有有效时间段
        if (pref.isNightBreakEnabled() && isValidRange(pref.getNightBreakStart(), pref.getNightBreakEnd())) {
            return fromRange(pref.getNightBreakStart(), pref.getNightBreakEnd());
        }
        // 4. 设置开启但无有效时间段：用默认时长从中点推算
        if (pref.isNightBreakEnabled()) {
            int duration = TimeUtils.parseBreakDuration(pref.getDefaultNightBreak());
            if (duration > 0) {
                return fromDurationMidpoint(duration, plannedStartMin, plannedEndMin);
            }
        }
        // 5. 未启用
        return new BreakInfo(0, 0, 0);
    }

    private static boolean isValidRange(String start, String end) {
        return isValidTime(start) && isValidTime(end);
    }

    private static boolean isValidTime(String time) {
        return time != null && !time.trim().isEmpty() && time.contains(":");
    }

    private static BreakInfo fromRange(String start, String end) {
        int startMin = TimeUtils.timeToMinutes(start);
        int endMin = TimeUtils.timeToMinutes(end);
        return new BreakInfo(startMin, endMin, Math.max(0, endMin - startMin));
    }

    private static BreakInfo fromDurationMidpoint(int duration, int plannedStartMin, int plannedEndMin) {
        int center = plannedStartMin + (plannedEndMin - plannedStartMin) / 2;
        int start = center - duration / 2;
        int end = start + duration;
        return new BreakInfo(start, end, duration);
    }

    /**
     * 计算两个时间段的重叠分钟数
     */
    private static int getBreakOverlap(int breakStart, int breakEnd, int workStart, int workEnd) {
        return Math.max(0, Math.min(breakEnd, workEnd) - Math.max(breakStart, workStart));
    }

    /**
     * 计算有效午休时间（扣除与请假时间重叠的部分）
     * 假设午休位于工作时间的中间段
     */
    private static int getEffectiveBreak(int breakStart, int breakEnd, int breakDuration,
                                         int leaveStartMin, int leaveEndMin) {
        if (leaveStartMin <= 0 && leaveEndMin <= 0) {
            return breakDuration;
        }
        int overlap = getBreakOverlap(breakStart, breakEnd, leaveStartMin, leaveEndMin);
        return Math.max(0, breakDuration - overlap);
    }

    public static class BreakInfo {
        public final int start;
        public final int end;
        public final int duration;

        public BreakInfo(int start, int end, int duration) {
            this.start = start;
            this.end = end;
            this.duration = duration;
        }
    }
}
