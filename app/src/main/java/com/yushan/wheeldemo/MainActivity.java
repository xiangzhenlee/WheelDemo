package com.yushan.wheeldemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.yushan.wheeldemo.utils.DateUtil;
import com.yushan.wheeldemo.weidgt.DataWheelView;
import com.yushan.wheeldemo.weidgt.FoodCalView;
import com.yushan.wheeldemo.weidgt.WeightWheelView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FoodCalView fcv_food;
    private int month;
    private int day;
    private int oldCalDate = -1;
    private ArrayList<FoodCalView.BarData> innerFoodCalData = new ArrayList<>();
    private ArrayList<DataWheelView.BarData> innerDateData = new ArrayList<>();
    private TextView tv_food_date;
    private DataWheelView dwv_data;
    private double oldDate;
    private TextView tv_date;
    private WeightWheelView wwv_mater;
    private String chooseWeight;
    private TextView tv_weight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initFoodCalData();
    }

    private void initView() {
        fcv_food = (FoodCalView) findViewById(R.id.fcv_food);
        tv_food_date = (TextView) findViewById(R.id.tv_food_date);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_weight = (TextView) findViewById(R.id.tv_weight);

        fcv_food.setOnFoodDateChangedListener(new FoodCalView.OnFoodDateChangedListener() {
            @Override
            public void dateWheelChanged(int position) {
                Log.e("yushan", "position:" + position);
                if (oldCalDate != position) {

                    String date = innerFoodCalData.get(position).getYear() + "/" + innerFoodCalData.get(position).getDate();
                    tv_food_date.setText(date);
                    oldCalDate = position;
                }
            }
        });

        dwv_data = (DataWheelView) findViewById(R.id.dwv_data);
        dwv_data.setOnDateChangedListener(new DataWheelView.OnDateChangedListener() {
            @Override
            public void dateWheelChanged(int data) {
                if (oldDate != data) {

                    String date = innerDateData.get(data).getYear() + "/" + innerDateData.get(data).getDate();
                    tv_date.setText(date);
                    oldDate = data;
                }
            }
        });

        wwv_mater = (WeightWheelView) findViewById(R.id.wwv_mater);

        wwv_mater.setOnWeightWheelChangedListener(new WeightWheelView.OnWeightWheelChangedListener() {
            @Override
            public void weightWheelChanged(int weight) {
                chooseWeight = weight / 10 + "." + weight % 10;

                tv_weight.setText(chooseWeight);
            }
        });
    }

    private void initFoodCalData() {
        String[] dateStr;
        String date;
        for (int i = 0; i < getDate().size(); i++) {

            dateStr = ((String) getDate().get(i)).split("/");
            date = dateStr[1] + "/" + dateStr[2];

            String dietCal = getNum(0, 4000) + "";

            innerFoodCalData.add(new FoodCalView.BarData(Integer.parseInt(dietCal), date, dateStr[0]));
        }

        fcv_food.setMaxValue(1850);
        fcv_food.setBarChartData(innerFoodCalData);
    }

    /**
     * 生成一个startNum 到 endNum之间的随机数(不包含endNum的随机数)
     *
     * @param startNum
     * @param endNum
     * @return
     */
    public static int getNum(int startNum, int endNum) {
        if (endNum > startNum) {
            Random random = new Random();
            return random.nextInt(endNum - startNum) + startNum;
        }
        return 0;
    }

    public ArrayList<String> getDate() {
        ArrayList<String> date = new ArrayList<>();

        for (int i = 2019; i <= DateUtil.getYear(); i++) {
            setDate(i);
            for (int j = 1; j <= month; j++) {
                if (j < 10) {

                    for (int k = 1; k <= day; k++) {
                        if (k < 10) {
                            date.add(i + "/0" + j + "/0" + k);
                            innerDateData.add(new DataWheelView.BarData(i + "", "0" + j + "/" + "0" + k));
                        } else {
                            date.add(i + "/0" + j + "/" + k);
                            innerDateData.add(new DataWheelView.BarData(i + "", "0" + j + "/" + k));
                        }
                    }
                } else {

                    for (int k = 1; k <= day; k++) {
                        if (k < 10) {
                            date.add(i + "/" + j + "/0" + k);
                            innerDateData.add(new DataWheelView.BarData(i + "", j + "/" + "0" + k));
                        } else {
                            date.add(i + "/" + j + "/" + k);
                            innerDateData.add(new DataWheelView.BarData(i + "", j + "/" + k));
                        }
                    }
                }
            }
        }

        Collections.reverse(innerDateData);
        dwv_data.setBarChartData(innerDateData);

        return date;
    }


    /**
     * 设置年月日
     *
     * @param year
     */
    public void setDate(int year) {

        if (year == DateUtil.getYear()) {
            month = DateUtil.getMonth();
        } else {
            month = 12;
        }
        calDays(year, month);
    }


    /**
     * 计算每月多少天
     *
     * @param month
     * @param year
     */
    public void calDays(int year, int month) {
        boolean leayyear = false;
        if (year % 4 == 0 && year % 100 != 0) {
            leayyear = true;
        } else {
            leayyear = false;
        }
        for (int i = 1; i <= 12; i++) {
            switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    this.day = 31;
                    break;
                case 2:
                    if (leayyear) {
                        this.day = 29;
                    } else {
                        this.day = 28;
                    }
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    this.day = 30;
                    break;
            }
        }
        if (year == DateUtil.getYear() && month == DateUtil.getMonth()) {
            this.day = DateUtil.getDay();
        }
    }
}
