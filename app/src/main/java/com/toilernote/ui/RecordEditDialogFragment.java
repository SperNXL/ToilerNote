package com.toilernote.ui;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.toilernote.R;
import com.toilernote.database.AppDatabase;
import com.toilernote.databinding.DialogRecordEditBinding;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.utils.TimeUtils;
import com.toilernote.utils.WorkHoursCalculator;


import java.util.Calendar;
import java.util.Locale;

public class RecordEditDialogFragment extends DialogFragment {

    private static final String ARG_DATE = "date";
    private DialogRecordEditBinding binding;
    private String date;
    private UserPreference preference;
    private DailyRecord existingRecord;
    private String currentStatus = "WORK";

    public static RecordEditDialogFragment newInstance(String date) {
        RecordEditDialogFragment fragment = new RecordEditDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_ToilerNote_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogRecordEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        date = getArguments() != null ? getArguments().getString(ARG_DATE) : "";

        // 使用 Window Insets API 处理键盘弹出（兼容 targetSdk 36）
        int topPadding = view.getPaddingTop(); // 保留布局自身的 paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            // 键盘高度作为底部 padding，让整个页面上移
            v.setPadding(0, topPadding, 0, imeInsets.bottom);
            return windowInsets;
        });

        // Load title
        Calendar cal = Calendar.getInstance();
        try {
            String[] parts = date.split("-");
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            cal.set(Integer.parseInt(parts[0]), month - 1, day);
        } catch (Exception ignored) {
        }
        String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};
        String title = String.format(Locale.getDefault(), "%d月%d日 周%s",
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
                weekdays[cal.get(Calendar.DAY_OF_WEEK) - 1]);
        binding.tvSheetTitle.setText(title);

        binding.btnCloseSheet.setOnClickListener(v -> dismiss());

        // Load preference and record
        loadData();

        // Status tabs
        binding.tabWork.setOnClickListener(v -> setStatus("WORK"));
        binding.tabRest.setOnClickListener(v -> setStatus("REST"));
        binding.tabLeave.setOnClickListener(v -> setStatus("LEAVE"));

        // Custom planned time switch
        binding.switchCustomPlannedTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePlannedTimeVisibility(isChecked);
            if (!isChecked) {
                resetPlannedTimeToDefaults();
            }
            updateBreakSummary(true);
            updateBreakSummary(false);
        });

        // Full day overtime switch
        binding.switchFullDayOvertime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 全天加班不扣晚休，但由 WorkHoursCalculator 处理；UI 上无需隐藏
            updateBreakSummary(false);
        });

        // Full day leave switch
        binding.switchFullDayLeave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateLeaveFormVisibility();
        });

        // Quick buttons
        binding.chipLate5.setOnClickListener(v -> addTime(binding.etActualStart, 5));
        binding.chipLate10.setOnClickListener(v -> addTime(binding.etActualStart, 10));
        binding.chipLate30.setOnClickListener(v -> addTime(binding.etActualStart, 30));
        binding.chipOt1.setOnClickListener(v -> addTime(binding.etActualEnd, 60));
        binding.chipOt2.setOnClickListener(v -> addTime(binding.etActualEnd, 120));
        binding.chipOt3.setOnClickListener(v -> addTime(binding.etActualEnd, 180));

        // Time pickers
        binding.etPlannedStart.setOnClickListener(v -> showTimePicker(binding.etPlannedStart, "计划上班时间"));
        binding.etActualStart.setOnClickListener(v -> showTimePicker(binding.etActualStart, "实际上班时间"));
        binding.etPlannedEnd.setOnClickListener(v -> showTimePicker(binding.etPlannedEnd, "计划下班时间"));
        binding.etActualEnd.setOnClickListener(v -> showTimePicker(binding.etActualEnd, "实际下班时间"));
        binding.etLeaveStart.setOnClickListener(v -> showTimePicker(binding.etLeaveStart, "请假开始时间"));
        binding.etLeaveEnd.setOnClickListener(v -> showTimePicker(binding.etLeaveEnd, "请假结束时间"));

        TextWatcher plannedTimeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateBreakSummary(true);
                updateBreakSummary(false);
            }
        };
        binding.etPlannedStart.addTextChangedListener(plannedTimeWatcher);
        binding.etPlannedEnd.addTextChangedListener(plannedTimeWatcher);

        binding.etMidBreakStart.setOnClickListener(v -> showTimePicker(binding.etMidBreakStart, "中间休息开始"));
        binding.etMidBreakEnd.setOnClickListener(v -> showTimePicker(binding.etMidBreakEnd, "中间休息结束"));
        binding.etNightBreakStart.setOnClickListener(v -> showTimePicker(binding.etNightBreakStart, "晚上休息开始"));
        binding.etNightBreakEnd.setOnClickListener(v -> showTimePicker(binding.etNightBreakEnd, "晚上休息结束"));

        // Custom break switches
        binding.switchCustomMidBreak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCustomBreakUi(true, isChecked);
        });
        binding.switchCustomNightBreak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCustomBreakUi(false, isChecked);
        });

        // Actions
        binding.btnCopyYesterday.setOnClickListener(v -> copyYesterday());
        binding.btnSave.setOnClickListener(v -> save(false));
