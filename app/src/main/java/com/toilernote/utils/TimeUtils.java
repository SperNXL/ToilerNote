package com.toilernote.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    public static int timeToMinutes(String time) {
        if (time == null || !time.contains(":")) return 0;
        String[] parts = time.split(":");
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String minutesToTime(int minutes) {
        minutes = Math.max(0, minutes);
        int h = (minutes / 60) % 24;
        int m = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", h, m);
    }

    public static String formatTruncatedHours(double hours) {
        double truncated = Math.floor(hours * 10) / 10.0;
        return String.format(Locale.getDefault(), "%.1f", truncated);
    }

    public static int parseBreakDuration(String breakRange) {
        if (breakRange == null || !breakRange.contains("-")) return 0;
        String[] parts = breakRange.split("-");
        if (parts.length != 2) return 0;
        int start = timeToMinutes(parts[0]);
        int end = timeToMinutes(parts[1]);
        return Math.max(0, end - start);
    }

    public static String formatDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
    }

    public static String getMonthPrefix(int year, int month) {
        return String.format(Locale.getDefault(), "%d-%02d", year, month + 1);
    }

    public static int getDayOfWeek(String dateStr) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.DAY_OF_WEEK) - 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isToday(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == year
                && today.get(Calendar.MONTH) == month
                && today.get(Calendar.DAY_OF_MONTH) == day;
    }

    public static int getDaysInMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getFirstDayOfWeek(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static String getYesterday(String dateStr) {
        try {
            Date date = DATE_FORMAT.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return DATE_FORMAT.format(cal.getTime());
        } catch (Exception e) {
            return null;
        }
    }
}
