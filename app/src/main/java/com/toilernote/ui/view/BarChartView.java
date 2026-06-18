package com.toilernote.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.toilernote.R;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {

    private final Paint paintWork = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintOvertime = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintAxis = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<BarData> dataList = new ArrayList<>();
    private float maxValue = 12f;
    private float barWidth;
    private float gap;
    private final RectF rect = new RectF();

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Context ctx = getContext();
        paintWork.setColor(ContextCompat.getColor(ctx, R.color.primary));
        paintOvertime.setColor(ContextCompat.getColor(ctx, R.color.chart_overtime));
        paintText.setColor(ContextCompat.getColor(ctx, R.color.text_tertiary));
        paintText.setTextSize(24f);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintAxis.setColor(ContextCompat.getColor(ctx, R.color.divider));
        paintAxis.setStrokeWidth(2f);
    }

    public void setData(List<BarData> data) {
        this.dataList.clear();
        if (data != null) {
            this.dataList.addAll(data);
        }
        calculateMax();
        invalidate();
    }

    private void calculateMax() {
        maxValue = 12f;
        for (BarData d : dataList) {
            float total = d.work + d.overtime;
            if (total > maxValue) maxValue = total;
        }
        if (maxValue < 1f) maxValue = 1f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataList.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float paddingBottom = 40f;
        float paddingTop = 20f;
        float chartHeight = height - paddingBottom - paddingTop;

        int count = dataList.size();
        gap = width / (count * 3f + 1);
        barWidth = gap * 1.2f;

        for (int i = 0; i < count; i++) {
            BarData d = dataList.get(i);
            float x = gap + i * (barWidth + gap);
            float workHeight = (d.work / maxValue) * chartHeight;
            float otHeight = (d.overtime / maxValue) * chartHeight;

            float bottom = height - paddingBottom;

            // Work bar
            if (workHeight > 0) {
                rect.set(x, bottom - workHeight - otHeight, x + barWidth, bottom - otHeight);
                canvas.drawRect(rect, paintWork);
            }

            // Overtime bar
            if (otHeight > 0) {
                rect.set(x, bottom - otHeight, x + barWidth, bottom);
                canvas.drawRect(rect, paintOvertime);
            }

            // Label
            canvas.drawText(String.valueOf(d.day), x + barWidth / 2, height - 10, paintText);
        }
    }

    public static class BarData {
        public int day;
        public float work;
        public float overtime;

        public BarData(int day, float work, float overtime) {
            this.day = day;
            this.work = work;
            this.overtime = overtime;
        }
    }
}
