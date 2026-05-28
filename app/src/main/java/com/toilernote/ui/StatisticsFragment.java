package com.toilernote.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.toilernote.databinding.FragmentStatisticsBinding;
import com.toilernote.entity.DailyRecord;
import com.toilernote.ui.view.BarChartView;
import com.toilernote.ui.view.LineChartView;
import com.toilernote.utils.TimeUtils;
import com.toilernote.viewmodel.StatisticsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private StatisticsViewModel viewModel;

    public StatisticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        viewModel.getMonthlyRecords(year, month).observe(getViewLifecycleOwner(), records -> {
            if (records != null) {
                updateCharts(records, year, month);
                updateInsights(records, year, month);
            }
        });

        viewModel.getUserPreference().observe(getViewLifecycleOwner(), pref -> {
            // Preference loaded
        });

        binding.btnExport.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "导出功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCharts(List<DailyRecord> records, int year, int month) {
        int daysInMonth = TimeUtils.getDaysInMonth(year, month);
        List<BarChartView.BarData> barData = new ArrayList<>();
        List<LineChartView.Point> workCumPoints = new ArrayList<>();
        List<LineChartView.Point> otCumPoints = new ArrayList<>();

        float cumWork = 0, cumOt = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            String date = TimeUtils.formatDate(year, month, day);
            DailyRecord record = null;
            for (DailyRecord r : records) {
                if (date.equals(r.getDate())) {
                    record = r;
                    break;
                }
            }

            if (record != null && "WORK".equals(record.getStatus())) {
                barData.add(new BarChartView.BarData(day, (float) record.getWorkHours(), (float) record.getOvertimeHours()));
                cumWork += record.getWorkHours();
                cumOt += record.getOvertimeHours();
            }
            workCumPoints.add(new LineChartView.Point(cumWork));
            otCumPoints.add(new LineChartView.Point(cumOt));
        }

        binding.barChart.setData(barData);
        binding.lineChart.setData(workCumPoints, otCumPoints);
    }

    private void updateInsights(List<DailyRecord> records, int year, int month) {
        int consecutive = viewModel.getConsecutiveWorkDays(records, year, month);
        double[] cumData = viewModel.getCumulativeData(records);
        double totalWork = cumData[0];
        double totalOt = cumData[1];

        StringBuilder insight = new StringBuilder();
        if (consecutive >= 6) {
            insight.append(String.format(Locale.getDefault(), "本月最长连续上班 %d 天，建议适当休息。", consecutive));
        } else {
            insight.append(String.format(Locale.getDefault(), "本月最长连续上班 %d 天。", consecutive));
        }

        if (totalWork > 0) {
            double otRatio = totalOt / totalWork * 100;
            insight.append(String.format(Locale.getDefault(), " 加班占比 %.1f%%", otRatio));
        }

        binding.tvInsightText.setText(insight.toString());

        // Compare with previous month
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        cal.add(Calendar.MONTH, -1);
        int prevYear = cal.get(Calendar.YEAR);
        int prevMonth = cal.get(Calendar.MONTH);

        viewModel.getMonthlyRecords(prevYear, prevMonth).observe(getViewLifecycleOwner(), prevRecords -> {
            if (prevRecords != null) {
                double[] prevData = viewModel.getCumulativeData(prevRecords);
                double prevWork = prevData[0];
                double prevOt = prevData[1];

                if (prevWork > 0) {
                    double workChange = (totalWork - prevWork) / prevWork * 100;
                    binding.tvCompareWorkHours.setText(String.format(Locale.getDefault(), "%+.0f%%", workChange));
                } else {
                    binding.tvCompareWorkHours.setText("--");
                }

                if (prevOt > 0) {
                    double otChange = (totalOt - prevOt) / prevOt * 100;
                    binding.tvCompareOvertime.setText(String.format(Locale.getDefault(), "%+.0f%%", otChange));
                } else {
                    binding.tvCompareOvertime.setText("--");
                }

                int lateCount = 0, prevLateCount = 0;
                for (DailyRecord r : records) {
                    if (r.isLate()) lateCount++;
                }
                for (DailyRecord r : prevRecords) {
                    if (r.isLate()) prevLateCount++;
                }
                if (lateCount == prevLateCount) {
                    binding.tvCompareLate.setText("持平");
                } else {
                    binding.tvCompareLate.setText(String.valueOf(lateCount));
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
