package com.example.niugulu.viewstudy.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.example.niugulu.viewstudy.R;

import java.util.Random;

/**
 * Created by zhangcaoyang on 16/8/22.
 */
public class LeafLoadingView extends View {
    private static final int DEFAULT_BG_OUTER = 0xfffde399;
    private static final String DEFAULT_WHITE = "#fffefd";
    private static final String DEFAULT_BG_INNER = "#ffa800";
    private static final String DEFAULT_BG_FAN = "#fcce5b";
    private static final int LOW_AMPLITUDE = 0;
    private static final int NORMAL_AMPLITUDE = 1;
    private static final int HIGH_AMPLITUDE = 2;
    // 叶子飘动一个周期所花的时间
    private static final int LEAF_FLOAT_TIME = 3000;

    private Resources mResources;

    private Paint innerPaint;
    private Paint outerPaint;
    private Paint fanPaint;
    private Paint fanBgPaint;
    private Paint textPaint;

    private int mWidth;
    private int mHeight;
    private float textHeight;

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
    private ValueAnimator completedAnimator;

    private float maxProgress = 100;
    private float currentProgress;
    private float completedProgress;

    private float firstStepTime;
    private float secondStepTime;
    private float thirdStepTime;

    private Leaf mLeaf;
    private Bitmap mLeafBitmap;
    private int mLeafWidth;
    private int mLeafHeight;
    private int mLeafFlyTime;

    public LeafLoadingView(Context context) {
        super(context);
    }

    public LeafLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResources = getResources();
        init();
        initValueAnimator();
        initBitmap();
    }

    public LeafLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        innerPaint = new Paint();
        innerPaint.setAntiAlias(true);
        innerPaint.setColor(Color.parseColor(DEFAULT_BG_INNER));
        innerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        outerPaint = new Paint(innerPaint);
        outerPaint.setColor(DEFAULT_BG_OUTER);

        fanPaint = new Paint();
        fanPaint.setAntiAlias(true);
        fanPaint.setStyle(Paint.Style.FILL);
        fanPaint.setColor(Color.parseColor(DEFAULT_WHITE));

        fanBgPaint = new Paint(fanPaint);
        textPaint = new Paint(fanPaint);

        fanBgPaint.setColor(Color.parseColor(DEFAULT_BG_FAN));

        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textHeight = (-fontMetrics.ascent - fontMetrics.descent) / 2;

        mLeaf = new Leaf();
        mLeafFlyTime = LEAF_FLOAT_TIME;
    }

    private void initValueAnimator() {
        valueAnimator = ValueAnimator.ofFloat(0, 100);
        completedAnimator = ValueAnimator.ofFloat(0, 1);
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
        completedAnimator.setDuration(1000);
        completedAnimator.setInterpolator(new AccelerateInterpolator());
        completedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                completedProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        completedAnimator.start();

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

//        canvas.save();
//        for (float i = 0; i <= 270; i = i + 90) {
//            canvas.rotate(i, 8 * outerRadius, 0);
//            canvas.drawPath(mPath, fanPaint);
//        }
//        canvas.restore();
//        canvas.drawCircle(8 * outerRadius, 0, 5, fanPaint);

        showCompletedText(canvas);
        drawLeaf(canvas);
    }

    private void drawLeaf(Canvas canvas) {
        canvas.save();
        canvas.translate(-mWidth/10,-outerRadius);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(8*outerRadius*(1-currentProgress), (float) ( outerRadius*Math.sin((1-currentProgress)*Math.PI*4))-mLeafHeight/2+outerRadius);
        matrix.postRotate(360*currentProgress,8*outerRadius*(1-currentProgress)+mLeafWidth/2,(float) ( outerRadius*Math.sin((1-currentProgress)*Math.PI*4))+outerRadius);
        canvas.drawBitmap(mLeafBitmap, matrix, outerPaint);
        canvas.restore();
    }

    private void drawInnerCircle(Canvas canvas) {
        firstStepTime = innerRadius / (innerRadius + 7 * outerRadius);
        if (currentProgress > firstStepTime) {
            canvas.drawArc(innerCircle, 90, 180, true, innerPaint);
            drawInnerRectangle(canvas);
        } else {
            canvas.drawArc(innerCircle, 180 - 90 * currentProgress / firstStepTime, 180 * currentProgress / firstStepTime, false, innerPaint);
        }
    }

    private void drawInnerRectangle(Canvas canvas) {
        secondStepTime = 1 - firstStepTime;
        if (currentProgress >= 1) {
//            canvas.drawRect(innerRectangle,innerPaint);
//            canvas.drawRect(-1, -innerRadius, 8 * outerRadius * (currentProgress - firstStepTime), innerRadius, innerPaint);
        } else {
            canvas.drawRect(-1, -innerRadius, 7 * outerRadius * (currentProgress - firstStepTime) / secondStepTime, innerRadius, innerPaint);

        }
    }

    private void showCompletedText(Canvas canvas) {
        canvas.drawRect(-1, -innerRadius, (7 + completedProgress) * outerRadius, innerRadius, innerPaint);
        canvas.drawArc(fanRect, 90, 360, true, fanPaint);

        canvas.save();

        canvas.scale(0.9f, 0.9f, 8 * outerRadius, 0);
        canvas.drawArc(fanRect, 90, 360, true, fanBgPaint);
        canvas.restore();
        if (completedProgress == 1) {
            textPaint.setTextSize(60);
            canvas.drawText("100%", 8 * outerRadius, textHeight, textPaint);
        } else {
            canvas.save();
            canvas.scale(1 - completedProgress, 1 - completedProgress, 8 * outerRadius, 0);
            for (float i = 0; i <= 270; i = i + 90) {
                canvas.rotate(i, 8 * outerRadius, 0);
                canvas.drawPath(mPath, fanPaint);
            }
            canvas.drawCircle(8 * outerRadius, 0, 5 * (1 - completedProgress), fanPaint);
            canvas.restore();
            textPaint.setTextSize(60 * completedProgress);
            canvas.drawText("100%", 8 * outerRadius, textHeight, textPaint);
        }

    }

    private void initBitmap() {
        mLeafBitmap = BitmapFactory.decodeResource(mResources, R.drawable.leaf);
        mLeafWidth = mLeafBitmap.getWidth();
        mLeafHeight = mLeafBitmap.getHeight();

    }

    private class Leaf {
        // 在绘制部分的位置
        float x, y;
        // 控制叶子飘动的幅度
        int type;
        // 旋转角度
        int rotateAngle;
        // 旋转方向--0代表顺时针，1代表逆时针
        int rotateDirection;
        // 起始时间(ms)
        long startTime;
    }

    private class LeafFactory {
        private static final int MAX_LEAFS = 8;
        Random random = new Random();

        public Leaf getLeaf(){
            Leaf leaf = new Leaf();
            int randomType = random.nextInt(3);
            int type = NORMAL_AMPLITUDE;
            switch (randomType) {
                case 0:
                    type = LOW_AMPLITUDE;
                    break;
                case 1:
                    break;
                case 2:
                    type = HIGH_AMPLITUDE;
                    break;
                default:
                    break;
            }
            leaf.type = type;
            // 随机起始的旋转角度
            leaf.rotateAngle = random.nextInt(360);
            // 随机旋转方向（顺时针或逆时针）
            leaf.rotateDirection = random.nextInt(2);
//            leaf.startTime
            return leaf;
        }
    }


}
