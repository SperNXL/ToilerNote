package com.toilernote.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.toilernote.R;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {

    private final Paint paintWork = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintOvertime = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Point> workPoints = new ArrayList<>();
    private final List<Point> otPoints = new ArrayList<>();
    private float maxValue = 1f;

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Context ctx = getContext();
        paintWork.setColor(ContextCompat.getColor(ctx, R.color.primary));
        paintWork.setStyle(Paint.Style.STROKE);
        paintWork.setStrokeWidth(4f);
        paintWork.setStrokeCap(Paint.Cap.ROUND);
        paintWork.setStrokeJoin(Paint.Join.ROUND);

        paintOvertime.setColor(ContextCompat.getColor(ctx, R.color.chart_overtime_line));
        paintOvertime.setStyle(Paint.Style.STROKE);
        paintOvertime.setStrokeWidth(4f);
        paintOvertime.setPathEffect(new android.graphics.DashPathEffect(new float[]{10, 10}, 0));
        paintOvertime.setStrokeCap(Paint.Cap.ROUND);

        paintFill.setColor(ContextCompat.getColor(ctx, R.color.primary));
        paintFill.setAlpha(30);
        paintFill.setStyle(Paint.Style.FILL);
    }

    public void setData(List<Point> work, List<Point> overtime) {
        this.workPoints.clear();
        this.otPoints.clear();
        if (work != null) this.workPoints.addAll(work);
        if (overtime != null) this.otPoints.addAll(overtime);
        calculateMax();
        invalidate();
    }

    private void calculateMax() {
        maxValue = 1f;
        for (Point p : workPoints) {
            if (p.value > maxValue) maxValue = p.value;
        }
        for (Point p : otPoints) {
            if (p.value > maxValue) maxValue = p.value;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (workPoints.isEmpty() && otPoints.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 20f;
        float chartHeight = height - padding * 2;

        // Draw work line
        if (workPoints.size() > 1) {
            Path path = new Path();
            Path fillPath = new Path();
            float stepX = width / (workPoints.size() - 1);

            for (int i = 0; i < workPoints.size(); i++) {
                float x = i * stepX;
                float y = height - padding - (workPoints.get(i).value / maxValue) * chartHeight;
                if (i == 0) {
                    path.moveTo(x, y);
                    fillPath.moveTo(x, height - padding);
                    fillPath.lineTo(x, y);
                } else {
                    path.lineTo(x, y);
                    fillPath.lineTo(x, y);
                }
            }
            fillPath.lineTo(width, height - padding);
            fillPath.close();
            canvas.drawPath(fillPath, paintFill);
            canvas.drawPath(path, paintWork);
        }

        // Draw overtime line
        if (otPoints.size() > 1) {
            Path path = new Path();
            float stepX = width / (otPoints.size() - 1);
            for (int i = 0; i < otPoints.size(); i++) {
                float x = i * stepX;
                float y = height - padding - (otPoints.get(i).value / maxValue) * chartHeight;
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            canvas.drawPath(path, paintOvertime);
        }
    }

    public static class Point {
        public float value;

        public Point(float value) {
            this.value = value;
        }
    }
}
