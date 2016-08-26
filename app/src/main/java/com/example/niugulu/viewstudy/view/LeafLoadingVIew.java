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
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.example.niugulu.viewstudy.R;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhangcaoyang on 16/8/22.
 */
public class LeafLoadingView extends View {
    private static final int DEFAULT_BG_OUTER = 0xfffde399; // 外部边框的背景颜色
    private static final String DEFAULT_WHITE = "#fffefd";
    private static final int DEFAULT_BG_INNER = 0xffffa800;  //内部进度条的颜色
    private static final String DEFAULT_BG_FAN = "#fcce5b";  // 风扇 扇叶的颜色

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 600;

    //振幅的强度
    private static final int LOW_AMPLITUDE = 0;
    private static final int NORMAL_AMPLITUDE = 1;
    private static final int HIGH_AMPLITUDE = 2;

    private static final int DEFAULT_AMPLITUDE = 20;

    // 叶子飘动一个周期所花的时间
    private static final int LEAF_FLOAT_TIME = 2000;

    private Resources mResources;

    // 定义画笔
    private Paint innerPaint;
    private Paint outerPaint;
    private Paint fanPaint;
    private Paint fanBgPaint;
    private Paint textPaint;

    // view的大小 和 “100%”的高度
    private int mWidth;
    private int mHeight;
    private float textHeight;

    //外部圆半径 内部圆半径  风扇背景的半径
    private float outerRadius;
    private float innerRadius;
    private float fanBgRadius;

    //各种路径
    private RectF outerCircle;
    private RectF outerRectangle;
    private RectF innerCircle;
    private RectF innerRectangle;
    private RectF fanWhiteRect;

    //电风扇 扇叶路径
    private Path mPath;
    private Path nPath;

    // 定义结束的属性动画
    private ValueAnimator valueAnimator;
    private ValueAnimator completedAnimator;

    //进度值
    private float maxProgress = 100;
    private float currentProgress;
    private float completedProgress;

    //先填充半圆的进度 和 长方形的时间
    private float firstStepTime;
    private float secondStepTime;

    //和叶片相关
    private Bitmap mLeafBitmap;
    private int mLeafWidth;
    private int mLeafHeight;
    private int mLeafFlyTime;
    private int mAddTime;
    private float mAmplitudeDisparity;

    //判断是否加载完毕 然后执行结束动画
    private boolean isFinished;

    //精度条的总长度
    private float mProgressWidth;

    private List<Leaf> leafInfos;

    //对 外面的边框缓存
    private WeakReference<Bitmap> outBorderBitmapCache;

    public LeafLoadingView(Context context) {
        super(context);
    }

    public LeafLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResources = getResources();
        init();
        initValueAnimator();
        initBitmap();
        leafInfos = new LeafFactory().generateLeafs();
    }

    public LeafLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        innerPaint = new Paint();
        innerPaint.setAntiAlias(true);
        innerPaint.setColor(DEFAULT_BG_INNER);  //抗锯齿
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
        //对字体测量 让字体“100%”居中
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textHeight = (-fontMetrics.ascent - fontMetrics.descent) / 2;

        mLeafFlyTime = LEAF_FLOAT_TIME;
        mAmplitudeDisparity = DEFAULT_AMPLITUDE;
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
        completedAnimator.setDuration(500);
        completedAnimator.setInterpolator(new AccelerateInterpolator());
        completedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                completedProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSize = Math.min(DEFAULT_HEIGHT, heightSize);
        }

        if (widthSpecMode == MeasureSpec.AT_MOST) {
            widthSize = Math.min(DEFAULT_WIDTH, widthSize);
        }
        setMeasuredDimension(widthSize, heightSize);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        outerRadius = ((w / 10) < (h / 2)) ? w / 10 : h / 2;
        innerRadius = outerRadius * 0.8f;

        mProgressWidth = innerRadius + outerRadius * 7;

        outerCircle = new RectF(-outerRadius, -outerRadius, outerRadius, outerRadius);
        outerRectangle = new RectF(0, -outerRadius, 8 * outerRadius, outerRadius);

        innerCircle = new RectF(-innerRadius, -innerRadius, innerRadius, innerRadius);
        innerRectangle = new RectF(-1, -innerRadius, 8 * outerRadius, innerRadius);

        fanWhiteRect = new RectF(7 * outerRadius, -outerRadius, 9 * outerRadius, outerRadius);

        fanBgRadius = outerRadius * 0.8f;

        mPath = new Path();
        nPath = new Path();
        mPath.moveTo(8 * outerRadius, -fanBgRadius);
        mPath.cubicTo(8 * outerRadius + fanBgRadius / 3 * 2, -fanBgRadius / 8 * 7, 8 * outerRadius + fanBgRadius / 10, -fanBgRadius / 10, 8 * outerRadius, -7);
        nPath.moveTo(8 * outerRadius, -fanBgRadius);
        nPath.cubicTo(8 * outerRadius - fanBgRadius / 3 * 2, -fanBgRadius / 8 * 7, 8 * outerRadius - fanBgRadius / 10, -fanBgRadius / 10, 8 * outerRadius, -7);
        mPath.addPath(nPath);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Bitmap outBorderBitmap = outBorderBitmapCache == null ? null : outBorderBitmapCache.get();

        if (outBorderBitmap == null || outBorderBitmap.isRecycled()) {
            outBorderBitmap = getBitmap();
            outBorderBitmapCache = new WeakReference<Bitmap>(outBorderBitmap);
        }

        int sc = canvas.saveLayer(0, 0, mWidth, mHeight, null, Canvas.MATRIX_SAVE_FLAG |
                Canvas.CLIP_SAVE_FLAG |
                Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                Canvas.CLIP_TO_LAYER_SAVE_FLAG);

        canvas.drawBitmap(outBorderBitmap, 0, 0, outerPaint);
