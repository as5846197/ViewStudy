package com.example.niugulu.viewstudy.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;

/**
 * Created by zhangcaoyang on 16/8/9.
 */
public class IQiYiLoadingView extends View {
    private static final String DEFAULT_COLOR = "#00ba9b";
    private static final int DEFAULT_SIZE = 50;
    private static final int DRAW_CIRCLE = 10001;
    private static final int ROTATE_TRIANGLE = 10002;
    private Paint mPaint;
    private long duration = 1500;
    private int mWidth;
    private int mHeight;
    private Path trianglePath;
    private Path circlePath;
    private Point p1, p2, p3;
    private ValueAnimator animator;
    private float mAnimatorValue = 0;
    private float circleCenter = 0;
    private int mCurrentState = 0;


    public IQiYiLoadingView(Context context) {
        super(context);
    }

    public IQiYiLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAnimation();
        animator.start();
    }

    public IQiYiLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor(DEFAULT_COLOR));
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(1);
        mPaint.setAntiAlias(true);
        mCurrentState = DRAW_CIRCLE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(Math.min(DEFAULT_SIZE, widthSpecSize), Math.min(DEFAULT_SIZE, heightSpecSize));
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(Math.min(DEFAULT_SIZE, widthSpecSize), heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, Math.min(DEFAULT_SIZE, heightSpecSize));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        initPath();
    }

    private void initPath() {
        trianglePath = new Path();
        circlePath = new Path();
        p1 = new Point();
        p2 = new Point();
        p3 = new Point();
        p1.x = -(int) ((mWidth / 4 * Math.tan(30 * Math.PI / 180)));
        p1.y = -mWidth / 4;
        p2.x = p1.x;
        p2.y = mWidth / 4;
        p3.x = (int) (mWidth / 4 / Math.sin(60 * Math.PI / 180));
        p3.y = 0;
        circleCenter = p3.x - (float) (mWidth / 4 / Math.cos(30 * Math.PI / 180));
        trianglePath.moveTo(p1.x, p1.y);
        trianglePath.lineTo(p2.x, p2.y);
        trianglePath.lineTo(p3.x, p3.y);
        RectF circleRect = new RectF(-mWidth / 2, -mWidth / 2, mWidth / 2, mWidth / 2);
        circlePath.addArc(circleRect, 268, 358);
        trianglePath.close();


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mHeight / 2);
//        canvas.save();
//        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//        canvas.rotate(360 * mAnimatorValue);
//        canvas.drawPath(trianglePath, mPaint);
//        canvas.restore();
//        mPaint.setStyle(Paint.Style.STROKE);
//        canvas.drawPath(circlePath,mPaint);
        PathMeasure mMesure = new PathMeasure(circlePath, false);
        Path dst = new Path();
//        mMesure.getSegment(0,mMesure.getLength()*mAnimatorValue,dst,true);
//        mMesure.getSegment(mMesure.getLength() * mAnimatorValue, mMesure.getLength(), dst, true);
//        canvas.drawPath(dst, mPaint);
        switch (mCurrentState) {
            case DRAW_CIRCLE:
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(trianglePath, mPaint);
                mPaint.setStyle(Paint.Style.STROKE);
                mMesure.getSegment(0, mMesure.getLength() * mAnimatorValue, dst, true);
                canvas.drawPath(dst, mPaint);
                if(mAnimatorValue==1){
                    mCurrentState = ROTATE_TRIANGLE;
                    animator.start();
                }
                break;
            case ROTATE_TRIANGLE:
                canvas.save();
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.rotate(360 * mAnimatorValue);
                canvas.drawPath(trianglePath, mPaint);
                canvas.restore();
                mPaint.setStyle(Paint.Style.STROKE);
                mMesure.getSegment(mMesure.getLength() * mAnimatorValue, mMesure.getLength(), dst, true);
                canvas.drawPath(dst, mPaint);
                break;
            default:
                break;
        }

    }

    private void initAnimation() {
        TimeInterpolator timeInterpolator = new AccelerateDecelerateInterpolator();
        animator = ValueAnimator.ofFloat(0, 1).setDuration(duration);
        animator.setInterpolator(timeInterpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }
}
