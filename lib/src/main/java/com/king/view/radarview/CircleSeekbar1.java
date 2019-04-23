package com.king.view.radarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by xinchen on 19-4-23.
 */

public class CircleSeekbar1 extends View{
    private final String TAG = "CircleSeekbar";
    private static int INVALID_PROGRESS_VALUE = -1;

    private Paint circlePaint;
    private Paint progressPaint;
    private Paint paintDegree;
    private Paint calibrationPaint;//刻度
    private Paint anglePaint;//角度显示
    private int circleColor;
    private int progressColor;
    private int textColor;
    private int textSize = 12;
    private int progressWidth = 5;
    private int circleWidth = 6;
    private int maxProgress = 100;
    private int padding = 5;
    private int degreeLength_long = 10;//刻度的长度
    private int degreeLength_short = 6;//短刻度线
    private boolean mTouchInside = true;
    private String[] degreeText = {"0°", "30°", "60°", "90°", "120°", "150°", "180°", "210°", "240°", "270°", "300°", "330°"};

    private boolean enable = true;

    private int maxWidth = 6;
    private int radius;
    private int diameter;
    private int mWidth;
    private float mCurAngle = 60;
    private int centreX;
    private int centreY;
    private float mTouchIgnoreRadius;
    private boolean isFirstTouch = true;
    private RectF mCircleRect = new RectF();

    private boolean isSecond = false;//是否是第二个

    private float startAngle = 220;

    public CircleSeekbar1(Context context) {
        super(context);
        init(context, null);
    }

    public CircleSeekbar1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleSeekbar1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        float density = context.getResources().getDisplayMetrics().density;
        int thumbHalfheight = 0;

        circleColor = getResources().getColor(R.color.circleseekbar_gray);
        progressColor = getResources().getColor(R.color.circleseekbar_blue_light);
        textColor = getResources().getColor(R.color.circleseekbar_text_color);
        progressWidth = (int) (progressWidth * density);
        circleWidth = (int) (circleWidth * density);
        textSize = (int) (textSize * density);

        degreeLength_long = (int) (degreeLength_long * density);
        degreeLength_short = (int) (degreeLength_short * density);

        padding = (int) (padding * density);

