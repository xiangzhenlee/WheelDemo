package com.yushan.wheeldemo.weidgt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
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


public class FoodCalView extends View {

    private int barInterval;
    private int barWidth;
    private int top_text_size;
    private int bar_color;
    private int bottom_line_color;
    private int top_text_color;
    private Paint mTopTextPaint;
    private Paint mBarPaint;
    private Paint mBottomLinePaint;
    private ArrayList<BarData> innerData = new ArrayList<>();
    private int paddingTop;
    private int paddingLeft;
    private int paddingBottom;
    private int paddingRight;
    private int defaultHeight = dp2Px(250);
    private int bottom_view_height = dp2Px(30);
    private int top_text_height = dp2Px(30);
    private float scaleTimes = 1;
    private float lastX = 0;
    private float lastY = 0;
    private int measureWidth = 0;
    //这是最初的的位置
    private float startOriganalX = 0;
    private HorizontalScrollRunnable horizontalScrollRunnable;
    private Scroll2CenterRunnable scroll2CenterRunnable;
    //临时滑动的距离
    private float tempLength = 0;
    private long startTime = 0;
    private boolean isFling = false;
    private float dispatchTouchX = 0;
    private float dispatchTouchY = 0;
    //是否到达边界
    private boolean isBoundary = false;
    private boolean isMove = false;
    private Paint mHLinePaint;
    private int centerPosition;
    private Paint mCenterBarPaint;
    private Paint mCenterFlagPaint;
    private float nextDis;
    private String moveTo;
    private Context mContext;
    private boolean setStart = true;
    private int maxValue = 1500;

    public FoodCalView(Context context) {
        this(context, null);
    }

