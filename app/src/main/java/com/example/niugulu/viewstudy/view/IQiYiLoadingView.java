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
    private static final int DRAW_CIRCLE = 10001; //状态标记  画出圆形和三角形 执行画出圆形的动画
    private static final int ROTATE_TRIANGLE = 10002; //状态标记  执行旋转三角形和收回圆形的动画
    private Context mContext;
    private Paint trianglePaint;
    private Paint circlePaint;
    private float paintStrokeWidth = 1; // 设置圆形的宽度
    private long duration = 800;    //执行时间
    private int mWidth;
    private int mHeight;
    private Path trianglePath;
    private Path circlePath;
    private Path dst;  //由pathMeasure计算后的path
    private Point p1, p2, p3;  //确定三角形的三个点
    private ValueAnimator animator;     //属性动画  主要是获取0-1的值来执行动画
    private float mAnimatorValue = 0;
    private int mCurrentState = 0;
    private int radius = 0;  //圆的半径

    private float startSegment; //圆开始画的长度
    private PathMeasure mMeasure;

    private int triangleColor = -1;
    private int circleColor = -1;


    public IQiYiLoadingView(Context context) {
        super(context);
        init();
        initAnimation();
        animator.start();
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
        initAnimation();
        animator.start();
    }

    /**
     * 获取声明的属性
     */
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
        array.recycle();
        array = null;
        init();
    }

    /**
     * 初始化画笔还有状态
     */
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

    /**
     * 设置wrap_content的时候宽高为默认值
     */
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

    /**
     * 初始化路径 三角形和外圈圆
     */
    private void initPath() {
        dst = new Path();
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
        trianglePath.moveTo(p1.x, p1.y);
        trianglePath.lineTo(p2.x, p2.y);
        trianglePath.lineTo(p3.x, p3.y);
        RectF circleRect = new RectF(-radius, -radius, radius, radius);
        circlePath.addArc(circleRect, 268, 358);
        mMeasure = new PathMeasure(circlePath, false);
        trianglePath.close();


    }

    /**
     * 这里主要涉及到的知识就是对path进行测量
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mHeight / 2);
        dst.reset();
        switch (mCurrentState) {
            case DRAW_CIRCLE:
                startSegment = (float) (mMeasure.getLength() / 5 * ((0.3 - mAnimatorValue) > 0 ? (0.3 - mAnimatorValue) : 0));
                trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(trianglePath, trianglePaint);
                mMeasure.getSegment(startSegment, mMeasure.getLength() * mAnimatorValue, dst, true);
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

    /**
     * 属性动画的设置
     */
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
