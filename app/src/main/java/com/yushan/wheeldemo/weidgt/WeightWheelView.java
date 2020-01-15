package com.yushan.wheeldemo.weidgt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.yushan.wheeldemo.R;

import java.util.ArrayList;

/**
 * autour : yushan
 * date : 2019/1/10
 * description :
 */


public class WeightWheelView extends View {

    private int barInterval;
    private int barWidth;
    private int bottom_text_size;
    private int center_line_color;
    private int bottom_line_color;
    private int bottom_text_color;
    private Paint mBottomTextPaint;
    private Paint mBottomLinePaint;
    private int paddingTop;
    private int paddingLeft;
    private int paddingBottom;
    private int paddingRight;
    private int defaultHeight = dp2Px(85);
    private int bottom_view_height = dp2Px(30);
    private float scaleTimes = 1;
    private float lastX = 0;
    private float lastY = 0;
    private int measureWidth = 0;
    //这是最初的的位置
    private float startOriganalX = 0;
    private HorizontalScrollRunnable horizontalScrollRunnable;
    //临时滑动的距离
    private float tempLength = 0;
    private long startTime = 0;
    private boolean isFling = false;
    private float dispatchTouchX = 0;
    private float dispatchTouchY = 0;
    //是否到达边界
    private boolean isBoundary = false;

    // 最大刻度
    private int maxSize = 50 * 10;
    private Paint mCenterLinePaint;
    private int centerPosition;
    private int refreshSize;
    private Matrix mMatrix;
    private Paint mBitmapPaint;
    private Paint mBarPaint;
    private BitmapShader mBitmapShader;
    private int bitmapMin;
    private float scale = 1.0f;
    private Bitmap backgroundBitmap;
    private Bitmap middleBitmap;
    private Bitmap upBitmap;
    private int pos;
    private Handler mHandler;
    private boolean setStart = true;

    public WeightWheelView(Context context) {
        this(context, null);
    }