    public FoodCalView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoodCalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.barchar_style);
        barInterval = (int) typedArray.getDimension(R.styleable.barchar_style_barInterval, dp2Px(25));
        bar_color = typedArray.getColor(R.styleable.barchar_style_bar_color, Color.WHITE);
        barWidth = (int) typedArray.getDimension(R.styleable.barchar_style_barWidth, dp2Px(16));
        top_text_size = (int) typedArray.getDimension(R.styleable.barchar_style_bottom_text_size, sp2Px(11));
        top_text_color = typedArray.getColor(R.styleable.barchar_style_bottom_text_color, Color.WHITE);
        bottom_line_color = typedArray.getColor(R.styleable.barchar_style_bottom_line_color, Color.parseColor("#ffc4a5"));
        typedArray.recycle();
        initPaint();
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
        mTopTextPaint = new Paint();
        mTopTextPaint.setTextSize(top_text_size);
        mTopTextPaint.setColor(top_text_color);
        mTopTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mTopTextPaint.setStyle(Paint.Style.FILL);
        mTopTextPaint.setDither(true);

        mBarPaint = new Paint();
        mBarPaint.setColor(bar_color);
        mBarPaint.setStrokeCap(Paint.Cap.ROUND);
        mBarPaint.setStyle(Paint.Style.FILL);
        mBarPaint.setDither(true);

        mCenterBarPaint = new Paint();
        mCenterBarPaint.setColor(bar_color);
        mCenterBarPaint.setStrokeCap(Paint.Cap.ROUND);
        mCenterBarPaint.setStyle(Paint.Style.FILL);
        mCenterBarPaint.setDither(true);

        mCenterFlagPaint = new Paint();
        mCenterFlagPaint.setColor(Color.parseColor("#f58c28"));
        mCenterFlagPaint.setStrokeCap(Paint.Cap.ROUND);
        mCenterFlagPaint.setStyle(Paint.Style.FILL);
        mCenterFlagPaint.setDither(true);

        mBottomLinePaint = new Paint();
        mBottomLinePaint.setColor(bottom_line_color);
        mBottomLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mBottomLinePaint.setStyle(Paint.Style.FILL);
        mBottomLinePaint.setDither(true);
        //设置底部线的宽度
        mBottomLinePaint.setStrokeWidth(dp2Px(0.6f));

        mHLinePaint = new Paint();
        mHLinePaint.reset();
        mHLinePaint.setStyle(Paint.Style.STROKE);
        mHLinePaint.setStrokeWidth(dp2Px(0.6f));
        mHLinePaint.setColor(bottom_line_color);
        mHLinePaint.setAntiAlias(true);
        mHLinePaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

    }

    public void setBarChartData(ArrayList<BarData> innerData) {
        this.innerData.clear();
        this.innerData.addAll(innerData);

        scaleTimes = (maxValue * 2) / (float) (defaultHeight - bottom_view_height - top_text_height);
        invalidate();
    }

    public void setMaxValue(int value) {
        this.maxValue = value;
    }

    //进行滑动的边界处理
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("yushan", "MyBarChartView===dispatchTouchEvent==" + ev.getAction());
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
        isMove = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                startTime = System.currentTimeMillis();
                //当点击的时候，判断如果是在fling的效果的时候，就停止快速滑动
                if (isFling) {
                    removeCallbacks(horizontalScrollRunnable);
                    isFling = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float currX = event.getX();
                float currY = event.getY();
                startOriganalX += currX - lastX;

                if ((currX - lastX) < 0) {
                    Log.e("yushan", "向左滑动");
                    if (startOriganalX < measureWidth / 2) {
                        startOriganalX = measureWidth / 2;
                        isBoundary = true;
                    }
                    moveTo = "Left";
                    centerPosition = (int) (startOriganalX - measureWidth / 2 + barInterval) / (barWidth + barInterval);
                    nextDis = (startOriganalX - measureWidth / 2) % (barWidth + barInterval);
                } else {
                    if (startOriganalX > measureWidth / 2 + (barWidth + barInterval) * (innerData.size() - 1)) {
                        startOriganalX = measureWidth / 2 + (barWidth + barInterval) * (innerData.size() - 1);
                        isBoundary = true;
                    }
                    Log.e("yushan", "向右滑动");
                    moveTo = "Right";
                    centerPosition = (int) (startOriganalX - measureWidth / 2 + barWidth) / (barWidth + barInterval);

                    nextDis = (startOriganalX - measureWidth / 2) % (barWidth + barInterval);
                }

                tempLength = currX - lastX;

                Log.e("yushan", "startOriganalX:" + startOriganalX + "  barWidth:" + (barWidth + barInterval) + "  measureWidth / 2:" + measureWidth / 2);
                //如果数据量少，根本没有充满横屏，就没必要重新绘制，
                if (measureWidth < innerData.size() * (barWidth + barInterval)) {
                    invalidate();
                }

                lastX = currX;
                lastY = currY;
                break;
            case MotionEvent.ACTION_UP:
                long endTime = System.currentTimeMillis();

                //计算猛滑动的速度，如果是大于某个值，并且数据的长度大于整个屏幕的长度，那么就允许有flIng后逐渐停止的效果
                float speed = tempLength / (endTime - startTime) * 1000;
                if (Math.abs(speed) > 100 && Math.abs(speed) < 1000 && !isFling && measureWidth < innerData.size() * (barWidth + barInterval)) {
                    this.post(horizontalScrollRunnable = new HorizontalScrollRunnable(speed));
                } else if (nextDis > 0) {
                    this.post(scroll2CenterRunnable = new Scroll2CenterRunnable(nextDis));
                }
                isMove = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                isMove = false;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (startOriganalX == 0) {
            startOriganalX = measureWidth / 2;
        }
        int startX = (int) (paddingLeft + startOriganalX - barWidth / 2);
        int endY = defaultHeight;

        if (onFoodDateChangedListener != null) {
            onFoodDateChangedListener.dateWheelChanged(centerPosition);
        }

        drawBottomLine(canvas);

        for (int i = 0; i < innerData.size(); i++) {
            float barHeight = 0;
            if (scaleTimes != 0) {
                float barValue;
                if (innerData.get(i).getCount() > maxValue * 2) {
                    barValue = maxValue * 2;
                } else {
                    barValue = innerData.get(i).getCount();
                }

                barHeight = barValue / scaleTimes;

            }
            int startY = (int) (defaultHeight - bottom_view_height - barHeight);

            //绘制下面的文字
            float bottomTextWidth = mTopTextPaint.measureText(innerData.get(i).date);
            float bottomStartX = startX + barWidth / 2 - bottomTextWidth / 2;
            Rect rect = new Rect();
            mTopTextPaint.getTextBounds(innerData.get(i).getDate(), 0, innerData.get(i).getDate().length(), rect);
            float bottomStartY = defaultHeight - bottom_view_height + 10 + rect.height();//rect.height()是获取文本的高度;

            //绘制线
            drawBottomLine(canvas, startX + barWidth / 2, bottomStartY);

            if (innerData.get(i).count != 0) {
                //绘制bar
                if (i == centerPosition) {
                    drawCenterBar(canvas, startX, startY + dp2Px(30), endY);
                } else {
                    drawBar(canvas, startX, startY + dp2Px(30), endY);
                }
            }

            //绘制底部的文字
            drawTopText(canvas, innerData.get(i).getDate(), bottomStartX, bottomStartY);

            startX = startX - (barWidth + barInterval);
        }

        drawCenterFlag(canvas, endY, dp2Px(6));
    }

    private void drawBottomLine(Canvas canvas) {

        Path path = new Path();

        path.moveTo(paddingLeft, (defaultHeight + bottom_view_height) / 2);
        path.lineTo(innerData.size() * (barWidth + barInterval), (defaultHeight + bottom_view_height) / 2);
        canvas.drawPath(path, mHLinePaint);
    }

    private void drawBottomLine(Canvas canvas, float bottomStartX, float bottomStartY) {
        canvas.drawLine(bottomStartX, bottomStartY + dp2Px(30), bottomStartX, bottomStartY - dp2Px(180), mBottomLinePaint);
    }

    //绘制bar
    private void drawBar(Canvas canvas, int startX, int startY, int endY) {
        Rect mRect = new Rect(startX, startY, startX + barWidth, endY);
        LinearGradient gradient = new LinearGradient(startX, startY, startX + barWidth, endY, Color.WHITE, Color.parseColor("#f58c28"), Shader.TileMode.CLAMP);
        mBarPaint.setShader(gradient);
        canvas.drawCircle(startX + barWidth / 2, startY, barWidth / 2, mBarPaint);
        canvas.drawRect(mRect, mBarPaint);
    }

    //绘制中心bar
    private void drawCenterBar(Canvas canvas, int startX, int startY, int endY) {
        Rect mRect = new Rect(startX, startY, startX + barWidth, endY);
        canvas.drawCircle(startX + barWidth / 2, startY, barWidth / 2, mCenterBarPaint);
        canvas.drawRect(mRect, mCenterBarPaint);
    }

    //绘制中心标识
    private void drawCenterFlag(Canvas canvas, int endY, int sideLength) {
        Path path = new Path();
        path.moveTo(measureWidth / 2, endY - sideLength);// 此点为多边形的起点
        path.lineTo(measureWidth / 2 + sideLength, endY + sideLength);
        path.lineTo(measureWidth / 2 - sideLength, endY + sideLength);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, mCenterFlagPaint);
    }

    private void drawTopText(Canvas canvas, String text, float bottomStartX, float bottomStartY) {
        canvas.drawText(text, bottomStartX, bottomStartY - dp2Px(200), mTopTextPaint);
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
            startOriganalX = measureWidth / 2;
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
        return (barWidth + barInterval) * innerData.size() - measureWidth / 2;
    }

    public boolean isBoundary() {
        return isBoundary;
    }

    public boolean isMove() {
        return isMove;
    }

    public static class BarData {
        private int count;
        private String date;
        private String year;

        public BarData(int count, String date, String year) {
            this.count = count;
            this.date = date;
            this.year = year;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getDate() {
            return date == null ? "" : date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getYear() {
            return year == null ? "" : year;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }

    private class HorizontalScrollRunnable implements Runnable {

        private float speed = 0;

        public HorizontalScrollRunnable(float speed) {
            this.speed = speed;
        }

        @Override
        public void run() {
            if (Math.abs(speed) < 30) {
                isFling = false;
                if (nextDis > 0) {
                    new Handler().post(scroll2CenterRunnable = new Scroll2CenterRunnable(nextDis));
                }
                return;
            }
            isFling = true;
            startOriganalX += speed / 15;
            speed = speed / 1.15f;
            //这是向右滑动
            if ((speed) < 0) {
                Log.e("yushan", "向左滑动");
                if (startOriganalX < measureWidth / 2) {
                    startOriganalX = measureWidth / 2;
                    isBoundary = true;
                }
                moveTo = "Left";
                centerPosition = (int) (startOriganalX - measureWidth / 2 + barInterval) / (barWidth + barInterval);
                nextDis = (startOriganalX - measureWidth / 2) % (barWidth + barInterval);
            } else {
                if (startOriganalX > measureWidth / 2 + (barWidth + barInterval) * (innerData.size() - 1)) {
                    startOriganalX = measureWidth / 2 + (barWidth + barInterval) * (innerData.size() - 1);
                    isBoundary = true;
                }
                Log.e("yushan", "向右滑动");
                moveTo = "Right";
                centerPosition = (int) (startOriganalX - measureWidth / 2 + barWidth) / (barWidth + barInterval);

                nextDis = (startOriganalX - measureWidth / 2) % (barWidth + barInterval);
            }

            postDelayed(this, 20);
            invalidate();
        }
    }

    private class Scroll2CenterRunnable implements Runnable {

        private float dis = 0;
        private float move = 0;
        private String flag;

        public Scroll2CenterRunnable(float dis) {
            if (dis > (barInterval + barWidth) / 2) {
                flag = "add";
                this.dis = (barInterval + barWidth) - dis;
            } else {
                flag = "sub";
                this.dis = dis;

            }
        }

        @Override
        public void run() {
            if (move >= dis) {
                isFling = false;
                return;
            }

            isFling = true;
            if ("add".equals(flag)) {
                startOriganalX += 1;
            } else {
                startOriganalX -= 1;
            }

            move += 1;

            if ("Left".equals(moveTo)) {
                Log.e("yushan", "向左滑动");
                if (startOriganalX < measureWidth / 2) {
                    startOriganalX = measureWidth / 2;
                    isBoundary = true;
                }

                centerPosition = (int) (startOriganalX - measureWidth / 2 + barInterval) / (barWidth + barInterval);
            } else {
                Log.e("yushan", "向右滑动");
                centerPosition = (int) (startOriganalX - measureWidth / 2 + barWidth) / (barWidth + barInterval);
            }

            postDelayed(this, 20);
            invalidate();
        }
    }

    private OnFoodDateChangedListener onFoodDateChangedListener;

    public void setOnFoodDateChangedListener(OnFoodDateChangedListener onFoodDateChangedListener) {
        this.onFoodDateChangedListener = onFoodDateChangedListener;
    }

    public interface OnFoodDateChangedListener {
        public void dateWheelChanged(int position);
    }

}


