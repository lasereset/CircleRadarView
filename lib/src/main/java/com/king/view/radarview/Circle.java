package com.king.view.radarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by xinchen on 19-4-23.
 */

public class Circle extends View {
    private Paint mPaint;
    private int circleColor;
    private int progressColor;
    private int progressWidth;
    private float startAngle;
    private float sweepAngle;
    private int radius;
    private int centreX;
    private int centreY;
    private int maxError = 70;
    private int maxError0 = 100;
    private boolean downOnArc;
    private boolean isSecond = true;
    private int maxProgress = 100;
    /**
     * 中间进度百分比的字符串的颜色
     */
    private int textColor;
    /**
     * 中间进度百分比的字符串的字体
     */
    private float textSize;

    public Circle(Context context) {
        this(context, null);
    }

    public Circle(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Circle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        circleColor = Color.GRAY;
        progressColor = Color.GREEN;
        progressWidth = 30;
        startAngle = 270;
        sweepAngle = 60;
        textColor = Color.BLACK;
        textSize = 40;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); //获取测量模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec); //获取测量大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int mWidth = 0;
        int mHeight = 0;
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            radius = widthSize / 2;
            centreX = widthSize / 2;
            centreY = heightSize / 2;
            mWidth = widthSize;
            mHeight = heightSize;
        }
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            mWidth = (int) (radius * 2);
            mHeight = (int) (radius * 2);
            centreX = radius;
            centreY = radius;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(circleColor);
        mPaint.setStrokeWidth(progressWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE); //        RectF rect = new RectF(progressWidth, progressWidth, getWidth() - progressWidth, getHeight() - progressWidth);
        RectF rect = new RectF(progressWidth * 2, progressWidth * 2, getWidth() - progressWidth * 2, getHeight() - progressWidth * 2);
        canvas.drawArc(rect, 0, 360, false, mPaint);
        mPaint.setColor(progressColor);
        canvas.drawArc(rect, startAngle, sweepAngle, false, mPaint); //        画小圆
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setStrokeWidth(4);
        p.setStyle(Paint.Style.FILL);
        PointF point = ChartUtils.calcArcEndPointXY(centreX, centreY, radius - progressWidth * 2, sweepAngle, startAngle);
        canvas.drawCircle(point.x, point.y, 40, p); //        画小圆2
        p.setColor(Color.YELLOW);
        PointF point2 = ChartUtils.calcArcEndPointXY(centreX, centreY, radius - progressWidth * 2, 0, startAngle);
        canvas.drawCircle(point2.x, point2.y, 40, p); /**
         * 画文字
         */
        mPaint.setStrokeWidth(0);
        mPaint.setColor(textColor);
        mPaint.setTextSize(textSize);
        String textTime = getTimeText(startAngle, sweepAngle);
        float textWidth = mPaint.measureText(textTime);
        canvas.drawText(textTime, centreX - textWidth / 2, centreY + textSize / 2, mPaint);
    }

    private String getTimeText(float startAngle, float sweepAngle) {
        float startProgress = (startAngle + 90) % 360 / 360 * maxProgress;
        float endProgress = sweepAngle / 360 * maxProgress + startProgress;
        String result = startProgress + "<--->" + endProgress % maxProgress;
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchArc(x, y)) {
                    downOnArc = true;
                    changePosition(x, y, radius);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (downOnArc) {
                    changePosition(x, y, radius);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                downOnArc = false;
                changePosition(x, y, radius);
                break;
        }
        return super.onTouchEvent(event);
    } // 判断是否按在圆边上

    private boolean isTouchArc(int x, int y) { //        double d = getTouchRadius(x, y);
        PointF p = ChartUtils.calcArcEndPointXY(centreX, centreY, radius, sweepAngle, startAngle);
        PointF p2 = ChartUtils.calcArcEndPointXY(centreX, centreY, radius, 0, startAngle);
        int absx = (int) Math.abs(x - p.x);
        int absy = (int) Math.abs(y - p.y);
        int absx2 = (int) Math.abs(x - p2.x);
        int absy2 = (int) Math.abs(y - p2.y);
        if (absx <= maxError && absy <= maxError) {
            isSecond = true;
            return true;
        }
        if (absx2 <= maxError0 && absy2 <= maxError0) {
            isSecond = false;
            return true;
        }
        return false;
    }

    private void changePosition(int x, int y, int r) {
        double v = ChartUtils.calSweep(x, y, r);
        if (sweepAngle >= 360) {
            sweepAngle = sweepAngle % 360;
        }
        if (isSecond) {
            changeSecond(x, y, r, v);
        } else {
            changeFirst(v);
        }
//        if (changeListener != null) {
//            changeListener.onProgressChange(startAngle, sweepAngle);
//        }
        invalidate();
    }

    //    改变第二个点的位置
    private void changeSecond(int x, int y, int r, double v) {
        if (x > r) {
            if (y <= r) {
                if (v >= startAngle) {
                    sweepAngle = (float) (v - startAngle);
                } else {
                    sweepAngle = (float) (360 - (startAngle - v));
                }
            } else {
                sweepAngle = (float) (360 - (startAngle - v));
            }
        } else {
            sweepAngle = (float) (360 + v - startAngle);
        }
        sweepAngle = sweepAngle % 360;
    } //    改变第一个点的位置

    private void changeFirst(double v) { //        float secondAngle = (startAngle + sweepAngle) % 360;

        if (sweepAngle < 0) {
            sweepAngle = sweepAngle + 360;
        }
        float cSweep = (float) (v - startAngle);
        startAngle = (float) v;
        sweepAngle = sweepAngle - cSweep;
    } //    判断第一个原点是不是跟在第二个后面

    private boolean isAfterFllow(float start, float sweep) {
        float startProgress = (startAngle + 90) % 360 / 360 * maxProgress;
        float endProgress = sweepAngle / 360 * maxProgress + startProgress;
        return endProgress <= maxProgress && (Math.ceil(sweep) >= 30);
    } // 计算某点到圆点的距离

    private double getTouchRadius(int x, int y) {
        int cx = x - getWidth() / 2;
        int cy = y - getHeight() / 2;
        return Math.hypot(cx, cy);
    }
}