        if (null != attrs) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleSeekbar);
            circleColor = typedArray.getColor(R.styleable.CircleSeekbar_circleColor1, circleColor);
            progressColor = typedArray.getColor(R.styleable.CircleSeekbar_circleColor1, progressColor);
            textColor = typedArray.getColor(R.styleable.CircleSeekbar_textColor, textColor);
            textSize = typedArray.getColor(R.styleable.CircleSeekbar_textSize, textSize);
            circleWidth = (int) typedArray.getDimension(R.styleable.CircleSeekbar_circleWidth, circleWidth);
            progressWidth = (int) typedArray.getDimension(R.styleable.CircleSeekbar_progressWidth, progressWidth);
            maxProgress = typedArray.getInteger(R.styleable.CircleSeekbar_maxProgress, maxProgress);
            mTouchInside = typedArray.getBoolean(R.styleable.CircleSeekbar_touchInside, mTouchInside);
            typedArray.recycle();
        }

        padding = thumbHalfheight + padding;

        maxWidth = circleWidth > progressWidth ? circleWidth : progressWidth;

        circlePaint = new Paint();
        circlePaint.setColor(circleColor);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(circleWidth);

        progressPaint = new Paint();
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);

        paintDegree = new Paint();
        paintDegree.setColor(textColor);
        paintDegree.setAntiAlias(true);
        paintDegree.setStyle(Paint.Style.FILL);
        paintDegree.setStrokeWidth(1);
        paintDegree.setTextSize(textSize);

        //绘制刻度
        calibrationPaint = new Paint();
        calibrationPaint.setColor(Color.BLACK);
        calibrationPaint.setAntiAlias(true);

        //绘制角度
        anglePaint = new Paint();
        anglePaint.setColor(textColor);
        anglePaint.setAntiAlias(true);
        anglePaint.setStyle(Paint.Style.FILL);
        anglePaint.setStrokeWidth(1);
        anglePaint.setTextSize(textSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(centreX, centreY, radius, circlePaint);
        canvas.drawArc(mCircleRect, startAngle, (float) mCurAngle, false, progressPaint);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        String angle = formatAngle(mCurAngle) + "°";
        canvas.drawText(angle, centreX - anglePaint.measureText(angle) / 2, centreY - 100/*字体的高度*/, anglePaint);

        float rotation = 360 / degreeText.length;
        for (int i = 0; i < degreeText.length; i++) {
            canvas.drawText(degreeText[i], centreX - paintDegree.measureText(degreeText[i]) / 2, 100/*字体的高度*/, paintDegree);
            canvas.rotate(rotation, centreX, centreY);
        }
        for (int i = 0; i < 60; i++) {
            int degreeLength;
            if(i % 5 == 0) {
                calibrationPaint.setStrokeWidth(6);
                degreeLength = degreeLength_long;
            } else {
                calibrationPaint.setStrokeWidth(3);
                degreeLength = degreeLength_short;
            }
            canvas.drawLine(centreX, centreY - radius, centreX, centreY - radius + degreeLength, calibrationPaint);
            canvas.rotate(360 / 60, centreX, centreY);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        mWidth = Math.min(width, height);
        centreX = mWidth / 2;
        centreY = mWidth / 2;

        float left = getPaddingLeft() + maxWidth / 2 + padding;
        float top = getPaddingTop() + maxWidth / 2 + padding;
        float right = mWidth - getPaddingRight() - maxWidth / 2 - padding;
        float bottom = mWidth - getPaddingBottom() - maxWidth / 2 - padding;

        diameter = mWidth - getPaddingLeft() - getPaddingRight() - maxWidth - padding * 2;
        radius = diameter / 2;
        mCircleRect.set(left, top, left + diameter, top + diameter);

        setTouchInSide(mTouchInside);
        setMeasuredDimension(mWidth, mWidth);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enable) {
            this.getParent().requestDisallowInterceptTouchEvent(true);//通知父控件勿拦截本次触摸事件

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isTouchArc(event.getX(), event.getY())) {
                        isFirstTouch = true;
                        setPressed(true);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isFirstTouch) {
                        changePosition(event.getX(), event.getY(), radius);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isFirstTouch = false;
                    setPressed(false);
                    break;
            }
            return true;
        }
        return false;
    }

    private int maxError = 70;
    private int maxError0 = 100;

    private boolean isTouchArc(float x, float y) {
        PointF p = ChartUtils.calcArcEndPointXY(centreX, centreY, radius, startAngle, mCurAngle);
        PointF p2 = ChartUtils.calcArcEndPointXY(centreX, centreY, radius, 0, mCurAngle);
        Log.d(TAG, "Enter into this isTouchArc p ===>" + p.x + "----" + p.y);
        Log.d(TAG, "Enter into this isTouchArc p2===>" + p2.x + "----" + p2.y);
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

    private void changePosition(float x, float y, int r) {
        double v = ChartUtils.calSweep(x, y, r);
        if (mCurAngle >= 360) {
            mCurAngle = mCurAngle % 360;
        }
        if (isSecond) {
            changeSecond(x, y, r, v);
        } else {
            changeFirst(v);
        }
        invalidate();
    }

    //    改变第二个点的位置
    private void changeSecond(float x, float y, int r, double v) {
        if (x > r) {
            if (y <= r) {
                if (v >= startAngle) {
                    mCurAngle = (float) (v - startAngle);
                } else {
                    mCurAngle = (float) (360 - (startAngle - v));
                }
            } else {
                mCurAngle = (float) (360 - (startAngle - v));
            }
        } else {
            mCurAngle = (float) (360 + v - startAngle);
        }
        mCurAngle = mCurAngle % 360;
    }

    //改变第一个点的位置
    private void changeFirst(double v) {
        if (mCurAngle < 0) {
            mCurAngle = mCurAngle + 360;
        }
        float cSweep = (float) (v - startAngle);
        startAngle = (float) v;
        mCurAngle = mCurAngle - cSweep;
    }

    @SuppressLint("DefaultLocale")
    private static String formatAngle(double angle) {
        return String.format("%.1f", angle);
    }

    //设置触摸生效范围
    public void setTouchInSide(boolean isEnabled) {
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreRadius = (float) radius / 4;
        }
    }

    //重写drawableStateChanged，同步改变thum的状态
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
