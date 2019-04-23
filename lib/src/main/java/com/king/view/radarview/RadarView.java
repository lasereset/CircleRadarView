/*
     The MIT License (MIT)
     Copyright (c) 2017 Jenly Yu
     https://github.com/jenly1314
     Permission is hereby granted, free of charge, to any person obtaining
     a copy of this software and associated documentation files
     (the "Software"), to deal in the Software without restriction, including
     without limitation the rights to use, copy, modify, merge, publish,
     distribute, sublicense, and/or sell copies of the Software, and to permit
     persons to whom the Software is furnished to do so, subject to the
     following conditions:
     The above copyright notice and this permission notice shall be included
     in all copies or substantial portions of the Software.
     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
     FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
     DEALINGS IN THE SOFTWARE.
 */
package com.king.view.radarview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */

public class RadarView extends View {

    public static final String DEFAULT_FORMAT = "%1$.0f";

    /**
     * 是否是扫描状态
     */
    private boolean mIsScaning = false;

    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * 外圆环笔画宽度
     */
    private float mOutsideStrokeWidth;
    /**
     * 对角线笔画宽度
     */
    private float mLineStrokeWidth;

    /**
     * 圆心坐标
     */
    private float mCircleCenterX,mCircleCenterY;

    /**
     * 外圆半径
     */
    private float mRadius;

    /**
     * 雷达扫描圆环的颜色
     */
    private int mCircleColor = Color.parseColor("#52fff9");
    /**
     * 对角线的颜色
     */
    private int mLineColor = Color.parseColor("#1ecdf4");


    /**
     * 用于绘制雷达圆环的着色器
     */
    private Shader mCircleShader;
    /**
     * 用于绘制雷达扫描区域的着色器
     */
    private Shader mScanShader;

    private Matrix mMatrix;

    /**
     * 雷达扫描旋转的角度
     */
    private int mRotate;

    /**
     * 是否显示对角线
     */
    private boolean mIsShowLine = true;

    private String mFormat = DEFAULT_FORMAT;

    private boolean mIsShowLabel = true;

    private boolean mIsShowText = true;

    /**
     * 扫描延迟时间（扫描旋转隔时间,默认2毫秒旋转一度）
     */
    private int mScanTime = 2;
    /**
     * 最后扫描刷新时间
     */
    private float mLastTime;

    public RadarView(Context context) {
        this(context,null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr,defStyleRes);
        init(context,attrs);
    }



    private void init(Context context,AttributeSet attrs) {

        mPaint =new Paint();
        mMatrix = new Matrix();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadarView);

        mFormat = a.getString(R.styleable.RadarView_format);
        if(TextUtils.isEmpty(mFormat)){
            mFormat = DEFAULT_FORMAT;
        }

        mCircleColor = a.getColor(R.styleable.RadarView_circleColor,mCircleColor);
        mLineColor = a.getColor(R.styleable.RadarView_lineColor,mLineColor);
        mRotate = a.getInt(R.styleable.RadarView_rotate,mRotate);
        mIsShowLine = a.getBoolean(R.styleable.RadarView_showLine,mIsShowLine);
        mIsShowText = a.getBoolean(R.styleable.RadarView_showText,mIsShowText);
        mIsShowLabel = a.getBoolean(R.styleable.RadarView_showLabel,mIsShowLabel);
        mScanTime = a.getInt(R.styleable.RadarView_scanTime,mScanTime);