//        canvas.translate(mWidth / 10, mHeight / 2);
        drawLeaf(canvas);
        canvas.restoreToCount(sc);

        canvas.translate(mWidth / 10, mHeight / 2);

        //画内部圆
        drawInnerCircle(canvas);

        //画风扇白色的背景
        canvas.drawArc(fanWhiteRect, 90, 360, true, fanPaint);

        //画风扇的黄色背景
        canvas.save();
        canvas.scale(0.9f, 0.9f, 8 * outerRadius, 0);
        canvas.drawArc(fanWhiteRect, 90, 360, true, fanBgPaint);
        canvas.restore();

        //画扇叶
        canvas.save();
        drawFan(canvas, 0, true);
        canvas.restore();

        //结束动画
        if (isFinished) {
            showCompletedText(canvas);
        }

    }

    /**
     * 画叶子
     */
    private void drawLeaf(Canvas canvas) {

        long currentTime = System.currentTimeMillis();
        canvas.save();
        canvas.translate(mWidth / 10 - innerRadius, mHeight / 2 - outerRadius);
        for (Leaf leaf : leafInfos) {
            if (currentTime > leaf.startTime && leaf.startTime != 0) {
                getLocation(leaf, currentTime);
                // 通过时间关联旋转角度，则可以直接通过修改LEAF_ROTATE_TIME调节叶子旋转快慢
                float rotateFraction = ((currentTime - leaf.startTime) % mLeafFlyTime)
                        / (float) mLeafFlyTime;
                int angle = (int) (rotateFraction * 360);
                int rotate = leaf.rotateDirection == 0 ? angle + leaf.rotateAngle : -angle
                        + leaf.rotateAngle;

                Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postTranslate(leaf.x, leaf.y);

                matrix.postRotate(rotate, leaf.x + mLeafWidth / 2, leaf.y + mLeafHeight / 2);
                outerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                canvas.drawBitmap(mLeafBitmap, matrix, outerPaint);
                outerPaint.setXfermode(null);
            } else {
                continue;
            }

        }
        canvas.restore();
    }

    //获取叶子当前的位置
    public void getLocation(Leaf leaf, long currentTime) {
        long intervalTime = currentTime - leaf.startTime;
        if (intervalTime < 0) {
            return;
        } else if (intervalTime > mLeafFlyTime) {
            leaf.startTime = System.currentTimeMillis()
                    + new Random().nextInt(mLeafFlyTime);
        }
        float fraction = (float) intervalTime / mLeafFlyTime;
        leaf.x = getLeafX(fraction);
        leaf.y = getLeafY(leaf);
    }

    //获取叶子x坐标
    public float getLeafX(float fraction) {
        return mProgressWidth * (1 - fraction);
    }

    //获取叶子y坐标
    public float getLeafY(Leaf leaf) {
        float w = (float) (2 * Math.PI / mProgressWidth);
        float a = outerRadius / 2;
        switch (leaf.type) {
            case LOW_AMPLITUDE:
                // 小振幅 ＝ 中等振幅 － 振幅差
                a = -mAmplitudeDisparity;
                break;
            case NORMAL_AMPLITUDE:
                break;
            case HIGH_AMPLITUDE:
                // 小振幅 ＝ 中等振幅 + 振幅差
                a = +mAmplitudeDisparity;
                break;
            default:
                break;
        }

        return (float) (a * Math.sin((w * leaf.x))) - mLeafHeight / 2 + outerRadius;
    }


    //先填充半圆
    private void drawInnerCircle(Canvas canvas) {
        firstStepTime = innerRadius / (innerRadius + 7 * outerRadius);
        if (currentProgress > firstStepTime) {
            canvas.drawArc(innerCircle, 90, 180, true, innerPaint);
            drawInnerRectangle(canvas);
        } else {
            canvas.drawArc(innerCircle, 180 - 90 * currentProgress / firstStepTime, 180 * currentProgress / firstStepTime, false, innerPaint);
        }
    }

    //填充剩下的长方形
    private void drawInnerRectangle(Canvas canvas) {
        secondStepTime = 1 - firstStepTime;
        if (currentProgress >= 1) {
            if (!isFinished) {
                isFinished = true;
                completedAnimator.start();
            }
        } else {
            canvas.drawRect(-1, -innerRadius, 7 * outerRadius * (currentProgress - firstStepTime) / secondStepTime, innerRadius, innerPaint);

        }
    }

    //结束时动画 展示“100%”字样
    private void showCompletedText(Canvas canvas) {
        canvas.drawRect(-1, -innerRadius, (7 + completedProgress) * outerRadius, innerRadius, innerPaint);
        canvas.drawArc(fanWhiteRect, 90, 360, true, fanPaint);

        canvas.save();

        canvas.scale(0.9f, 0.9f, 8 * outerRadius, 0);
        canvas.drawArc(fanWhiteRect, 90, 360, true, fanBgPaint);
        canvas.restore();
        if (completedProgress == 1) {
            textPaint.setTextSize(60);
            canvas.drawText("100%", 8 * outerRadius, textHeight, textPaint);
        } else {
            drawFan(canvas, completedProgress, false);
            textPaint.setTextSize(60 * completedProgress);
            canvas.drawText("100%", 8 * outerRadius, textHeight, textPaint);
        }

    }

    //画扇叶
    private void drawFan(Canvas canvas, float completedProgress, boolean isNeedRotate) {
        canvas.save();
        if (isNeedRotate) {
            canvas.rotate(-currentProgress * 360 * 5, 8 * outerRadius, 0);
        }
        if (completedProgress != 0) {
            canvas.scale(1 - completedProgress, 1 - completedProgress, 8 * outerRadius, 0);
        }
        for (float i = 0; i <= 270; i = i + 90) {
            canvas.rotate(i, 8 * outerRadius, 0);
            canvas.drawPath(mPath, fanPaint);
        }
        canvas.drawCircle(8 * outerRadius, 0, 5 * (1 - completedProgress), fanPaint);
        canvas.restore();
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

        public Leaf getLeaf() {
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
            mAddTime += random.nextInt((int) (mLeafFlyTime * 1.5));
            leaf.startTime = System.currentTimeMillis() + mAddTime;
            return leaf;
        }

        // 根据最大叶子数产生叶子信息
        public List<Leaf> generateLeafs() {
            return generateLeafs(MAX_LEAFS);
        }

        // 根据传入的叶子数量产生叶子信息
        public List<Leaf> generateLeafs(int leafSize) {
            List<Leaf> leafs = new LinkedList<Leaf>();
            for (int i = 0; i < leafSize; i++) {
                leafs.add(getLeaf());
            }
            return leafs;
        }
    }

    /**
     * 绘制外部背景
     *
     * @return
     */
    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(mWidth / 10, mHeight / 2);

        canvas.drawArc(outerCircle, 90, 180, true, outerPaint);
        canvas.drawRect(outerRectangle, outerPaint);
        return bitmap;
    }


}
