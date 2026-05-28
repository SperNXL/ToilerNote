package com.toilernote.ui;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.toilernote.databinding.FragmentSettingsBinding;
import com.toilernote.entity.UserPreference;
import com.toilernote.viewmodel.SettingsViewModel;

import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private UserPreference preference;

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

        binding.btnDarkMode.setOnClickListener(v -> toggleDarkMode());
    }

    private void bindSettings() {
        // Nickname
        binding.tvNickname.setText(preference.getNickname());
        binding.tvNickname.setOnClickListener(v -> showEditTextDialog("修改昵称", preference.getNickname(), text -> {
            preference.setNickname(text);
            viewModel.savePreference(preference);
        }));

        // Default times
        bindSettingItem(binding.itemWorkStart.tvSettingIcon, binding.itemWorkStart.tvSettingLabel, binding.itemWorkStart.tvSettingValue,
                "🕘", "默认上班时间", preference.getDefaultWorkStart());
        binding.itemWorkStart.getRoot().setOnClickListener(v ->
                showTimePicker("上班时间", preference.getDefaultWorkStart(), time -> {
                    preference.setDefaultWorkStart(time);
                    viewModel.savePreference(preference);
                }));

        bindSettingItem(binding.itemWorkEnd.tvSettingIcon, binding.itemWorkEnd.tvSettingLabel, binding.itemWorkEnd.tvSettingValue,
                "🕕", "默认下班时间", preference.getDefaultWorkEnd());
        binding.itemWorkEnd.getRoot().setOnClickListener(v ->
                showTimePicker("下班时间", preference.getDefaultWorkEnd(), time -> {
                    preference.setDefaultWorkEnd(time);
                    viewModel.savePreference(preference);
                }));

        bindSettingItem(binding.itemMidBreak.tvSettingIcon, binding.itemMidBreak.tvSettingLabel, binding.itemMidBreak.tvSettingValue,
                "☕", "中间休息时间", preference.getDefaultMidBreak());
        binding.itemMidBreak.getRoot().setOnClickListener(v ->
                showEditTextDialog("中间休息时间", preference.getDefaultMidBreak(), text -> {
                    preference.setDefaultMidBreak(text);
                    viewModel.savePreference(preference);
                }));

        bindSettingItem(binding.itemNightBreak.tvSettingIcon, binding.itemNightBreak.tvSettingLabel, binding.itemNightBreak.tvSettingValue,
                "🌙", "晚上休息时间", preference.getDefaultNightBreak());
        binding.itemNightBreak.getRoot().setOnClickListener(v ->
                showEditTextDialog("晚上休息时间", preference.getDefaultNightBreak(), text -> {
                    preference.setDefaultNightBreak(text);
                    viewModel.savePreference(preference);
                }));

        bindSettingItem(binding.itemWorkWeekDays.tvSettingIcon, binding.itemWorkWeekDays.tvSettingLabel, binding.itemWorkWeekDays.tvSettingValue,
                "📅", "默认工作日", preference.getWorkWeekDays());
        binding.itemWorkWeekDays.getRoot().setOnClickListener(v ->
                showEditTextDialog("默认工作日（0=周日,1=周一...）", preference.getWorkWeekDays(), text -> {
                    preference.setWorkWeekDays(text);
                    viewModel.savePreference(preference);
                }));

        // Colors
        bindColorItem(binding.itemColorWork.tvSettingIcon, binding.itemColorWork.tvSettingLabel, binding.itemColorWork.colorDot,
                "💼", "上班标注颜色", preference.getWorkDayColor());
        binding.itemColorWork.getRoot().setOnClickListener(v -> showColorPicker("上班标注颜色", preference.getWorkDayColor(), color -> {
            preference.setWorkDayColor(color);
            viewModel.savePreference(preference);
        }));

        bindColorItem(binding.itemColorRest.tvSettingIcon, binding.itemColorRest.tvSettingLabel, binding.itemColorRest.colorDot,
                "🏖️", "休息标注颜色", preference.getRestDayColor());
        binding.itemColorRest.getRoot().setOnClickListener(v -> showColorPicker("休息标注颜色", preference.getRestDayColor(), color -> {
            preference.setRestDayColor(color);
            viewModel.savePreference(preference);
        }));

        bindColorItem(binding.itemColorLeave.tvSettingIcon, binding.itemColorLeave.tvSettingLabel, binding.itemColorLeave.colorDot,
                "🏥", "请假标注颜色", preference.getLeaveDayColor());
        binding.itemColorLeave.getRoot().setOnClickListener(v -> showColorPicker("请假标注颜色", preference.getLeaveDayColor(), color -> {
            preference.setLeaveDayColor(color);
            viewModel.savePreference(preference);
        }));

        bindColorItem(binding.itemColorLate.tvSettingIcon, binding.itemColorLate.tvSettingLabel, binding.itemColorLate.colorDot,
                "⏰", "迟到标注颜色", preference.getLateDayColor());
        binding.itemColorLate.getRoot().setOnClickListener(v -> showColorPicker("迟到标注颜色", preference.getLateDayColor(), color -> {
            preference.setLateDayColor(color);
            viewModel.savePreference(preference);
        }));

        // Data & Salary
        String hourlyRate = preference.getHourlyRate() != null
                ? String.format(Locale.getDefault(), "¥%.0f/时", preference.getHourlyRate()) : "未设置";
        bindSettingItem(binding.itemHourlyRate.tvSettingIcon, binding.itemHourlyRate.tvSettingLabel, binding.itemHourlyRate.tvSettingValue,
                "💰", "时薪设置", hourlyRate);
        binding.itemHourlyRate.getRoot().setOnClickListener(v -> {
            String current = preference.getHourlyRate() != null ? String.valueOf(preference.getHourlyRate()) : "";
            showEditTextDialog("时薪设置", current, text -> {
                try {
                    preference.setHourlyRate(Double.parseDouble(text));
                    viewModel.savePreference(preference);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "请输入有效数字", Toast.LENGTH_SHORT).show();
                }
            });
        });

        bindSettingItem(binding.itemOvertimeMultiplier.tvSettingIcon, binding.itemOvertimeMultiplier.tvSettingLabel, binding.itemOvertimeMultiplier.tvSettingValue,
                "⚡", "加班倍数", String.format(Locale.getDefault(), "%.1f 倍", preference.getOvertimeMultiplier()));
        binding.itemOvertimeMultiplier.getRoot().setOnClickListener(v -> {
            showEditTextDialog("加班倍数", String.valueOf(preference.getOvertimeMultiplier()), text -> {
                try {
                    preference.setOvertimeMultiplier(Double.parseDouble(text));
                    viewModel.savePreference(preference);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "请输入有效数字", Toast.LENGTH_SHORT).show();
                }
            });
        });

        bindSettingItem(binding.itemExportJson.tvSettingIcon, binding.itemExportJson.tvSettingLabel, binding.itemExportJson.tvSettingValue,
                "💾", "导入 / 导出 JSON", "");
        binding.itemExportJson.getRoot().setOnClickListener(v -> {
            Toast.makeText(requireContext(), "导出功能开发中", Toast.LENGTH_SHORT).show();
        });

        bindSettingItem(binding.itemClearData.tvSettingIcon, binding.itemClearData.tvSettingLabel, binding.itemClearData.tvSettingValue,
                "🗑️", "清空当月数据", "");
        binding.itemClearData.getRoot().setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("确认清空")
                    .setMessage("确定要清空当月所有考勤数据吗？此操作不可撤销。")
                    .setPositiveButton("清空", (dialog, which) -> {
                        Calendar cal = Calendar.getInstance();
                        viewModel.clearCurrentMonthData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
                        Toast.makeText(requireContext(), "已清空当月数据", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void bindSettingItem(TextView tvIcon, TextView tvLabel, TextView tvValue, String icon, String label, String value) {
        tvIcon.setText(icon);
        tvLabel.setText(label);
        tvValue.setText(value + (value.isEmpty() ? "" : " ›"));
    }

    private void bindColorItem(TextView tvIcon, TextView tvLabel, View colorDot, String icon, String label, String color) {
        tvIcon.setText(icon);
        tvLabel.setText(label);
        try {
            colorDot.setBackgroundColor(Color.parseColor(color));
        } catch (Exception e) {
            colorDot.setBackgroundColor(Color.GRAY);
        }
    }

    private void showTimePicker(String title, String currentTime, TimeCallback callback) {
        int hour = 9, minute = 0;
        if (currentTime != null && currentTime.contains(":")) {
            String[] parts = currentTime.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
            callback.onTimeSelected(time);
        }, hour, minute, true).show();
    }

    private void showEditTextDialog(String title, String currentValue, TextCallback callback) {
        EditText editText = new EditText(requireContext());
        editText.setText(currentValue);
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(editText)
                .setPositiveButton("确定", (dialog, which) -> callback.onTextEntered(editText.getText().toString()))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showColorPicker(String title, String currentColor, ColorCallback callback) {
        String[] presets = {"#2196F3", "#1976D2", "#0D47A1", "#4CAF50", "#009688", "#98FB98",
                "#FFEB3B", "#FFC107", "#FF9800", "#F44336", "#E91E63", "#B71C1C"};
        String[] names = {"蓝", "靛蓝", "深蓝", "绿", "青绿", "薄荷绿",
                "黄", "琥珀", "橙黄", "红", "玫红", "深红"};

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        for (int i = 0; i < presets.length; i++) {
            TextView tv = new TextView(requireContext());
            tv.setText("● " + names[i]);
            tv.setTextSize(16);
            tv.setPadding(16, 16, 16, 16);
            try {
                tv.setTextColor(Color.parseColor(presets[i]));
            } catch (Exception e) {
                tv.setTextColor(Color.BLACK);
            }
            final String color = presets[i];
            tv.setOnClickListener(v -> callback.onColorSelected(color));
            layout.addView(tv);
        }

        EditText customColor = new EditText(requireContext());
        customColor.setHint("自定义 HEX 颜色（如 #FF0000）");
        customColor.setText(currentColor);
        layout.addView(customColor);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    String color = customColor.getText().toString();
                    if (color.matches("#[0-9A-Fa-f]{6}")) {
                        callback.onColorSelected(color);
                    } else if (!color.isEmpty()) {
                        Toast.makeText(requireContext(), "颜色格式错误", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void toggleDarkMode() {
        int currentMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    interface TimeCallback {
        void onTimeSelected(String time);
    }

    interface TextCallback {
        void onTextEntered(String text);
    }

    interface ColorCallback {
        void onColorSelected(String color);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
