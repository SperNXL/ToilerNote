package com.toilernote.utils;

import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * WorkHoursCalculator 单元测试
 */
public class WorkHoursCalculatorTest {

    private UserPreference createPreference(boolean nightBreakEnabled) {
        UserPreference pref = new UserPreference();
        pref.setDefaultWorkStart("09:00");
        pref.setDefaultWorkEnd("18:00");
        pref.setMidBreakStart("12:00");
        pref.setMidBreakEnd("14:00");
        pref.setMidBreakEnabled(true);
        pref.setNightBreakStart("18:00");
        pref.setNightBreakEnd("19:00");
        pref.setNightBreakEnabled(nightBreakEnabled);
        return pref;
    }

    private DailyRecord createLeaveRecord(String leaveStart, String leaveEnd,
                                          String actualStart, String actualEnd) {
        DailyRecord record = new DailyRecord("2026-06-19", "LEAVE");
        record.setPlannedStart("09:00");
        record.setPlannedEnd("18:00");
        record.setActualStart(actualStart);
        record.setActualEnd(actualEnd);
        record.setLeaveStart(leaveStart);
        record.setLeaveEnd(leaveEnd);
        record.setFullDayLeave(false);
        return record;
    }

    /**
     * 用户反馈场景：非全天请假，计划18:00下班，实际20:00下班，
     * 晚休 18:00-19:00 开启时应扣除，加班应为 1 小时。
     */
    @Test
    public void leaveWithOvertime_nightBreakEnabled_overtimeIsOneHour() {
        DailyRecord record = createLeaveRecord("09:00", "12:00", "09:00", "20:00");
        UserPreference pref = createPreference(true);

        WorkHoursCalculator.calculate(record, pref);

        assertEquals(1.0, record.getOvertimeHours(), 0.001);
    }

    /**
     * 非全天请假，计划18:00下班，实际20:00下班，晚休关闭时加班应为 2 小时。
     */
    @Test
    public void leaveWithOvertime_nightBreakDisabled_overtimeIsTwoHours() {
        DailyRecord record = createLeaveRecord("09:00", "12:00", "09:00", "20:00");
        UserPreference pref = createPreference(false);

        WorkHoursCalculator.calculate(record, pref);

        assertEquals(2.0, record.getOvertimeHours(), 0.001);
    }

    /**
     * 实际下班时间只比计划下班晚半小时，且落在晚休内，加班应为 0。
     */
    @Test
    public void leaveWithOvertime_actualEndInsideNightBreak_overtimeIsZero() {
        DailyRecord record = createLeaveRecord("09:00", "12:00", "09:00", "18:30");
        UserPreference pref = createPreference(true);

        WorkHoursCalculator.calculate(record, pref);

        assertEquals(0.0, record.getOvertimeHours(), 0.001);
    }

    /**
     * 全天请假，但仍有实际下班时间晚于计划下班，加班应正常计算。
     */
    @Test
    public void fullDayLeave_withOvertime_overtimeIsOneHour() {
        DailyRecord record = new DailyRecord("2026-06-19", "LEAVE");
        record.setPlannedStart("09:00");
        record.setPlannedEnd("18:00");
        record.setActualStart("09:00");
        record.setActualEnd("20:00");
        record.setLeaveStart("09:00");
        record.setLeaveEnd("18:00");
        record.setFullDayLeave(true);

        UserPreference pref = createPreference(true);
        WorkHoursCalculator.calculate(record, pref);

        assertEquals(1.0, record.getOvertimeHours(), 0.001);
        assertEquals(0.0, record.getWorkHours(), 0.001);
    }

    /**
     * 普通工作日，计划18:00下班，实际20:00下班，晚休开启，加班应为 1 小时。
     */
    @Test
    public void normalWorkDay_withOvertime_overtimeIsOneHour() {
        DailyRecord record = new DailyRecord("2026-06-19", "WORK");
        record.setPlannedStart("09:00");
        record.setPlannedEnd("18:00");
        record.setActualStart("09:00");
        record.setActualEnd("20:00");

        UserPreference pref = createPreference(true);
        WorkHoursCalculator.calculate(record, pref);

        assertEquals(1.0, record.getOvertimeHours(), 0.001);
    }
}
