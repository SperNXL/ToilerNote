package com.toilernote.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        holder.tvDayInfo.setVisibility(View.INVISIBLE);
        holder.lateBadge.setVisibility(View.GONE);

        if (preference != null) {
            int workColor = Color.parseColor(preference.getWorkDayColor());
            int restColor = Color.parseColor(preference.getRestDayColor());
            int leaveColor = Color.parseColor(preference.getLeaveDayColor());
            int lateColor = Color.parseColor(preference.getLateDayColor());

            if (!item.isCurrentMonth) {
                holder.tvDayNum.setBackground(null);
                holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_tertiary));
                holder.itemView.setAlpha(0.3f);
            } else {
                holder.itemView.setAlpha(1f);
                if (item.record != null) {
                    String status = item.record.getStatus();
                    if ("WORK".equals(status)) {
                        applyStatusBackground(holder, workColor, item.isToday);
                        holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_bright_status));
                        String info = TimeUtils.formatTruncatedHours(item.record.getWorkHours()) + "+"
                                + TimeUtils.formatTruncatedHours(item.record.getOvertimeHours());
                        holder.tvDayInfo.setText(info);
                        holder.tvDayInfo.setVisibility(View.VISIBLE);
                        if (item.record.isLate()) {
                            holder.lateBadge.setVisibility(View.VISIBLE);
                            holder.lateBadge.getBackground().setTint(lateColor);
                        }
                    } else if ("REST".equals(status)) {
                        applyStatusBackground(holder, restColor, item.isToday);
                        holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_bright_status));
                        holder.tvDayInfo.setText("休");
                        holder.tvDayInfo.setVisibility(View.VISIBLE);
                    } else if ("LEAVE".equals(status)) {
                        if (item.record.isFullDayLeave()) {
                            applyStatusBackground(holder, leaveColor, item.isToday);
                            holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_bright_status));
                            holder.tvDayInfo.setText("假");
                            holder.tvDayInfo.setVisibility(View.VISIBLE);
                        } else {
                            applyStatusBackground(holder, workColor, item.isToday);
                            holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_bright_status));
                            String info = TimeUtils.formatTruncatedHours(item.record.getWorkHours()) + "+"
                                    + TimeUtils.formatTruncatedHours(item.record.getOvertimeHours());
                            holder.tvDayInfo.setText(info);
                            holder.tvDayInfo.setVisibility(View.VISIBLE);
                            if (item.record.isLate()) {
                                holder.lateBadge.setVisibility(View.VISIBLE);
                                holder.lateBadge.getBackground().setTint(lateColor);
                            }
                        }
                    }
                } else if (item.isRestDay) {
                    holder.tvDayNum.setBackgroundResource(R.drawable.bg_rectangle_work);
                    holder.tvDayNum.getBackground().setTint(restColor);
                    holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_bright_status));
                    holder.tvDayInfo.setText("休");
                    holder.tvDayInfo.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDayNum.setBackground(null);
                    holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
                    if (item.isToday) {
                        holder.tvDayNum.setBackgroundResource(R.drawable.bg_circle_primary_light);
                        holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
                    }
                }
            }
        } else {
            holder.tvDayNum.setBackground(null);
            holder.tvDayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item.isCurrentMonth) {
                listener.onDayClick(item.date, item.isCurrentMonth);
            }
        });
    }

    private LayerDrawable createTodayDotBackground(android.content.Context context, int statusColor, int viewWidth) {
        float density = context.getResources().getDisplayMetrics().density;
        int corner = (int) (8 * density);
        int lineWidth = (int) (8 * density);
        int lineHeight = (int) (3 * density);

        GradientDrawable rect = new GradientDrawable();
        rect.setShape(GradientDrawable.RECTANGLE);
        rect.setCornerRadius(corner);
        rect.setColor(statusColor);

        // 用小圆角矩形代替 LINE，确保在 RECTANGLE 内正确显示
        GradientDrawable line = new GradientDrawable();
        line.setShape(GradientDrawable.RECTANGLE);
        line.setCornerRadius(lineHeight / 2f);
        line.setColor(ContextCompat.getColor(context, R.color.text_on_bright_status));
        line.setSize(lineWidth, lineHeight);

        LayerDrawable layer = new LayerDrawable(new GradientDrawable[]{rect, line});

        int viewHeight = (int) (32 * density);
        int bottomMargin = (int) (4 * density);

        int insetHorizontal = (viewWidth - lineWidth) / 2;
        int insetTop = viewHeight - lineHeight - bottomMargin;
        int insetBottom = bottomMargin;

        layer.setLayerInset(1, insetHorizontal, insetTop, insetHorizontal, insetBottom);
        return layer;
    }

    private void applyStatusBackground(DayViewHolder holder, int color, boolean isToday) {
        if (isToday) {
            int viewWidth = holder.tvDayNum.getWidth();
            if (viewWidth > 0) {
                holder.tvDayNum.setBackground(createTodayDotBackground(holder.itemView.getContext(), color, viewWidth));
            } else {
                // View 尚未测量完成，布局完成后重新设置背景
                holder.tvDayNum.post(() -> {
                    int w = holder.tvDayNum.getWidth();
                    if (w > 0) {
                        holder.tvDayNum.setBackground(createTodayDotBackground(holder.itemView.getContext(), color, w));
                    }
                });
            }
        } else {
            holder.tvDayNum.setBackgroundResource(R.drawable.bg_rectangle_work);
            holder.tvDayNum.getBackground().setTint(color);
        }
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
