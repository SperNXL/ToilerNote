package com.toilernote.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.toilernote.R;
import com.toilernote.databinding.FragmentSettingsBinding;
import com.toilernote.entity.UserPreference;
import com.toilernote.viewmodel.SettingsViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private UserPreference preference;

    private static final String[] COLOR_PRESETS = {
            "#FFFFFF", "#F8FAFC", "#1E293B", "#3B82F6", "#60A5FA", "#FB7185", "#EF4444", "#FACC15"
    };
    private static final String DEFAULT_WORK_COLOR = "#6366F1";
    private static final String DEFAULT_REST_COLOR = "#10B981";
    private static final String DEFAULT_LEAVE_COLOR = "#F59E0B";
    private static final String DEFAULT_LATE_COLOR = "#EF4444";

    private final List<View> workColorViews = new ArrayList<>();
    private final List<View> restColorViews = new ArrayList<>();
    private final List<View> leaveColorViews = new ArrayList<>();
    private final List<View> lateColorViews = new ArrayList<>();

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        viewModel.getUserPreference().observe(getViewLifecycleOwner(), pref -> {
            if (pref != null) {
                this.preference = pref;
                bindSettings();
            }
        });
    }

    private void bindSettings() {
        // Nickname (P1 placeholder)
        binding.tvNickname.setText(preference.getNickname());

        // Work Start Time (always visible)
        binding.etWorkStart.setText(preference.getDefaultWorkStart());
        binding.etWorkStart.setOnClickListener(v ->
                showMaterialTimePicker("上班时间", preference.getDefaultWorkStart(), time -> {
                    preference.setDefaultWorkStart(time);
                    binding.etWorkStart.setText(time);
                    viewModel.savePreference(preference);
                }));

        // Work End Time (always visible)
        binding.etWorkEnd.setText(preference.getDefaultWorkEnd());
        binding.etWorkEnd.setOnClickListener(v ->
                showMaterialTimePicker("下班时间", preference.getDefaultWorkEnd(), time -> {
                    preference.setDefaultWorkEnd(time);
                    binding.etWorkEnd.setText(time);
                    viewModel.savePreference(preference);
                }));

        // Mid Break Switch + expandable form
        binding.switchMidBreak.setChecked(preference.isMidBreakEnabled());
        binding.formMidBreak.setVisibility(preference.isMidBreakEnabled() ? View.VISIBLE : View.GONE);
        binding.switchMidBreak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preference.setMidBreakEnabled(isChecked);
            binding.formMidBreak.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            viewModel.savePreference(preference);
        });
        binding.etMidBreakStart.setText(preference.getMidBreakStart());
        binding.etMidBreakStart.setOnClickListener(v ->
                showMaterialTimePicker("中间休息开始", preference.getMidBreakStart(), time -> {
                    preference.setMidBreakStart(time);
                    binding.etMidBreakStart.setText(time);
                    viewModel.savePreference(preference);
                }));
        binding.etMidBreakEnd.setText(preference.getMidBreakEnd());
        binding.etMidBreakEnd.setOnClickListener(v ->
                showMaterialTimePicker("中间休息结束", preference.getMidBreakEnd(), time -> {
                    preference.setMidBreakEnd(time);
                    binding.etMidBreakEnd.setText(time);
                    viewModel.savePreference(preference);
                }));

        // Night Break Switch + expandable form
        binding.switchNightBreak.setChecked(preference.isNightBreakEnabled());
        binding.formNightBreak.setVisibility(preference.isNightBreakEnabled() ? View.VISIBLE : View.GONE);
        binding.switchNightBreak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preference.setNightBreakEnabled(isChecked);
            binding.formNightBreak.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            viewModel.savePreference(preference);
        });
        binding.etNightBreakStart.setText(preference.getNightBreakStart());
        binding.etNightBreakStart.setOnClickListener(v ->
                showMaterialTimePicker("晚上休息开始", preference.getNightBreakStart(), time -> {
                    preference.setNightBreakStart(time);
                    binding.etNightBreakStart.setText(time);
                    viewModel.savePreference(preference);
                }));
        binding.etNightBreakEnd.setText(preference.getNightBreakEnd());
        binding.etNightBreakEnd.setOnClickListener(v ->
                showMaterialTimePicker("晚上休息结束", preference.getNightBreakEnd(), time -> {
                    preference.setNightBreakEnd(time);
                    binding.etNightBreakEnd.setText(time);
                    viewModel.savePreference(preference);
                }));

        // Work Week Days
        binding.tvWorkWeekDaysValue.setText(formatWorkDays(preference.getWorkWeekDays()) + " ›");
        binding.itemWorkWeekDays.setOnClickListener(v -> showWorkDaysPicker());

        // Calendar Colors
        bindColorRow(binding.colorRowWork, workColorViews, preference.getWorkDayColor(), DEFAULT_WORK_COLOR,
                color -> {
                    preference.setWorkDayColor(color);
                    viewModel.savePreference(preference);
                    refreshColorSelection(workColorViews, color);
                });
        binding.btnResetWorkColor.setOnClickListener(v -> {
            preference.setWorkDayColor(DEFAULT_WORK_COLOR);
            viewModel.savePreference(preference);
            refreshColorSelection(workColorViews, DEFAULT_WORK_COLOR);
            Toast.makeText(requireContext(), "已恢复默认", Toast.LENGTH_SHORT).show();
        });

        bindColorRow(binding.colorRowRest, restColorViews, preference.getRestDayColor(), DEFAULT_REST_COLOR,
                color -> {
                    preference.setRestDayColor(color);
                    viewModel.savePreference(preference);
                    refreshColorSelection(restColorViews, color);
                });
        binding.btnResetRestColor.setOnClickListener(v -> {
            preference.setRestDayColor(DEFAULT_REST_COLOR);
            viewModel.savePreference(preference);
            refreshColorSelection(restColorViews, DEFAULT_REST_COLOR);
            Toast.makeText(requireContext(), "已恢复默认", Toast.LENGTH_SHORT).show();
        });

        bindColorRow(binding.colorRowLeave, leaveColorViews, preference.getLeaveDayColor(), DEFAULT_LEAVE_COLOR,
                color -> {
                    preference.setLeaveDayColor(color);
                    viewModel.savePreference(preference);
                    refreshColorSelection(leaveColorViews, color);
                });
        binding.btnResetLeaveColor.setOnClickListener(v -> {
            preference.setLeaveDayColor(DEFAULT_LEAVE_COLOR);
            viewModel.savePreference(preference);
            refreshColorSelection(leaveColorViews, DEFAULT_LEAVE_COLOR);
            Toast.makeText(requireContext(), "已恢复默认", Toast.LENGTH_SHORT).show();
        });

        bindColorRow(binding.colorRowLate, lateColorViews, preference.getLateDayColor(), DEFAULT_LATE_COLOR,
                color -> {
                    preference.setLateDayColor(color);
                    viewModel.savePreference(preference);
                    refreshColorSelection(lateColorViews, color);
                });
        binding.btnResetLateColor.setOnClickListener(v -> {
            preference.setLateDayColor(DEFAULT_LATE_COLOR);
            viewModel.savePreference(preference);
            refreshColorSelection(lateColorViews, DEFAULT_LATE_COLOR);
            Toast.makeText(requireContext(), "已恢复默认", Toast.LENGTH_SHORT).show();
        });

        // P1 placeholders (Data & Salary)
        bindP1Placeholder(binding.itemHourlyRate, "💰", "时薪设置",
                preference.getHourlyRate() != null
                        ? String.format(Locale.getDefault(), "¥%.0f/时", preference.getHourlyRate()) : "未设置");
        bindP1Placeholder(binding.itemOvertimeMultiplier, "⚡", "加班倍数",
                String.format(Locale.getDefault(), "%.1f 倍", preference.getOvertimeMultiplier()));
        bindP1Placeholder(binding.itemExportJson, "💾", "导入 / 导出 JSON", "");
        bindP1Placeholder(binding.itemClearData, "🗑️", "清空当月数据", "");

        // Dark Mode (P1 placeholder)
        binding.btnDarkMode.setAlpha(0.5f);
        binding.btnDarkMode.setClickable(false);
    }

    private void bindP1Placeholder(com.toilernote.databinding.ItemSettingBinding itemBinding, String icon, String label, String value) {
        itemBinding.tvSettingIcon.setText(icon);
        itemBinding.tvSettingLabel.setText(label);
        itemBinding.tvSettingValue.setText(value + (value.isEmpty() ? "" : " ›"));
        itemBinding.tvSettingDesc.setVisibility(View.GONE);
        itemBinding.getRoot().setAlpha(0.5f);
        itemBinding.getRoot().setClickable(false);
    }

    private void showMaterialTimePicker(String title, String currentTime, Consumer<String> onConfirm) {
        int hour = 9, minute = 0;
        if (currentTime != null && currentTime.contains(":")) {
            String[] parts = currentTime.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(title)
                .build();

        picker.show(getParentFragmentManager(), "time_picker");
        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", picker.getHour(), picker.getMinute());
            onConfirm.accept(time);
        });
    }

    private void showWorkDaysPicker() {
        String[] dayLabels = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        boolean[] checked = new boolean[7];
        Set<Integer> workDays = parseWorkDays(preference.getWorkWeekDays());
        for (int day : workDays) {
            if (day >= 0 && day < 7) checked[day] = true;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("默认工作日")
                .setMultiChoiceItems(dayLabels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("确定", (dialog, which) -> {
                    List<String> selected = new ArrayList<>();
                    for (int i = 0; i < 7; i++) {
                        if (checked[i]) selected.add(String.valueOf(i));
                    }
                    String value = selected.isEmpty() ? "1,2,3,4,5" : String.join(",", selected);
                    preference.setWorkWeekDays(value);
                    binding.tvWorkWeekDaysValue.setText(formatWorkDays(value) + " ›");
                    viewModel.savePreference(preference);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private Set<Integer> parseWorkDays(String workWeekDays) {
        Set<Integer> set = new HashSet<>();
        if (workWeekDays == null || workWeekDays.isEmpty()) {
            set.add(1); set.add(2); set.add(3); set.add(4); set.add(5);
            return set;
        }
        String[] parts = workWeekDays.split(",");
        for (String p : parts) {
            try {
                set.add(Integer.parseInt(p.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return set;
    }

    private String formatWorkDays(String workWeekDays) {
        Set<Integer> days = parseWorkDays(workWeekDays);
        String[] labels = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        if (days.size() == 5 && days.contains(1) && days.contains(2) && days.contains(3) && days.contains(4) && days.contains(5)) {
            return "周一至周五";
        }
        if (days.size() == 7) {
            return "每天";
        }
        if (days.size() == 2 && days.contains(6) && days.contains(0)) {
            return "周末";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (days.contains(i)) {
                if (sb.length() > 0) sb.append("、");
                sb.append(labels[i]);
            }
        }
        return sb.length() > 0 ? sb.toString() : "未设置";
    }

    private void bindColorRow(LinearLayout container, List<View> colorViews, String currentColor, String defaultColor, Consumer<String> onSelect) {
        container.removeAllViews();
        colorViews.clear();
        String selectedColor = currentColor != null ? currentColor : defaultColor;

        for (String color : COLOR_PRESETS) {
            boolean isSelected = color.equalsIgnoreCase(selectedColor);
            View option = createColorOption(color, isSelected);
            option.setOnClickListener(v -> {
                onSelect.accept(color);
            });
            container.addView(option);
            colorViews.add(option);
        }
    }

    private View createColorOption(String colorHex, boolean isSelected) {
        FrameLayout container = new FrameLayout(requireContext());
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());
        int dotSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, 0, margin, 0);
        container.setLayoutParams(params);

        View dot = new View(requireContext());
        FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
        dotParams.gravity = Gravity.CENTER;
        dot.setLayoutParams(dotParams);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(colorHex));
        dot.setBackground(drawable);
        container.addView(dot);

        if (isSelected) {
            GradientDrawable ring = new GradientDrawable();
            ring.setShape(GradientDrawable.OVAL);
            ring.setColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
            ring.setStroke(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                    ContextCompat.getColor(requireContext(), R.color.primary));
            container.setBackground(ring);
        }

        return container;
    }

    private void refreshColorSelection(List<View> colorViews, String selectedColor) {
        for (int i = 0; i < colorViews.size(); i++) {
            View container = colorViews.get(i);
            String color = COLOR_PRESETS[i];
            if (color.equalsIgnoreCase(selectedColor)) {
                GradientDrawable ring = new GradientDrawable();
                ring.setShape(GradientDrawable.OVAL);
                ring.setColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
                ring.setStroke(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                        ContextCompat.getColor(requireContext(), R.color.primary));
                container.setBackground(ring);
            } else {
                container.setBackground(null);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}