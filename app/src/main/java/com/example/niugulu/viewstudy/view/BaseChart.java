package com.example.niugulu.viewstudy.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.niugulu.viewstudy.PieData;
import com.example.niugulu.viewstudy.R;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by zhangcaoyang on 16/8/2.
 */
public class BaseChart extends View {

    Paint mPaint = new Paint();

    int mWidth, mViewWidth;
    int mHeight, mViewHeight;

    private ArrayList<PieData> mPieData = new ArrayList<>();

    float mStartAngle = 0;
    RectF rectF = new RectF(), rectFTra = new RectF(), rectFIn = new RectF();
    float r, rTra, rWhite;
    RectF rectFF = new RectF(), rectFTraF = new RectF(), reatFWhite = new RectF();
    float rF, rTraF, rWhiteF;

    ValueAnimator animator;
    float animatedValue;
    long animatorDuration = 5000;
    TimeInterpolator timeInterpolator = new AccelerateDecelerateInterpolator();
    boolean animatedFlag = true;

    boolean touchFlag = true;
    float[] pieAngles;
    int angleId;
    double offsetScaleRadius = 1.1;

    double widthScaleRadius = 0.9;
    double radiusScaleTransparent = 0.6;
    double radiusScaleInside = 0.5;

    int percentTextSize = 45;
    int centerTextSize = 60;

    int centerTextColor = Color.BLACK;
    int percentTextColor = Color.WHITE;
    int percentDecimal = 0;
    String name = "PieChart";
    Point mPoint = new Point();
    float minAngle = 30;

    Path outPath = new Path();
    Path minPath = new Path();
    Path inPath = new Path();
    Path outMidPath = new Path();
    Path minInPath = new Path();

    int stringId = 0;

    boolean percentFlag = true;


    public BaseChart(Context context) {
        super(context);
    }