        mOutsideStrokeWidth = a.getDimension(R.styleable.RadarView_outsideStrokeWidth,TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getDisplayMetrics()));
        mLineStrokeWidth = a.getDimension(R.styleable.RadarView_lineStrokeWidth,TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,0.3f,getDisplayMetrics()));
        a.recycle();
    }


    private DisplayMetrics getDisplayMetrics(){
        return getResources().getDisplayMetrics();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //默认值
        int defaultValue = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,200,getDisplayMetrics());
        //默认宽高
        int defaultWidth = defaultValue + getPaddingLeft() + getPaddingRight();
        int defaultHeight = defaultValue + getPaddingTop() + getPaddingBottom();

        int width = measureHandler(widthMeasureSpec,defaultWidth);
        int height = measureHandler(heightMeasureSpec,defaultHeight);

        //圆心坐标
        mCircleCenterX = (width + getPaddingLeft() - getPaddingRight())/ 2.0f;
        mCircleCenterY = (height + getPaddingTop() - getPaddingBottom())/ 2.0f;

        //外圆半径
        mRadius = (width - getPaddingLeft() - getPaddingRight() - mOutsideStrokeWidth) / 2.0f;

        setMeasuredDimension(width,height);

    }


    private int measureHandler(int measureSpec, int defaultSize){

        int result = defaultSize;
        int measureMode = MeasureSpec.getMode(measureSpec);
        int measureSize = MeasureSpec.getSize(measureSpec);
        if(measureMode == MeasureSpec.UNSPECIFIED){
            result = defaultSize;
        }else if(measureMode == MeasureSpec.AT_MOST){
            result = Math.min(defaultSize,measureSize);
        }else{
            result = measureSize;
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRadar(canvas);
    }

    /**
     * 绘制雷达扫描图
     * @param canvas
     */
    private void  drawRadar(Canvas canvas){

        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        if(mIsShowLine){//是否绘制对角线和斜对角线
            mPaint.setColor(mLineColor);
            mPaint.setStrokeWidth(mLineStrokeWidth);

            //对角线的半径
            float lineRadius =  mRadius - mOutsideStrokeWidth/2;
            // 根据角度绘制对角线
            float startX, startY, endX, endY;
            double radian;

            //绘制0°~45°区域线

            radian = Math.toRadians(22.5f);
            endX = (float) (mCircleCenterX - lineRadius * Math.sin(radian));
            endY = (float) (mCircleCenterY - lineRadius * Math.cos(radian));
            canvas.drawLine(mCircleCenterX, mCircleCenterY, endX, endY, mPaint);

            endX = (float) (mCircleCenterX + lineRadius * Math.sin(radian));
            endY = (float) (mCircleCenterY - lineRadius * Math.cos(radian));
            canvas.drawLine(mCircleCenterX, mCircleCenterY, endX, endY, mPaint);

//
//            // 绘制0°~180°对角线
//            canvas.drawLine(mCircleCenterX - lineRadius, mCircleCenterY, mCircleCenterX + lineRadius, mCircleCenterY, mPaint);
//            // 绘制90°~270°对角线
//            canvas.drawLine(mCircleCenterX, mCircleCenterY - lineRadius, mCircleCenterX, mCircleCenterY + lineRadius, mPaint);
//
//
//            // 绘制45°~225°对角线
//            // 计算开始位置x/y坐标点
//            radian = Math.toRadians(45f);
//            startX = (float) (mCircleCenterX + lineRadius * Math.cos(radian));
//            startY = (float) (mCircleCenterY + lineRadius * Math.sin(radian));
//            // 计算结束位置x/y坐标点
//            radian = Math.toRadians(45f + 180f);
//            endX = (float) (mCircleCenterX + lineRadius * Math.cos(radian));
//            endY = (float) (mCircleCenterY + lineRadius * Math.sin(radian));
//            canvas.drawLine(startX, startY, endX, endY, mPaint);
//
//            // 绘制135°~315°对角线
//            // 计算开始位置x/y坐标点
//            radian = Math.toRadians(135f);
//            startX = (float) (mCircleCenterX + lineRadius * Math.cos(radian));
//            startY = (float) (mCircleCenterY + lineRadius * Math.sin(radian));
//            // 计算结束位置x/y坐标点
//            radian = Math.toRadians(135f + 180f);
//            endX = (float) (mCircleCenterX + lineRadius * Math.cos(radian));
//            endY = (float) (mCircleCenterY + lineRadius * Math.sin(radian));
//            canvas.drawLine(startX, startY, endX, endY, mPaint);
        }
        //为矩阵设置旋转坐标
        mMatrix.setRotate(mRotate, mCircleCenterX, mCircleCenterY);

        //绘制圆环的渐变着色器
        if (mCircleShader == null) {
            mCircleShader = new SweepGradient(mCircleCenterX, mCircleCenterY, Color.TRANSPARENT, mCircleColor);
        }
        mCircleShader.setLocalMatrix(mMatrix);
        mPaint.setShader(mCircleShader);

        mPaint.setColor(mCircleColor);

        //绘制外框圆
        mPaint.setStrokeWidth(mOutsideStrokeWidth);
        canvas.drawCircle(mCircleCenterX,mCircleCenterY,mRadius,mPaint);

        //绘制圆扫描区域的渐变着色器
        if (mScanShader == null){
            mScanShader = new SweepGradient(mCircleCenterX, mCircleCenterY,new int[] {Color.TRANSPARENT,Color.TRANSPARENT, mCircleColor},null);
        }
        mScanShader.setLocalMatrix(mMatrix);
        mPaint.setShader(mScanShader);
        mPaint.setStyle(Paint.Style.FILL);

        //绘制扫面区域
        float radius = mRadius + mOutsideStrokeWidth/2;
        canvas.drawCircle(mCircleCenterX, mCircleCenterX, radius, mPaint);
    }

    /**
     * 开始扫描动画
     */
    public void start(){
        mIsScaning = true;
        updateScan();

    }

    public void start(int... colors){
        setScanColor(colors);
        start();
    }

    /**
     * 更新雷达扫描区域
     */
    private void updateScan(){
        float curTime = System.currentTimeMillis();
        if(curTime>=mLastTime + mScanTime && mIsScaning){
            mLastTime = curTime;
            removeCallbacks(mRunnable);
            postDelayed(mRunnable,mScanTime);
        }

    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mRotate++;
            if(mRotate>=360){
                mRotate = 0;
            }
            invalidate();
            updateScan();
        }
    };

    /**
     * 停止扫描动画
     */
    public void stop(){
        mIsScaning = false;
    }

    public int getCircleColor() {
        return mCircleColor;
    }

    /**
     * 雷达扫描区域的着色器色值
     * @param colors 传多个色值表示渐变
     */
    public void setScanColor(int... colors){
        mScanShader = new SweepGradient(mCircleCenterX, mCircleCenterY,colors,null);
    }

    /**
     * 雷达内外圆环的着色器色值
     * @param colors 传多个色值表示渐变
     */
    public void setCircleColor(int... colors){
        mCircleShader = new SweepGradient(mCircleCenterX, mCircleCenterY,colors,null);
    }
}
