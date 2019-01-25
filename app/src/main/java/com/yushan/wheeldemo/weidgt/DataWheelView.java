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


public class DataWheelView extends View {

    private int barInterval;
    private int barWidth;

    private int bottom_text_size;
    private int bar_color;
    private int bottom_line_color;
    private int bottom_text_color;

    private ArrayList<BarData> innerData = new ArrayList<>();
    private ArrayList<BarData> refreshList = new ArrayList<>();
    private int paddingTop;
    private int paddingLeft;
    private int paddingBottom;
    private int paddingRight;
    private int defaultHeight = dp2Px(44);
    private int bottom_view_height = dp2Px(30);
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
    private int centerPosition;
    private float nextDis;
    private String moveTo;
    private Context mContext;
    private Paint mTopTextPaint;
    private Paint mBarPaint;
    private Paint mCenterBarPaint;
    private boolean setStart = true;
    private String drawText;

    public DataWheelView(Context context) {
        this(context, null);
    }

    public DataWheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.barchar_style);
        barInterval = (int) typedArray.getDimension(R.styleable.barchar_style_barInterval, dp2Px(10));
        bar_color = typedArray.getColor(R.styleable.barchar_style_bar_color, Color.parseColor("#de7c1f"));
        barWidth = (int) typedArray.getDimension(R.styleable.barchar_style_barWidth, dp2Px(40));
        bottom_text_size = (int) typedArray.getDimension(R.styleable.barchar_style_bottom_text_size, sp2Px(11));
        bottom_text_color = typedArray.getColor(R.styleable.barchar_style_bottom_text_color, Color.WHITE);
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
        mTopTextPaint.setTextSize(bottom_text_size);
        mTopTextPaint.setColor(bottom_text_color);
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

    }

    public void setBarChartData(ArrayList<BarData> innerData) {
        this.innerData.clear();
        this.innerData.addAll(innerData);
        this.refreshList.clear();
        if (innerData.size() > 20) {
            this.refreshList.addAll(innerData.subList(0, 20));
        } else {
            this.refreshList.addAll(innerData);
        }


        invalidate();
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
                    Log.e("TAG", "向左滑动");
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
                    Log.e("TAG", "向右滑动");
                    moveTo = "Right";
                    centerPosition = (int) (startOriganalX - measureWidth / 2 + barWidth) / (barWidth + barInterval);

                    // TODO: 2019-1-11
                    if (centerPosition % 15 == 0 && centerPosition != 0) {
                        refreshList.clear();
                        if (innerData.size() > 20) {
                            refreshList.addAll(innerData.subList(0, centerPosition + 20));
                        } else {
                            refreshList.addAll(innerData);
                        }
                    }

                    nextDis = (startOriganalX - measureWidth / 2) % (barWidth + barInterval);
                }

                tempLength = currX - lastX;

                Log.e("yushan", "startOriganalX:" + startOriganalX + "  barWidth:" + (centerPosition) + "  measureWidth / 2:" + refreshList.size());
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

        if (onDateChangedListener != null) {
            onDateChangedListener.dateWheelChanged(centerPosition);
        }

        drawCenterBar(canvas, endY);
        for (int i = 0; i < refreshList.size(); i++) {
            float barHeight = 0;
            int startY = (int) (defaultHeight - bottom_view_height - barHeight);

            if (i == 0) {
                drawText = "今日";
            } else {
                drawText = refreshList.get(i).getDate();
            }

            //绘制下面的文字
            float bottomTextWidth = mTopTextPaint.measureText(drawText);
            float bottomStartX = startX + barWidth / 2 - bottomTextWidth / 2;
            Rect rect = new Rect();
            mTopTextPaint.getTextBounds(refreshList.get(i).getDate(), 0, refreshList.get(i).getDate().length(), rect);
            float bottomStartY = defaultHeight - bottom_view_height + 10 + rect.height();//rect.height()是获取文本的高度;


            //绘制底部的文字
            drawText(canvas, drawText, bottomStartX, bottomStartY);

            startX = startX - (barWidth + barInterval);
        }
    }

    //绘制中心bar
    private void drawCenterBar(Canvas canvas, int endY) {
        Rect mRect = new Rect((measureWidth - barWidth) / 2, 0, (measureWidth + barWidth) / 2, endY);
        canvas.drawRect(mRect, mCenterBarPaint);
    }

    private void drawText(Canvas canvas, String text, float bottomStartX, float bottomStartY) {
        canvas.drawText(text, bottomStartX, bottomStartY, mTopTextPaint);
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
        private String year;

        private String date;

        public BarData(String year, String bottomText) {
            this.date = bottomText;
            this.year = year;
        }

        public String getYear() {
            return year == null ? "" : year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getDate() {
            return date == null ? "" : date;
        }

        public void setDate(String date) {
            this.date = date;
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
                Log.e("TAG", "Left");
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
                Log.e("TAG", "Right");
                moveTo = "Right";
                centerPosition = (int) (startOriganalX - measureWidth / 2 + barWidth) / (barWidth + barInterval);

                if (centerPosition % 15 == 0 && centerPosition != 0) {
                    refreshList.clear();
                    if (innerData.size() > 20) {
                        refreshList.addAll(innerData.subList(0, centerPosition + 20));
                    } else {
                        refreshList.addAll(innerData);
                    }
                }

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
                Log.e("TAG", "moveTo:Left");
                if (startOriganalX < measureWidth / 2) {
                    startOriganalX = measureWidth / 2;
                    isBoundary = true;
                }

                centerPosition = (int) (startOriganalX - measureWidth / 2 + barInterval) / (barWidth + barInterval);
            } else {
                Log.e("TAG", "moveTo");
                centerPosition = (int) (startOriganalX - measureWidth / 2 + barWidth) / (barWidth + barInterval);
            }

            postDelayed(this, 20);
            invalidate();
        }
    }

    private OnDateChangedListener onDateChangedListener;

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        this.onDateChangedListener = onDateChangedListener;
    }

    public interface OnDateChangedListener {
        public void dateWheelChanged(int data);
    }

}