    public WeightWheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeightWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.barchar_style);
        barInterval = (int) typedArray.getDimension(R.styleable.barchar_style_barInterval, dp2Px(2));
        center_line_color = typedArray.getColor(R.styleable.barchar_style_bar_color, Color.parseColor("#333333"));
        barWidth = (int) typedArray.getDimension(R.styleable.barchar_style_barWidth, dp2Px(2));

        bottom_text_size = (int) typedArray.getDimension(R.styleable.barchar_style_bottom_text_size, sp2Px(9));
        bottom_text_color = typedArray.getColor(R.styleable.barchar_style_bottom_text_color, Color.parseColor("#c7c7cc"));
        bottom_line_color = typedArray.getColor(R.styleable.barchar_style_bottom_line_color, Color.parseColor("#c7c7cc"));
        typedArray.recycle();

        initPaint();
        initBitmap();
    }

    private int dp2Px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private int sp2Px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void initPaint() {

        mBottomTextPaint = new Paint();
        mBottomTextPaint.setTextSize(bottom_text_size);
        mBottomTextPaint.setColor(bottom_text_color);
        mBottomTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mBottomTextPaint.setStyle(Paint.Style.FILL);
        mBottomTextPaint.setDither(true);

        mBottomLinePaint = new Paint();
        mBottomLinePaint.setColor(bottom_line_color);
        mBottomLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mBottomLinePaint.setStyle(Paint.Style.FILL);
        mBottomLinePaint.setDither(true);
        //设置底部线的宽度
        mBottomLinePaint.setStrokeWidth(dp2Px(0.6f));

        mCenterLinePaint = new Paint();
        mCenterLinePaint.setColor(center_line_color);
        mCenterLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mCenterLinePaint.setStyle(Paint.Style.FILL);
        mCenterLinePaint.setDither(true);

        mCenterLinePaint.setStrokeWidth(dp2Px(1f));

        mBarPaint = new Paint();
        mBarPaint.setColor(Color.parseColor("#f58c28"));
        mBarPaint.setStrokeCap(Paint.Cap.ROUND);
        mBarPaint.setStyle(Paint.Style.FILL);
        mBarPaint.setDither(true);

        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setColor(Color.BLUE);
        mBitmapPaint.setStrokeCap(Paint.Cap.ROUND);
        mBitmapPaint.setStyle(Paint.Style.FILL);
        mBitmapPaint.setAntiAlias(true);
    }

    private void initBitmap() {
        // 创建Bitmap渲染对象
        backgroundBitmap = drawable2Bitmap(getResources().getDrawable(R.drawable.icon_food_fist));
        middleBitmap = drawable2Bitmap(getResources().getDrawable(R.drawable.bg_food_fist));
        upBitmap = drawable2Bitmap(getResources().getDrawable(R.drawable.icon_fist));

        bitmapMin = Math.min(backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
    }

    //进行滑动的边界处理
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("TAG", "MyBarChartView===dispatchTouchEvent==" + ev.getAction());
        int dispatchCurrX = (int) ev.getX();
        int dispatchCurrY = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //父容器不拦截点击事件，子控件拦截点击事件。如果不设置为true,外层会直接拦截，从而导致motionEvent为cancle
                getParent().requestDisallowInterceptTouchEvent(true);
                dispatchTouchX = getX();
                dispatchTouchY = getY();
                break;
            case MotionEvent.ACTION_MOVE:

                float deltaX = dispatchCurrX - dispatchTouchX;
                float deltaY = dispatchCurrY - dispatchTouchY;
                if (Math.abs(deltaY) - Math.abs(deltaX) > 0) {//竖直滑动的父容器拦截事件
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                //这是向右滑动，如果是滑动到边界，那么就让父容器进行拦截
                if ((dispatchCurrX - dispatchTouchX) > 0 && startOriganalX == 0) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else if ((dispatchCurrX - dispatchTouchX) < 0 && startOriganalX == -getMoveLength()) {//这是向右滑动
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        dispatchTouchX = dispatchCurrX;
        dispatchTouchY = dispatchCurrY;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isBoundary = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                startTime = System.currentTimeMillis();
                //当点击的时候，判断如果是在fling的效果的时候，就停止快速滑动
                if (isFling) {
                    removeCallbacks(horizontalScrollRunnable);
                    tempLength = 0;
                    isFling = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float currX = event.getX();
                float currY = event.getY();
                startOriganalX += currX - lastX;

                //这是向右滑动
                if ((currX - lastX) > 0) {
                    Log.e("TAG", "向右滑动");
                    if (startOriganalX > measureWidth / 2) {
                        startOriganalX = measureWidth / 2 - barWidth / 2;
                        isBoundary = true;
                    }

                    centerPosition = (int) (measureWidth / 2 - startOriganalX) / (barWidth + barInterval);

                    if (centerPosition - 100 <= refreshSize) {
                        Log.e("yushan", "refreshSize:" + refreshSize + "  maxSize:" + maxSize);
                        if (refreshSize > 0) {
                            refreshSize -= 50;
                        } else {
                            refreshSize = 0;
                        }
                    }

                } else {//这是向右滑动
                    Log.e("TAG", "向左滑动");
                    if (Math.abs(startOriganalX) > getMoveLength() + (measureWidth + barInterval) / 2) {
                        startOriganalX = -(getMoveLength() + (measureWidth + barInterval) / 2);
                    }

                    centerPosition = (int) (measureWidth / 2 - startOriganalX) / (barWidth + barInterval);

                    if (refreshSize + 200 - centerPosition  <= 100) {
                        Log.e("yushan", "refreshSize:" + refreshSize + "  maxSize:" + maxSize);
                        if (refreshSize < maxSize) {
                            refreshSize += 50;
                        } else {
                            refreshSize = maxSize;
                        }
                    }
                }

                if (centerPosition % 10 == 0) {
                    scale = 1.0f;
                } else {
                    scale = (centerPosition % 10) / 10f;
                }

                tempLength = currX - lastX;
                Log.e("yushan", "startOriganalX:" + startOriganalX + "  barWidth:" + scale + "  measureWidth / 2:" + (centerPosition % 10));
                //如果数据量少，根本没有充满横屏，就没必要重新绘制，
                if (measureWidth < maxSize * (barWidth + barInterval)) {
                    invalidate();
                }
                lastX = currX;
                lastY = currY;
                break;
            case MotionEvent.ACTION_UP:
                long endTime = System.currentTimeMillis();
                //计算猛滑动的速度，如果是大于某个值，并且数据的长度大于整个屏幕的长度，那么就允许有flIng后逐渐停止的效果
                float speed = tempLength / (endTime - startTime) * 1000;
                if (Math.abs(speed) > 100 && Math.abs(speed) < 1000 && !isFling && measureWidth < maxSize * (barWidth + barInterval)) {
                    this.post(horizontalScrollRunnable = new HorizontalScrollRunnable(speed));

                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (startOriganalX == 0) {
            startOriganalX = measureWidth / 2 - barWidth / 2;
        }

        int startX = (int) (paddingLeft + startOriganalX);
        int endY = defaultHeight - bottom_text_size;

        backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap, bitmapMin, bitmapMin, false);
        upBitmap = Bitmap.createScaledBitmap(upBitmap, bitmapMin, bitmapMin, false);

        if (onWeightWheelChangedListener != null) {
            onWeightWheelChangedListener.weightWheelChanged(centerPosition);
        }

        for (int i = refreshSize + 0; i <= refreshSize + 200; i++) {

            if (i > maxSize) {
                break;
            }

            int startY = (int) (defaultHeight - bottom_view_height);
            //绘制下面的文字
            float bottomTextWidth = mBottomTextPaint.measureText(i / 10 + "");
            float bottomStartX = startX + (barWidth + barInterval) * i;
            Rect rect = new Rect();
            mBottomTextPaint.getTextBounds(i + "", 0, (i + "").length(), rect);
            float bottomStartY = defaultHeight;//rect.height()是获取文本的高度;

            if (i % 10 == 0) {
                //绘制线
                drawBottomLine(canvas, bottomStartX + barWidth / 2, bottomStartY - bottom_text_size, dp2Px(0));
                //绘制底部的文字
                drawBottomText(canvas, i / 10 + "", bottomStartX - bottomTextWidth / 2, bottomStartY);
            } else if (i % 10 == 5) {
                //绘制线
                drawBottomLine(canvas, bottomStartX + barWidth / 2, bottomStartY - bottom_text_size, dp2Px(5));

                if (i / 10 < centerPosition / 10) {
                    middleBitmap = Bitmap.createScaledBitmap(middleBitmap, bitmapMin, bitmapMin, false);
                    canvas.drawBitmap(drawFistImage(), bottomStartX + barWidth / 2 - bitmapMin / 2, bottomStartY - bottom_text_size - dp2Px(60), null);
                } else if (i / 10 == centerPosition / 10) {

                    if (scale == 1.0f) {
                        canvas.drawBitmap(upBitmap, bottomStartX + barWidth / 2 - bitmapMin / 2, bottomStartY - bottom_text_size - dp2Px(60), mBarPaint);
                    } else {
                        middleBitmap = Bitmap.createScaledBitmap(middleBitmap, (int) (bitmapMin * scale), bitmapMin, false);
                        canvas.drawBitmap(drawFistImage(), bottomStartX + barWidth / 2 - bitmapMin / 2, bottomStartY - bottom_text_size - dp2Px(60), null);
                        canvas.drawBitmap(upBitmap, bottomStartX + barWidth / 2 - bitmapMin / 2, bottomStartY - bottom_text_size - dp2Px(60), mBarPaint);
                    }
                } else {
                    canvas.drawBitmap(upBitmap, bottomStartX + barWidth / 2 - bitmapMin / 2, bottomStartY - bottom_text_size - dp2Px(60), mBarPaint);
                }

            } else {
                //绘制线
                drawBottomLine(canvas, bottomStartX + barWidth / 2, bottomStartY - bottom_text_size, dp2Px(10));
            }

        }

        drawCenterLine(canvas, endY, dp2Px(0));
    }

    private void drawBottomLine(Canvas canvas, float bottomStartX, float bottomStartY, float startDis) {
        canvas.drawLine(bottomStartX, bottomStartY - startDis, bottomStartX, bottomStartY - dp2Px(20), mBottomLinePaint);
    }

    private void drawNoDataText(Canvas canvas) {
        String text = "loading...";
        float textWidth = mBottomTextPaint.measureText(text);
        canvas.drawText(text, measureWidth / 2 - textWidth / 2, defaultHeight / 2 - 10, mBottomTextPaint);
    }

    //绘制中心bar
    private void drawCenterLine(Canvas canvas, int bottomStartY, int startDis) {
        Rect mRect = new Rect((measureWidth - barWidth) / 2, bottomStartY, (measureWidth - barWidth) / 2 + barWidth, bottomStartY - dp2Px(20));
        canvas.drawRect(mRect, mCenterLinePaint);
    }

    private void drawBottomText(Canvas canvas, String text, float bottomStartX, float bottomStartY) {
        canvas.drawText(text, bottomStartX, bottomStartY, mBottomTextPaint);
    }

    private Bitmap drawFistImage() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap finalBmp = Bitmap.createBitmap(middleBitmap.getWidth(), middleBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalBmp);
        canvas.drawBitmap(backgroundBitmap, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(middleBitmap, 0, 0, paint);

        return finalBmp;
    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;

        if (widthMode == MeasureSpec.EXACTLY) {
            measureWidth = width = widthSize;
        } else {
            width = getAndroiodScreenProperty().get(0);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            defaultHeight = height = heightSize;
        } else {
            height = defaultHeight;
        }
        setMeasuredDimension(width, height);
        paddingTop = getPaddingTop();
        paddingLeft = getPaddingLeft();
        paddingBottom = getPaddingBottom();
        paddingRight = getPaddingRight();

        if (!setStart) {
            setStart = false;
            startOriganalX = measureWidth / 2 - barWidth / 2;
        }
    }

    private ArrayList<Integer> getAndroiodScreenProperty() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        int screenHeight = (int) (height / density);// 屏幕高度(dp)

        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(screenWidth);
        integers.add(screenHeight);
        return integers;
    }

    private int getMoveLength() {
        return (barWidth + barInterval) * maxSize - measureWidth;
    }

    public boolean isBoundary() {
        return isBoundary;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    private class HorizontalScrollRunnable implements Runnable {

        private float speed = 0;

        public HorizontalScrollRunnable(float speed) {
            this.speed = speed;
        }

        @Override
        public void run() {
            if (Math.abs(speed) < 50) {
                isFling = false;
                return;
            }
            isFling = true;
            startOriganalX += speed / 15;
            speed = speed / 1.15f;
            //这是向右滑动
            if ((speed) > 0) {
                Log.e("TAG", "向右滑动");
                if (startOriganalX > measureWidth / 2) {
                    startOriganalX = measureWidth / 2 - barWidth / 2;
                    isBoundary = true;
                }

                centerPosition = (int) (measureWidth / 2 - startOriganalX) / (barWidth + barInterval);

                if (centerPosition - 100 <= refreshSize) {
                    Log.e("yushan", "refreshSize:" + refreshSize + "  maxSize:" + maxSize);
                    if (refreshSize > 0) {
                        refreshSize -= 50;
                    } else {
                        refreshSize = 0;
                    }
                }

            } else {//这是向右滑动
                Log.e("TAG", "向左滑动");
                if (Math.abs(startOriganalX) > getMoveLength() + (measureWidth + barInterval) / 2) {
                    startOriganalX = -(getMoveLength() + (measureWidth + barInterval) / 2);
                }

                centerPosition = (int) (measureWidth / 2 - startOriganalX) / (barWidth + barInterval);

                if (refreshSize + 200 - centerPosition  <= 100) {
                    Log.e("yushan", "refreshSize:" + refreshSize + "  maxSize:" + maxSize);
                    if (refreshSize < maxSize) {
                        refreshSize += 50;
                    } else {
                        refreshSize = maxSize;
                    }
                }
            }

            if (centerPosition % 10 == 0) {
                scale = 1.0f;
            } else {
                scale = (centerPosition % 10) / 10f;
            }

            postDelayed(this, 20);
            invalidate();
        }
    }

    private OnWeightWheelChangedListener onWeightWheelChangedListener;

    public void setOnWeightWheelChangedListener(OnWeightWheelChangedListener onWeightWheelChangedListener) {
        this.onWeightWheelChangedListener = onWeightWheelChangedListener;
    }

    public interface OnWeightWheelChangedListener {
        public void weightWheelChanged(int weight);
    }
}


