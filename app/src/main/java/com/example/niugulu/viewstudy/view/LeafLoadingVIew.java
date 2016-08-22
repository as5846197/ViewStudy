package com.example.niugulu.viewstudy.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.RectF;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by zhangcaoyang on 16/8/22.
 */
public class LeafLoadingView extends View {
    private static final int DEFAULT_BG_OUTER = 0xfffde399;
    private static final String DEFAULT_WHITE = "#fffefd";
    private static final String DEFAULT_BG_INNER = "#ffa800";
    private static final String DEFAULT_BG_FAN = "#fcce5b";
    private Paint innerPaint;
    private Paint outerPaint;
    private Paint fanPaint;
    private Paint fanBgPaint;

    private int mWidth;
    private int mHeight;

    private float outerRadius;
    private float innerRadius;
    private float fanBgRadius;

    private RectF outerCircle;
    private RectF outerRectangle;
    private RectF innerCircle;
    private RectF innerRectangle;
    private RectF fanRect;

    private Path mPath;
    private Path nPath;

    private ValueAnimator valueAnimator;

    private float maxProgress = 100;
    private float currentProgress;

    private float firstStepTime;
    private float secondStepTime;
    private float thirdStepTime;

    public LeafLoadingView(Context context) {
        super(context);
    }

    public LeafLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initValueAnimator();
    }

    public LeafLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        innerPaint = new Paint();
        outerPaint = new Paint();
        fanPaint = new Paint();
        fanBgPaint = new Paint(fanPaint);
        innerPaint.setAntiAlias(true);
        innerPaint.setColor(Color.parseColor(DEFAULT_BG_INNER));
        innerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        outerPaint.setAntiAlias(true);
        outerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        outerPaint.setColor(DEFAULT_BG_OUTER);
        fanPaint.setAntiAlias(true);
        fanPaint.setStyle(Paint.Style.FILL);
        fanPaint.setColor(Color.parseColor(DEFAULT_WHITE));
        fanBgPaint.setColor(Color.parseColor(DEFAULT_BG_FAN));
    }

    private void initValueAnimator() {
        valueAnimator = ValueAnimator.ofFloat(0, 100);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentProgress = (float) animation.getAnimatedValue() / maxProgress;
                invalidate();
            }
        });
        valueAnimator.setDuration(5000);
        valueAnimator.start();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        outerRadius = ((w / 10) < (h / 2)) ? w / 10 : h / 2;
        innerRadius = outerRadius * 0.8f;

        outerCircle = new RectF(-outerRadius, -outerRadius, outerRadius, outerRadius);
        outerRectangle = new RectF(0, -outerRadius, 8 * outerRadius, outerRadius);

        innerCircle = new RectF(-innerRadius, -innerRadius, innerRadius, innerRadius);
        innerRectangle = new RectF(-1, -innerRadius, 8 * outerRadius, innerRadius);

        fanRect = new RectF(7 * outerRadius, -outerRadius, 9 * outerRadius, outerRadius);
        mPath = new Path();
        nPath = new Path();
        fanBgRadius = outerRadius * 0.8f;
        mPath.moveTo(8 * outerRadius, -fanBgRadius);
        mPath.cubicTo(8 * outerRadius + fanBgRadius / 3 * 2, -fanBgRadius / 8 * 7, 8 * outerRadius + fanBgRadius / 10, -fanBgRadius / 10, 8 * outerRadius, -7);
        nPath.moveTo(8 * outerRadius, -fanBgRadius);
        nPath.cubicTo(8 * outerRadius - fanBgRadius / 3 * 2, -fanBgRadius / 8 * 7, 8 * outerRadius - fanBgRadius / 10, -fanBgRadius / 10, 8 * outerRadius, -7);
        mPath.addPath(nPath);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mWidth / 10, mHeight / 2);

        canvas.drawArc(outerCircle, 90, 180, true, outerPaint);
        canvas.drawRect(outerRectangle, outerPaint);

        drawInnerCircle(canvas);

        canvas.drawArc(fanRect, 90, 360, true, fanPaint);

        canvas.save();

        canvas.scale(0.9f, 0.9f, 8 * outerRadius, 0);
        canvas.drawArc(fanRect, 90, 360, true, fanBgPaint);
        canvas.restore();

        canvas.save();
        for (float i = 0; i <= 270; i = i + 90) {
            canvas.rotate(i, 8 * outerRadius, 0);
            canvas.drawPath(mPath, fanPaint);
        }
        canvas.restore();
        canvas.drawCircle(8 * outerRadius, 0, 5, fanPaint);
    }

    private void drawInnerCircle(Canvas canvas) {
        firstStepTime = innerRadius / (innerRadius + 8 * outerRadius);
        if (currentProgress > firstStepTime) {
            canvas.drawArc(innerCircle, 90, 180, true, innerPaint);
            drawInnerRectangle(canvas);
        } else {
            canvas.drawArc(innerCircle, 180 - 90 * currentProgress / firstStepTime, 180 * currentProgress / firstStepTime, false, innerPaint);
        }
    }

    private void drawInnerRectangle(Canvas canvas) {
        secondStepTime = 7f/8f*(1-firstStepTime)+firstStepTime;
        if (currentProgress > secondStepTime) {
//            canvas.drawRect(innerRectangle,innerPaint);
//            canvas.drawRect(-1, -innerRadius, 8 * outerRadius * (currentProgress - firstStepTime), innerRadius, innerPaint);
        } else {
            canvas.drawRect(-1, -innerRadius, 8 * outerRadius * (currentProgress-firstStepTime)/secondStepTime, innerRadius, innerPaint);

        }
    }


}
