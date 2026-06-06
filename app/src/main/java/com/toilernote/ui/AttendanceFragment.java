package com.toilernote.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.toilernote.adapter.CalendarAdapter;
import com.toilernote.databinding.FragmentAttendanceBinding;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.model.MonthStatistics;
import com.toilernote.utils.TimeUtils;
import com.toilernote.viewmodel.CalendarViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttendanceFragment extends Fragment {

    private FragmentAttendanceBinding binding;
    private CalendarViewModel viewModel;
    private CalendarAdapter adapter;
    private UserPreference currentPreference;
    private List<DailyRecord> currentRecords = new ArrayList<>();
    private Calendar currentMonth;

    public AttendanceFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        adapter = new CalendarAdapter();
        binding.recyclerCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.recyclerCalendar.setAdapter(adapter);
        binding.recyclerCalendar.setNestedScrollingEnabled(false);

        adapter.setOnDayClickListener((date, isCurrentMonth) -> {
            if (isCurrentMonth) {
                openEditSheet(date);
            }
        });

        binding.btnPrevMonth.setOnClickListener(v -> viewModel.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), this::updateCalendar);
        viewModel.getMonthlyRecords().observe(getViewLifecycleOwner(), records -> {
            currentRecords = records != null ? records : new ArrayList<>();
            if (currentMonth != null) {
                renderCalendar(currentMonth, currentRecords);
            }
            if (currentPreference != null) {
                viewModel.calculateStatistics(currentRecords, currentPreference);
            }
        });
        viewModel.getUserPreference().observe(getViewLifecycleOwner(), pref -> {
            currentPreference = pref;
            adapter.setPreference(pref);
            if (currentMonth != null) {
                renderCalendar(currentMonth, currentRecords);
            }
            if (currentRecords != null) {
                viewModel.calculateStatistics(currentRecords, pref);
            }
        });
        viewModel.getStatistics().observe(getViewLifecycleOwner(), this::updateStatistics);
    }

    private void updateCalendar(Calendar calendar) {
        currentMonth = calendar;
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        String monthText = String.format(java.util.Locale.getDefault(), "%d年%d月", year, month + 1);
        binding.tvMonthYear.setText(monthText);
        renderCalendar(calendar, currentRecords);
    }

    private void renderCalendar(Calendar calendar, List<DailyRecord> records) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        int daysInMonth = TimeUtils.getDaysInMonth(year, month);
        int firstDayOfWeek = TimeUtils.getFirstDayOfWeek(year, month);
        int prevMonthDays = TimeUtils.getDaysInMonth(
                month == 0 ? year - 1 : year, month == 0 ? 11 : month - 1);

        List<CalendarAdapter.DayItem> items = new ArrayList<>();

        // Previous month padding
        for (int i = firstDayOfWeek - 1; i >= 0; i--) {
            items.add(new CalendarAdapter.DayItem(prevMonthDays - i, null, false, false));
        }

        // Parse work days configuration
        Set<Integer> workDays = null;
        String effectiveDate = null;
        boolean hasWorkDaysConfig = false;
        if (currentPreference != null && currentPreference.getWorkWeekDays() != null) {
            workDays = new HashSet<>();
            String[] days = currentPreference.getWorkWeekDays().split(",");
            for (String d : days) {
                try {
                    workDays.add(Integer.parseInt(d.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            effectiveDate = currentPreference.getWorkDaysEffectiveDate();
            hasWorkDaysConfig = !workDays.isEmpty();
        }

        // Current month
        for (int day = 1; day <= daysInMonth; day++) {
            String dateStr = TimeUtils.formatDate(year, month, day);
            boolean isToday = TimeUtils.isToday(year, month, day);

            boolean isRestDay = false;
            if (hasWorkDaysConfig) {
                boolean shouldApply = effectiveDate == null || dateStr.compareTo(effectiveDate) >= 0;
                if (shouldApply) {
                    int dayOfWeek = TimeUtils.getDayOfWeek(dateStr);
                    if (!workDays.contains(dayOfWeek)) {
                        isRestDay = true;
                    }
                }
            }

            CalendarAdapter.DayItem item = new CalendarAdapter.DayItem(day, dateStr, true, isToday);
            item.isRestDay = isRestDay;
            for (DailyRecord r : records) {
                if (dateStr.equals(r.getDate())) {
                    item.record = r;
                    break;
                }
            }
            items.add(item);
        }

        // Next month padding
        int totalCells = firstDayOfWeek + daysInMonth;
        int remaining = 7 - (totalCells % 7);
        if (remaining < 7) {
            for (int day = 1; day <= remaining; day++) {
                items.add(new CalendarAdapter.DayItem(day, null, false, false));
            }
        }

        adapter.setData(items);
    }

    private void updateStatistics(MonthStatistics stats) {
        binding.statWorkDays.tvStatLabel.setText("上班天数");
        binding.statWorkDays.tvStatValue.setText(String.valueOf(stats.getWorkDays()));

        binding.statWorkHours.tvStatLabel.setText("工时合计");
        binding.statWorkHours.tvStatValue.setText(String.format(java.util.Locale.getDefault(), "%.1fh", stats.getTotalWorkHours()));

        binding.statOvertime.tvStatLabel.setText("加班时长");
        binding.statOvertime.tvStatValue.setText(String.format(java.util.Locale.getDefault(), "%.1fh", stats.getTotalOvertimeHours()));

        binding.statLeaveDays.tvStatLabel.setText("请假天数");
        binding.statLeaveDays.tvStatValue.setText(String.valueOf(stats.getLeaveDays()));

        binding.statLateCount.tvStatLabel.setText("迟到次数");
        binding.statLateCount.tvStatValue.setText(String.valueOf(stats.getLateCount()));

        binding.statEstimatedSalary.tvStatLabel.setText("预估月薪");
        if (stats.getEstimatedSalary() > 0) {
            binding.statEstimatedSalary.tvStatValue.setText(String.format(java.util.Locale.getDefault(), "¥%.0f", stats.getEstimatedSalary()));
        } else {
            binding.statEstimatedSalary.tvStatValue.setText("--");
        }
    }

    private void openEditSheet(String date) {
        RecordEditDialogFragment dialog = RecordEditDialogFragment.newInstance(date);
        dialog.show(getParentFragmentManager(), "edit_record");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