    public BaseChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PieChart);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.PieChart_name:
                    break;
                case R.styleable.PieChart_percentDecimal:
                    break;
                case R.styleable.PieChart_textSize:
                    break;
                default:
                    break;
            }
        }
        array.recycle();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    private void initAnimator(long duration) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
            animator.start();
        } else {
            animator = ValueAnimator.ofFloat(0, 360).setDuration(duration);
            animator.setInterpolator(timeInterpolator);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animatedValue = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });

            animator.start();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measuredDimension(widthMeasureSpec);
        int height = measuredDimension(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measuredDimension(int measureSpec) {
        int size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                size = measureWrap(mPaint);
                break;
            case MeasureSpec.EXACTLY:
                size = specSize;
                break;
            case MeasureSpec.AT_MOST:
                size = Math.min(specSize, measureWrap(mPaint));
                break;
            default:
                size = measureWrap(mPaint);
                break;
        }
        return size;
    }

    private int measureWrap(Paint paint) {
        float wrapSize;
        if (mPieData != null && mPieData.size() > 1) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMinimumFractionDigits(percentDecimal);
            paint.setTextSize(percentTextColor);
            float percentWidth = paint.measureText(numberFormat.format(mPieData.get(stringId).getPercentage()) + "");
            paint.setTextSize(centerTextSize);
            float nameWidth = paint.measureText(name + "");
            wrapSize = (percentWidth * 4 + nameWidth * 1.0f) * (float) offsetScaleRadius;
        } else {
            wrapSize = 0;
        }
        return (int) wrapSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w - getPaddingLeft() - getPaddingRight();
        mHeight = h - getPaddingTop() - getPaddingBottom();
        mViewWidth = w;
        mViewHeight = h;

        r = (float) (Math.min(mWidth, mHeight) / 2 * widthScaleRadius);
        if (r > Math.min(mWidth, mHeight)) {
            r = 0;
            percentFlag = false;
            name = "";
        }
        rectF.set(-r, -r, r, r);
        rTra = (float) (r * radiusScaleTransparent);

        rectFTra.set(-rTra, -rTra, rTra, rTra);

        rWhite = (float) (r * radiusScaleInside);
        rectFIn.set(-rWhite, -rWhite, rWhite, rWhite);

        rF = (float) (Math.min(mWidth, mHeight) / 2 * widthScaleRadius * offsetScaleRadius);
        rectFF.set(-rF, -rF, rF, rF);

        rTra = (float) (rF * radiusScaleTransparent);

        rectFTra.set(-rTra, -rTra, rTra, rTra);

        rWhite = (float) (rF * radiusScaleInside);
        rectFIn.set(-rWhite, -rWhite, rWhite, rWhite);
        if (animatedFlag) {
            initAnimator(animatorDuration);
        } else {
            animatedValue = 360f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPieData == null) {
            return;
        }
        float currentStartAngle = 0;
        canvas.translate(mViewWidth / 2, mViewHeight / 2);
        canvas.save();
        canvas.rotate(mStartAngle);
        float drawAngle;
        for (int i = 0; i < mPieData.size(); i++) {
            PieData pie = mPieData.get(i);
            if (Math.min(pie.getAngle() - 1, animatedValue - currentStartAngle) >= 0) {
                drawAngle = Math.min(pie.getAngle() - 1, animatedValue - currentStartAngle);
            } else {
                drawAngle = 0;
            }
            if (i == angleId) {
                drawArc(canvas, currentStartAngle, drawAngle, pie, rectFF, rectFTraF, reatFWhite, mPaint);
            } else {
                drawArc(canvas, currentStartAngle, drawAngle, pie, rectF, rectFTra, rectFIn, mPaint);
            }
            currentStartAngle += pie.getAngle();
        }
        canvas.restore();
        currentStartAngle = mStartAngle;
        for (int i = 0; i < mPieData.size(); i++) {
            PieData pie = mPieData.get(i);
            mPaint.setColor(percentTextColor);
            mPaint.setTextSize(percentTextSize);
            mPaint.setTextAlign(Paint.Align.CENTER);
            NumberFormat numberFormat = NumberFormat.getPercentInstance();
            numberFormat.setMinimumFractionDigits(percentDecimal);
            int textPathX = 0;
            int textPathY = 0;
            if (animatedValue > pieAngles[i] - pie.getAngle() / 2 && percentFlag) {
                if (i == angleId) {
                    drawText(canvas, pie, currentStartAngle, numberFormat, true);
                } else {
                    if (pie.getAngle() > minAngle) {
                        drawText(canvas, pie, currentStartAngle, numberFormat, false);
                    }
                }
            }
            currentStartAngle += pie.getAngle();
        }

        mPaint.setColor(centerTextColor);
        mPaint.setTextSize(centerTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        //根据Paint的TextSize计算Y轴的值
        mPoint.x = 0;
        mPoint.y = 0;
        String[] strings = new String[]{name + ""};
        if (strings.length == 1)
            textCenter(strings, mPaint, canvas, mPoint, Paint.Align.CENTER);
    }

    private void textCenter(String[] strings, Paint paint, Canvas canvas, Point point, Paint.Align align) {
        paint.setTextAlign(align);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int length = strings.length;
        float total = (length - 1) * (-top + bottom) + (-fontMetrics.ascent + fontMetrics.descent);
        float offset = total / 2 - bottom;
        for (int i = 0; i < length; i++) {
            float yAxis = -(length - i - 1) * (-top + bottom) + offset;
            canvas.drawText(strings[i], point.x, point.y + yAxis, paint);
//            Log.d("TAG",mPaint.measureText(strings[i])+":"+strings[i]);
        }
    }

    private void drawText(Canvas canvas, PieData pie, float currentStartAngle, NumberFormat numberFormat, boolean b) {
    }

    private void drawArc(Canvas canvas, float currentStartAngle, float drawAngle, PieData pie, RectF rectFF, RectF rectFTraF, RectF reatFWhite, Paint mPaint) {
    }
}
