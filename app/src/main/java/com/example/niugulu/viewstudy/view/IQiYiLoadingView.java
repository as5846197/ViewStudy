package com.example.niugulu.viewstudy.view;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
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

import com.example.niugulu.viewstudy.R;
import com.example.niugulu.viewstudy.Utils;

/**
 * Created by zhangcaoyang on 16/8/9.
 */
public class IQiYiLoadingView extends View {
    private static final String DEFAULT_COLOR = "#00ba9b";
    private static final int DEFAULT_SIZE = 50;
    private static final int DRAW_CIRCLE = 10001;
    private static final int ROTATE_TRIANGLE = 10002;
    private Context mContext;
    private Paint trianglePaint;
    private Paint circlePaint;
    private float paintStrokeWidth = 1;
    private long duration = 800;
    private int mWidth;
    private int mHeight;
    private Path trianglePath;
    private Path circlePath;
    private Point p1, p2, p3;
    private ValueAnimator animator;
    private float mAnimatorValue = 0;
    private float circleCenter = 0;
    private int mCurrentState = 0;
    private int radius = 0;

    private int triangleColor = -1;
    private int circleColor = -1;


    public IQiYiLoadingView(Context context) {
        super(context);
        init();
    }

    public IQiYiLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
        initAnimation();
        animator.start();
    }

    public IQiYiLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.IQiYiLoading);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.IQiYiLoading_triangleColor:
                    triangleColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.IQiYiLoading_circleColor:
                    circleColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.IQiYiLoading_duration:
                    duration = array.getInt(attr, 800);
                    break;
                case R.styleable.IQiYiLoading_circleWidth:
                    paintStrokeWidth = array.getDimension(attr, Utils.dip2px(mContext, 1));
                    break;
                default:
                    break;
            }
        }
        init();
    }

    private void init() {
        trianglePaint = new Paint();
        circlePaint = new Paint();
        trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePaint.setStrokeWidth(Utils.dip2px(mContext, 1));
        trianglePaint.setColor(triangleColor == -1 ? Color.parseColor(DEFAULT_COLOR) : triangleColor);
        trianglePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(paintStrokeWidth);
        circlePaint.setColor(circleColor == -1 ? Color.parseColor(DEFAULT_COLOR) : circleColor);
        circlePaint.setAntiAlias(true);
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
            setMeasuredDimension(Math.min(Utils.dip2px(mContext, DEFAULT_SIZE), widthSpecSize), Math.min(Utils.dip2px(mContext, DEFAULT_SIZE), heightSpecSize));
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(Math.min(Utils.dip2px(mContext, DEFAULT_SIZE), widthSpecSize), heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, Math.min(Utils.dip2px(mContext, DEFAULT_SIZE), heightSpecSize));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w - getPaddingLeft() - getPaddingRight();
        mHeight = h - getPaddingTop() - getPaddingBottom();
        initPath();
    }

    private void initPath() {
        trianglePath = new Path();
        circlePath = new Path();
        p1 = new Point();
        p2 = new Point();
        p3 = new Point();
        radius = (int) ((mWidth < mHeight ? mWidth : mHeight) - paintStrokeWidth) / 2;
        p1.x = -(int) ((radius / 2 * Math.tan(30 * Math.PI / 180)));
        p1.y = -radius / 2;
        p2.x = p1.x;
        p2.y = radius / 2;
        p3.x = (int) (radius / 2 / Math.sin(60 * Math.PI / 180));
        p3.y = 0;
        circleCenter = p3.x - (float) (radius / 2 / Math.cos(30 * Math.PI / 180));
        trianglePath.moveTo(p1.x, p1.y);
        trianglePath.lineTo(p2.x, p2.y);
        trianglePath.lineTo(p3.x, p3.y);
        RectF circleRect = new RectF(-radius, -radius, radius, radius);
        circlePath.addArc(circleRect, 268, 358);
        trianglePath.close();


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mHeight / 2);
        PathMeasure mMeasure = new PathMeasure(circlePath, false);
        Path dst = new Path();
        switch (mCurrentState) {
            case DRAW_CIRCLE:
                trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(trianglePath, trianglePaint);
                mMeasure.getSegment(0, mMeasure.getLength() * mAnimatorValue, dst, true);
                canvas.drawPath(dst, circlePaint);
                break;
            case ROTATE_TRIANGLE:
                canvas.save();
                trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.rotate(360 * mAnimatorValue);
                canvas.drawPath(trianglePath, trianglePaint);
                canvas.restore();
                mMeasure.getSegment(mMeasure.getLength() * mAnimatorValue, mMeasure.getLength(), dst, true);
                canvas.drawPath(dst, circlePaint);
                break;
            default:
                break;
        }

    }

    private void initAnimation() {
        TimeInterpolator timeInterpolator = new AccelerateDecelerateInterpolator();
        animator = ValueAnimator.ofFloat(0, 1).setDuration(duration);
        animator.setInterpolator(timeInterpolator);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                switch (mCurrentState) {
                    case DRAW_CIRCLE:
                        mCurrentState = ROTATE_TRIANGLE;
                        break;
                    case ROTATE_TRIANGLE:
                        mCurrentState = DRAW_CIRCLE;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void setTriangleColor(int triangleColor) {
        this.triangleColor = triangleColor;
        trianglePaint.setColor(triangleColor);
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
        circlePaint.setColor(circleColor);
    }

    public void setDuration(int duration) {
        this.duration = duration;
        animator.setDuration(duration);
    }

    public void setPaintStrokeWidth(float strokeWidth) {
        this.paintStrokeWidth = strokeWidth;
        circlePaint.setStrokeWidth(paintStrokeWidth);
    }
}
