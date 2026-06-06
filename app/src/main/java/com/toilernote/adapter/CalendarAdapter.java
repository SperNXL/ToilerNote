package com.toilernote.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.toilernote.R;
import com.toilernote.entity.DailyRecord;
import com.toilernote.entity.UserPreference;
import com.toilernote.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(String date, boolean isCurrentMonth);
    }

    private final List<DayItem> dayItems = new ArrayList<>();
    private OnDayClickListener listener;
    private UserPreference preference;

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setPreference(UserPreference preference) {
        this.preference = preference;
        notifyDataSetChanged();
    }

    public void setData(List<DayItem> items) {
        dayItems.clear();
        dayItems.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_cell, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayItem item = dayItems.get(position);
        holder.tvDayNum.setText(String.valueOf(item.day));
        holder.tvDayInfo.setVisibility(View.GONE);
        holder.lateBadge.setVisibility(View.GONE);

        if (preference != null) {
            int workColor = Color.parseColor(preference.getWorkDayColor());
            int restColor = Color.parseColor(preference.getRestDayColor());
            int leaveColor = Color.parseColor(preference.getLeaveDayColor());
            int lateColor = Color.parseColor(preference.getLateDayColor());

            if (!item.isCurrentMonth) {
                holder.tvDayNum.setBackground(null);
                holder.tvDayNum.setTextColor(Color.parseColor("#94A3B8"));
                holder.itemView.setAlpha(0.3f);
            } else {
                holder.itemView.setAlpha(1f);
                if (item.record != null) {
                    String status = item.record.getStatus();
                    if ("WORK".equals(status)) {
                        holder.tvDayNum.setBackgroundResource(R.drawable.bg_circle_work);
                        holder.tvDayNum.getBackground().setTint(workColor);
                        holder.tvDayNum.setTextColor(Color.WHITE);
                        String info = String.format(java.util.Locale.getDefault(), "%.0fh+%.0fh",
                                item.record.getWorkHours(), item.record.getOvertimeHours());
                        holder.tvDayInfo.setText(info);
                        holder.tvDayInfo.setVisibility(View.VISIBLE);
                        if (item.record.isLate()) {
                            holder.lateBadge.setVisibility(View.VISIBLE);
                            holder.lateBadge.getBackground().setTint(lateColor);
                        }
                    } else if ("REST".equals(status)) {
                        holder.tvDayNum.setBackgroundResource(R.drawable.bg_circle_work);
                        holder.tvDayNum.getBackground().setTint(restColor);
                        holder.tvDayNum.setTextColor(Color.WHITE);
                        holder.tvDayInfo.setText("休");
                        holder.tvDayInfo.setVisibility(View.VISIBLE);
                    } else if ("LEAVE".equals(status)) {
                        if (item.record.isFullDayLeave()) {
                            holder.tvDayNum.setBackgroundResource(R.drawable.bg_circle_work);
                            holder.tvDayNum.getBackground().setTint(leaveColor);
                            holder.tvDayNum.setTextColor(Color.WHITE);
                            holder.tvDayInfo.setText("假");
                            holder.tvDayInfo.setVisibility(View.VISIBLE);
                        } else {
                            holder.tvDayNum.setBackgroundResource(R.drawable.bg_circle_work);
                            holder.tvDayNum.getBackground().setTint(workColor);
                            holder.tvDayNum.setTextColor(Color.WHITE);
                            String info = String.format(java.util.Locale.getDefault(), "%.0fh+%.0fh",
                                    item.record.getWorkHours(), item.record.getOvertimeHours());
                            holder.tvDayInfo.setText(info);
                            holder.tvDayInfo.setVisibility(View.VISIBLE);
                            if (item.record.isLate()) {
                                holder.lateBadge.setVisibility(View.VISIBLE);
                                holder.lateBadge.getBackground().setTint(lateColor);
                            }
                        }
                    }
                } else if (item.isRestDay) {
                    holder.tvDayNum.setBackgroundResource(R.drawable.bg_circle_work);
                    holder.tvDayNum.getBackground().setTint(restColor);
                    holder.tvDayNum.setTextColor(Color.WHITE);
                    holder.tvDayInfo.setText("休");
                    holder.tvDayInfo.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDayNum.setBackground(null);
                    holder.tvDayNum.setTextColor(Color.parseColor("#1E293B"));
                    if (item.isToday) {
                        holder.tvDayNum.setBackgroundResource(R.drawable.bg_circle_primary_light);
                        holder.tvDayNum.setTextColor(Color.parseColor("#6366F1"));
                    }
                }
            }
        } else {
            holder.tvDayNum.setBackground(null);
            holder.tvDayNum.setTextColor(Color.parseColor("#1E293B"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item.isCurrentMonth) {
                listener.onDayClick(item.date, item.isCurrentMonth);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayItems.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNum, tvDayInfo;
        View lateBadge;
        LinearLayout dayContainer;

        DayViewHolder(View itemView) {
            super(itemView);
            tvDayNum = itemView.findViewById(R.id.tvDayNum);
            tvDayInfo = itemView.findViewById(R.id.tvDayInfo);
            lateBadge = itemView.findViewById(R.id.lateBadge);
            dayContainer = itemView.findViewById(R.id.dayContainer);
        }
    }

    public static class DayItem {
        public int day;
        public String date;
        public boolean isCurrentMonth;
        public boolean isToday;
        public DailyRecord record;
        public boolean isRestDay;

        public DayItem(int day, String date, boolean isCurrentMonth, boolean isToday) {
            this.day = day;
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
            this.isToday = isToday;
        }
    }
}