//        binding.btnSaveAndContinue.setOnClickListener(v -> save(true));

        // 对会弹出键盘的输入框设置焦点监听，自动滚动到可见位置
        View.OnFocusChangeListener scrollOnFocus = (v, hasFocus) -> {
            if (hasFocus) {
                binding.scrollContent.postDelayed(() -> {
                    if (binding == null) return;
                    Rect focusRect = new Rect();
                    v.getDrawingRect(focusRect);
                    binding.scrollContent.offsetDescendantRectToMyCoords(v, focusRect);
                    binding.scrollContent.requestChildRectangleOnScreen(
                            binding.scrollContent.getChildAt(0), focusRect, false);
                }, 300);
            }
        };
        binding.etRemark.setOnFocusChangeListener(scrollOnFocus);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setWindowAnimations(R.style.DialogAnimation);
            }
            // 确保 Window Insets 被分发
            ViewCompat.requestApplyInsets(dialog.getWindow().getDecorView());
        }
    }

    private void loadData() {
        new Thread(() -> {
            preference = AppDatabase.getInstance(requireContext()).userPreferenceDao().getPreference();
            if (preference == null) {
                preference = new UserPreference();
            }
            existingRecord = AppDatabase.getInstance(requireContext()).dailyRecordDao().getRecordByDate(date);

            requireActivity().runOnUiThread(() -> {
                if (existingRecord != null) {
                    bindRecord(existingRecord);
                } else {
                    bindDefaults();
                }
            });
        }).start();
    }

    private void bindDefaults() {
        if (preference == null) return;
        binding.switchCustomPlannedTime.setChecked(false);
        binding.etPlannedStart.setText(preference.getDefaultWorkStart());
        binding.etPlannedEnd.setText(preference.getDefaultWorkEnd());
        updatePlannedTimeVisibility(false);
        binding.etActualStart.setText(preference.getDefaultWorkStart());
        binding.etActualEnd.setText(preference.getDefaultWorkEnd());
        binding.switchCustomMidBreak.setChecked(false);
        binding.switchCustomNightBreak.setChecked(false);
        resetBreakPickersToDefaults();
        updateCustomBreakUi(true, false);
        updateCustomBreakUi(false, false);
        binding.switchFullDayOvertime.setChecked(false);
        binding.switchFullDayLeave.setChecked(true);
        binding.etRemark.setText("");
        setStatus("WORK");
    }

    private void bindRecord(DailyRecord record) {
        boolean customPlanned = shouldUseCustomPlannedTime(record);
        binding.switchCustomPlannedTime.setChecked(customPlanned);
        binding.etPlannedStart.setText(customPlanned && record.getPlannedStart() != null
                ? record.getPlannedStart() : preference.getDefaultWorkStart());
        binding.etPlannedEnd.setText(customPlanned && record.getPlannedEnd() != null
                ? record.getPlannedEnd() : preference.getDefaultWorkEnd());
        updatePlannedTimeVisibility(customPlanned);
        binding.etActualStart.setText(record.getActualStart() != null ? record.getActualStart() : preference.getDefaultWorkStart());
        binding.etActualEnd.setText(record.getActualEnd() != null ? record.getActualEnd() : preference.getDefaultWorkEnd());
        boolean customMidBreak = shouldUseCustomMidBreak(record);
        boolean customNightBreak = shouldUseCustomNightBreak(record);
        binding.switchCustomMidBreak.setChecked(customMidBreak);
        binding.switchCustomNightBreak.setChecked(customNightBreak);

        int plannedStartMin = TimeUtils.timeToMinutes(getEffectivePlannedStart());
        int plannedEndMin = TimeUtils.timeToMinutes(getEffectivePlannedEnd());
        String[] midRange = resolveBreakRangeForBinding(record.isCustomMidBreak(), record.getMidBreakStart(),
                record.getMidBreakEnd(), record.getMidBreakMinutes(), plannedStartMin, plannedEndMin,
                preference.getMidBreakStart(), preference.getMidBreakEnd());
        String[] nightRange = resolveBreakRangeForBinding(record.isCustomNightBreak(), record.getNightBreakStart(),
                record.getNightBreakEnd(), record.getNightBreakMinutes(), plannedStartMin, plannedEndMin,
                preference.getNightBreakStart(), preference.getNightBreakEnd());
        binding.etMidBreakStart.setText(midRange[0]);
        binding.etMidBreakEnd.setText(midRange[1]);
        binding.etNightBreakStart.setText(nightRange[0]);
        binding.etNightBreakEnd.setText(nightRange[1]);
        updateCustomBreakUi(true, customMidBreak);
        updateCustomBreakUi(false, customNightBreak);
        binding.switchFullDayOvertime.setChecked(record.isFullDayOvertime());
        binding.switchFullDayLeave.setChecked(record.isFullDayLeave());
        binding.etRemark.setText(record.getRemark() != null ? record.getRemark() : "");
        if (record.getLeaveStart() != null) binding.etLeaveStart.setText(record.getLeaveStart());
        if (record.getLeaveEnd() != null) binding.etLeaveEnd.setText(record.getLeaveEnd());
        setStatus(record.getStatus());
    }

    private void setStatus(String status) {
        currentStatus = status;
        binding.tabWork.setStrokeColor(ColorStateList.valueOf(Color.parseColor("WORK".equals(status) ? "#6366F1" : "#E2E8F0")));
        binding.tabRest.setStrokeColor(ColorStateList.valueOf(Color.parseColor("REST".equals(status) ? "#6366F1" : "#E2E8F0")));
        binding.tabLeave.setStrokeColor(ColorStateList.valueOf(Color.parseColor("LEAVE".equals(status) ? "#6366F1" : "#E2E8F0")));

        binding.tabWork.setBackgroundColor(Color.parseColor("WORK".equals(status) ? "#EEF2FF" : "#FFFFFF"));
        binding.tabRest.setBackgroundColor(Color.parseColor("REST".equals(status) ? "#EEF2FF" : "#FFFFFF"));
        binding.tabLeave.setBackgroundColor(Color.parseColor("LEAVE".equals(status) ? "#EEF2FF" : "#FFFFFF"));

        if ("REST".equals(status)) {
            binding.workFormContainer.setVisibility(View.GONE);
            binding.fullDayLeaveContainer.setVisibility(View.GONE);
            binding.leaveDivider.setVisibility(View.GONE);
        } else if ("LEAVE".equals(status)) {
            binding.workFormContainer.setVisibility(View.VISIBLE);
            binding.fullDayLeaveContainer.setVisibility(View.VISIBLE);
            binding.leaveDivider.setVisibility(View.VISIBLE);
            updateLeaveFormVisibility();
        } else {
            binding.workFormContainer.setVisibility(View.VISIBLE);
            binding.fullDayLeaveContainer.setVisibility(View.GONE);
            binding.leaveDivider.setVisibility(View.GONE);
            binding.leaveTimeContainer.setVisibility(View.GONE);
        }
    }

    private void updateLeaveFormVisibility() {
        if (binding.switchFullDayLeave.isChecked()) {
            binding.workFormContainer.setVisibility(View.GONE);
        } else {
            binding.workFormContainer.setVisibility(View.VISIBLE);
            binding.leaveTimeContainer.setVisibility(View.VISIBLE);
        }
    }

    private void updatePlannedTimeVisibility(boolean showCustom) {
        binding.tilPlannedStart.setVisibility(showCustom ? View.VISIBLE : View.GONE);
        binding.tilPlannedEnd.setVisibility(showCustom ? View.VISIBLE : View.GONE);
    }

    private void resetPlannedTimeToDefaults() {
        if (preference == null) return;
        binding.etPlannedStart.setText(preference.getDefaultWorkStart());
        binding.etPlannedEnd.setText(preference.getDefaultWorkEnd());
    }

    private boolean shouldUseCustomPlannedTime(DailyRecord record) {
        if (record == null) return false;
        // New field takes precedence
        if (record.isCustomPlannedTime()) return true;
        // Fallback for legacy records: treat non-default values as custom
        String defaultStart = preference != null ? preference.getDefaultWorkStart() : null;
        String defaultEnd = preference != null ? preference.getDefaultWorkEnd() : null;
        boolean hasCustomStart = record.getPlannedStart() != null
                && defaultStart != null
                && !record.getPlannedStart().equals(defaultStart);
        boolean hasCustomEnd = record.getPlannedEnd() != null
                && defaultEnd != null
                && !record.getPlannedEnd().equals(defaultEnd);
        return hasCustomStart || hasCustomEnd;
    }

    private boolean shouldUseCustomMidBreak(DailyRecord record) {
        if (record == null) return false;
        if (record.isCustomMidBreak()) return true;
        return record.getMidBreakMinutes() > 0;
    }

    private boolean shouldUseCustomNightBreak(DailyRecord record) {
        if (record == null) return false;
        if (record.isCustomNightBreak()) return true;
        return record.getNightBreakMinutes() > 0;
    }

    private void resetBreakPickersToDefaults() {
        if (preference == null) return;
        binding.etMidBreakStart.setText(preference.getMidBreakStart());
        binding.etMidBreakEnd.setText(preference.getMidBreakEnd());
        binding.etNightBreakStart.setText(preference.getNightBreakStart());
        binding.etNightBreakEnd.setText(preference.getNightBreakEnd());
    }

    private void updateCustomBreakUi(boolean isMid, boolean showCustom) {
        if (isMid) {
            binding.containerMidBreakPickers.setVisibility(showCustom ? View.VISIBLE : View.GONE);
        } else {
            binding.containerNightBreakPickers.setVisibility(showCustom ? View.VISIBLE : View.GONE);
        }
        updateBreakSummary(isMid);
    }

    private void updateBreakSummary(boolean isMid) {
        if (preference == null) return;
        TextView summaryView = isMid ? binding.tvMidBreakSummary : binding.tvNightBreakSummary;
        SwitchMaterial switchView = isMid ? binding.switchCustomMidBreak : binding.switchCustomNightBreak;
        if (switchView.isChecked()) {
            summaryView.setVisibility(View.GONE);
            return;
        }

        int plannedStartMin = TimeUtils.timeToMinutes(getEffectivePlannedStart());
        int plannedEndMin = TimeUtils.timeToMinutes(getEffectivePlannedEnd());
        DailyRecord temp = new DailyRecord();
        temp.setCustomPlannedTime(binding.switchCustomPlannedTime.isChecked());
        temp.setPlannedStart(binding.etPlannedStart.getText().toString());
        temp.setPlannedEnd(binding.etPlannedEnd.getText().toString());

        WorkHoursCalculator.BreakInfo info;
        if (isMid) {
            temp.setCustomMidBreak(false);
            temp.setMidBreakMinutes(0);
            info = WorkHoursCalculator.resolveMidBreak(temp, preference, plannedStartMin, plannedEndMin);
        } else {
            temp.setCustomNightBreak(false);
            temp.setNightBreakMinutes(0);
            info = WorkHoursCalculator.resolveNightBreak(temp, preference, plannedStartMin, plannedEndMin,
                    binding.switchFullDayOvertime.isChecked());
        }

        if (info.duration <= 0) {
            summaryView.setText(R.string.break_time_none);
        } else {
            String range = String.format(Locale.getDefault(), "%s-%s",
                    TimeUtils.minutesToTime(info.start), TimeUtils.minutesToTime(info.end));
            summaryView.setText(getString(R.string.break_time_summary, range));
        }
        summaryView.setVisibility(View.VISIBLE);
    }

    private String[] resolveBreakRangeForBinding(boolean isCustom, String recordStart, String recordEnd,
                                                  int legacyMinutes, int plannedStartMin, int plannedEndMin,
                                                  String prefStart, String prefEnd) {
        if (isCustom && recordStart != null && recordEnd != null) {
            return new String[]{recordStart, recordEnd};
        }
        if (legacyMinutes > 0) {
            int center = plannedStartMin + (plannedEndMin - plannedStartMin) / 2;
            int start = center - legacyMinutes / 2;
            int end = start + legacyMinutes;
            return new String[]{TimeUtils.minutesToTime(start), TimeUtils.minutesToTime(end)};
        }
        return new String[]{prefStart, prefEnd};
    }

    private String getEffectivePlannedStart() {
        String text = binding.etPlannedStart.getText().toString();
        return !text.isEmpty() ? text : (preference != null ? preference.getDefaultWorkStart() : null);
    }

    private String getEffectivePlannedEnd() {
        String text = binding.etPlannedEnd.getText().toString();
        return !text.isEmpty() ? text : (preference != null ? preference.getDefaultWorkEnd() : null);
    }

    private void addTime(android.widget.EditText editText, int minutes) {
        String text = editText.getText().toString();
        int total = TimeUtils.timeToMinutes(text) + minutes;
        editText.setText(TimeUtils.minutesToTime(total));
    }

    private void showTimePicker(android.widget.EditText editText, String title) {
        int hour = 9;
        int minute = 0;
        String text = editText.getText().toString();
        if (text != null && !text.isEmpty()) {
            String[] parts = text.split(":");
            if (parts.length == 2) {
                try {
                    hour = Integer.parseInt(parts[0]);
                    minute = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
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
            editText.setText(time);
        });
    }

    private void copyYesterday() {
        String yesterday = TimeUtils.getYesterday(date);
        if (yesterday == null) return;
        new Thread(() -> {
            DailyRecord yesterdayRecord = AppDatabase.getInstance(requireContext()).dailyRecordDao().getRecordByDate(yesterday);
            requireActivity().runOnUiThread(() -> {
                if (yesterdayRecord != null) {
                    binding.etActualStart.setText(yesterdayRecord.getActualStart());
                    binding.etActualEnd.setText(yesterdayRecord.getActualEnd());

                    boolean customMid = shouldUseCustomMidBreak(yesterdayRecord);
                    boolean customNight = shouldUseCustomNightBreak(yesterdayRecord);
                    binding.switchCustomMidBreak.setChecked(customMid);
                    binding.switchCustomNightBreak.setChecked(customNight);

                    int yesterdayPlannedStartMin = TimeUtils.timeToMinutes(
                            yesterdayRecord.getPlannedStart() != null ? yesterdayRecord.getPlannedStart() : preference.getDefaultWorkStart());
                    int yesterdayPlannedEndMin = TimeUtils.timeToMinutes(
                            yesterdayRecord.getPlannedEnd() != null ? yesterdayRecord.getPlannedEnd() : preference.getDefaultWorkEnd());
                    String[] yesterdayMidRange = resolveBreakRangeForBinding(yesterdayRecord.isCustomMidBreak(),
                            yesterdayRecord.getMidBreakStart(), yesterdayRecord.getMidBreakEnd(),
                            yesterdayRecord.getMidBreakMinutes(), yesterdayPlannedStartMin, yesterdayPlannedEndMin,
                            preference.getMidBreakStart(), preference.getMidBreakEnd());
                    String[] yesterdayNightRange = resolveBreakRangeForBinding(yesterdayRecord.isCustomNightBreak(),
                            yesterdayRecord.getNightBreakStart(), yesterdayRecord.getNightBreakEnd(),
                            yesterdayRecord.getNightBreakMinutes(), yesterdayPlannedStartMin, yesterdayPlannedEndMin,
                            preference.getNightBreakStart(), preference.getNightBreakEnd());
                    binding.etMidBreakStart.setText(yesterdayMidRange[0]);
                    binding.etMidBreakEnd.setText(yesterdayMidRange[1]);
                    binding.etNightBreakStart.setText(yesterdayNightRange[0]);
                    binding.etNightBreakEnd.setText(yesterdayNightRange[1]);
                    updateCustomBreakUi(true, customMid);
                    updateCustomBreakUi(false, customNight);

                    binding.switchFullDayOvertime.setChecked(yesterdayRecord.isFullDayOvertime());
                    binding.etRemark.setText("");
                    setStatus(yesterdayRecord.getStatus());
                }
            });
        }).start();
    }

    private void save(boolean andContinue) {
        DailyRecord record = existingRecord != null ? existingRecord : new DailyRecord(date, currentStatus);
        record.setDate(date);
        record.setStatus(currentStatus);
        boolean customPlanned = binding.switchCustomPlannedTime.isChecked();
        record.setCustomPlannedTime(customPlanned);
        record.setPlannedStart(customPlanned ? binding.etPlannedStart.getText().toString() : null);
        record.setPlannedEnd(customPlanned ? binding.etPlannedEnd.getText().toString() : null);
        record.setActualStart(binding.etActualStart.getText().toString());
        record.setActualEnd(binding.etActualEnd.getText().toString());
        boolean customMidBreak = binding.switchCustomMidBreak.isChecked();
        boolean customNightBreak = binding.switchCustomNightBreak.isChecked();
        record.setCustomMidBreak(customMidBreak);
        record.setMidBreakStart(customMidBreak ? binding.etMidBreakStart.getText().toString() : null);
        record.setMidBreakEnd(customMidBreak ? binding.etMidBreakEnd.getText().toString() : null);
        record.setCustomNightBreak(customNightBreak);
        record.setNightBreakStart(customNightBreak ? binding.etNightBreakStart.getText().toString() : null);
        record.setNightBreakEnd(customNightBreak ? binding.etNightBreakEnd.getText().toString() : null);
        record.setFullDayOvertime(binding.switchFullDayOvertime.isChecked());
        record.setFullDayLeave(binding.switchFullDayLeave.isChecked());
        record.setRemark(binding.etRemark.getText().toString());

        if ("LEAVE".equals(currentStatus)) {
            record.setLeaveStart(binding.etLeaveStart.getText().toString());
            record.setLeaveEnd(binding.etLeaveEnd.getText().toString());
        } else {
            record.setLeaveStart(null);
            record.setLeaveEnd(null);
        }

        if (preference != null && !"REST".equals(currentStatus)) {
            WorkHoursCalculator.calculate(record, preference);
        }

        new Thread(() -> {
            AppDatabase.getInstance(requireContext()).dailyRecordDao().insert(record);
            requireActivity().runOnUiThread(() -> {
                if (andContinue) {
                    moveToNextDay();
                } else {
                    dismiss();
                }
            });
        }).start();
    }

    private void moveToNextDay() {
        try {
            String[] parts = date.split("-");
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            cal.add(Calendar.DAY_OF_MONTH, 1);
            date = String.format(Locale.getDefault(), "%d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
            existingRecord = null;
            loadData();
        } catch (Exception e) {
            dismiss();
        }
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}