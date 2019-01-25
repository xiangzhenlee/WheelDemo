package com.yushan.wheeldemo.utils;

import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private static final String TAG = "DateUtil";
    public static final String FORMAT_PATTERN_1 = "yyyy";
    public static final String FORMAT_PATTERN_2 = "yyyyMMdd";
    public static final String FORMAT_PATTERN_3 = "yyyyMMddHHmmss";
    public static final String FORMAT_PATTERN_4 = "yyyyMMddHH:mm:ss";
    public static final String FORMAT_PATTERN_5 = "yyyyMMdd HH:mm:ss";
    public static final String FORMAT_PATTERN_6 = "M月d日 HH:mm:ss";
    public static final String FORMAT_PATTERN_7 = "yyyy年M月d日 HH:mm:ss";
    public static final String FORMAT_PATTERN_8 = "M月d日";
    public static final String FORMAT_PATTERN_9 = "yyyy年M月d日";
    public static final String FORMAT_PATTERN_10 = "yyyy:MM:dd:HH:mm:ss";
    public static final String FORMAT_PATTERN_11 = "yyyyMMddHH";
    public static final String FORMAT_PATTERN_12 = "yyyy-MM-dd";
    public static final String FORMAT_PATTERN_13 = "yyyy-M-d HH:mm";
    public static final String FORMAT_PATTERN_14 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String FORMAT_PATTERN_15 = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_PATTERN_16 = "yyyy年M月";
    public static final String FORMAT_PATTERN_17 = "yy-MM-dd";
    public static final String FORMAT_PATTERN_18 = "M-d";
    public static final String FORMAT_PATTERN_19 = "M月d日 HH:mm";
    public static final String FORMAT_PATTERN_20 = "yy年M月d日 HH:mm";
    public static final String FORMAT_PATTERN_21 = "yy年M月d日";
    public static final String FORMAT_PATTERN_22 = "yyyy.MM.dd";


    /**
     * 将时分秒转为秒数
     */
    public static long formatTurnSecond(String time) {
        String s = time;
        int index1 = s.indexOf(":");
        int index2 = s.indexOf(":", index1 + 1);
        int hh = Integer.parseInt(s.substring(0, index1));
        int mi = Integer.parseInt(s.substring(index1 + 1, index2));
        int ss = Integer.parseInt(s.substring(index2 + 1));

        return hh * 60 * 60 + mi * 60 + ss;
    }

    /**
     * 将普通时间转换为 Unix 时间戳
     *
     * @param dateString 时间格式：yyyy-MM-dd hh:mm:ss
     */
    public static long commonChangeUnixDate(String format, String dateString) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time = date.getTime() / 1000;
        System.out.println(time);
        return time;
    }

    /**
     * 根据传入的时间格式 和 时间戳格式化出时间
     *
     * @param pattern
     * @param ms
     * @return
     */
    public static String getDateStr(String pattern, long ms) {
        if (TextUtils.isEmpty(pattern)) return null;
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(ms);
    }

    /**
     * 将Date对象转换为时间字符串
     *
     * @param pattern
     * @param date
     * @return
     */
    public static String getDateStr(String pattern, Date date) {
        if (TextUtils.isEmpty(pattern) || date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date.getTime());
    }

    /**
     * 将时间字符串转换为时间戳
     *
     * @param pattern
     * @param dateStr
     * @return
     */
    public static long parseDateStr2Millis(String pattern, String dateStr) {
        if (TextUtils.isEmpty(pattern) || TextUtils.isEmpty(dateStr)) return 0;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            Log.e(TAG, "parseDateStr2Millis() ", e);
        }
        return date.getTime();
    }

    /**
     * 将时间字符串转换为Date对象
     *
     * @param pattern
     * @param dateStr
     * @return
     */
    public static Date parseDateStr2Date(String pattern, String dateStr) {
        if (TextUtils.isEmpty(pattern) || TextUtils.isEmpty(dateStr)) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 将日历类型时间转为Date类型时间
     *
     * @param dateStr
     * @return
     */
    public static String parseCalendar2Date(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) return null;
        String year = dateStr.substring(0, 4);
        String moon = dateStr.substring(5, 7);
        String day = dateStr.substring(8, dateStr.length());
        return year + moon + day;
    }

    /**
     * 时间比较
     *
     * @param date1
     * @param date2
     * @param pattern
     * @return
     */
    public static int compare(String pattern, Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String dateStr1 = sdf.format(date1);
        String dateStr2 = sdf.format(date2);
        return dateStr1.compareTo(dateStr2);
    }

    /**
     * @param date1 比较日期
     * @param date2 上传日期
     * @return 0:相等，1大于 -1 小于
     */
    public static int compareTo(String date1, String date2) {
        String[] str1 = date1.split("-");
        String[] str2 = date2.split("-");
        if (str1.length != 3 || str2.length != 3) return 1;
        if (Integer.parseInt(str1[0]) > Integer.parseInt(str2[0])) {// 比较年
            return 1;
        } else if (Integer.parseInt(str1[0]) < Integer.parseInt(str2[0])) {
            return -1;
        } else {// 比较月
            if (Integer.parseInt(str1[1]) > Integer.parseInt(str2[1])) {
                return 1;
            } else if (Integer.parseInt(str1[1]) < Integer.parseInt(str2[1])) {
                return -1;
            } else {// 比较日
                if (Integer.parseInt(str1[2]) > Integer.parseInt(str2[2])) {
                    return 1;
                } else if (Integer.parseInt(str1[2]) < Integer
                        .parseInt(str2[2])) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    /**
     * 刚刚
     * 5分钟前
     * 昨天
     * 03-26
     * 16-02-08
     * 15-01-07
     *
     * @param data
     * @return
     */
    public static String TalkCompareToTime(String data) {
        if (data == null || "".equals(data)) return "";
        long datal = Long.parseLong(data);
        Date newd = new Date();
        long newdate = newd.getTime() / 1000;
        long oneminute = 60L;// 分钟
        long onehours = 60L * 60;// 小时
        long oneday = 60L * 60 * 24;// 天
        long onemonth = 60L * 60 * 24 * 30;// 月
        long oneyear = 60L * 60 * 24 * 30 * 12;// 年
        long result = newdate - datal;
        String str = "";
        if (result < oneminute) {// 小于1分钟
            str = "刚刚";
        } else if (oneminute <= result && result < onehours) {// 分钟
            str = ((int) result / oneminute) + "分钟前";
        } else if (onehours <= result && result < oneday) {// 小时
            str = ((int) result / onehours) + "小时前";
        } else if (oneday <= result && result < 2 * oneday) {// 昨天
            str = "昨天";
        } else if (2 * oneday <= result && result < oneyear) {// 今年
            str = getDateStr("MM-dd", datal * 1000);
        } else if (oneyear <= result) {// 往年
            str = getDateStr("yy-MM-dd", datal * 1000);
        }
        return str;
    }


    public static String TalkCompareTo(String data) {
        if (data == null || "".equals(data)) return "";
        long datal = Long.parseLong(data);
        Date newd = new Date();
        long newdate = newd.getTime() / 1000;
        long oneminute = 60L;// 分钟
        long onehours = 60L * 60;// 小时
        long oneday = 60L * 60 * 24;// 天
        long onemonth = 60L * 60 * 24 * 30;// 月
        long oneyear = 60L * 60 * 24 * 30 * 12;// 年
        long result = newdate - datal;
        String str = "";
        if (result < oneminute) {// 小于1分钟
            str = "刚刚";
        } else if (oneminute <= result && result < onehours) {// 分钟
            str = ((int) result / oneminute) + "分钟前";
        } else if (onehours <= result && result < oneday) {// 小时
            str = ((int) result / onehours) + "小时前";
        } else if (oneday <= result && result < onemonth) {// 天
            str = ((int) result / oneday) + "天前";
        } else if (onemonth <= result && result < oneyear) {// 月
            str = ((int) result / onemonth) + "月前";
        } else if (oneyear <= result) {// 年
            str = ((int) result / oneyear) + "年前";
        }
        return str;
    }

    /**
     * 检查日期是否正确
     *
     * @param format
     * @param date
     * @return
     */
    public static boolean isDateFormatRight(String format, String date) {
        boolean isRight;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            isRight = true;
        } catch (Exception e) {
            isRight = false;
        }
        return isRight;
    }

    /**
     * 多少天之后的日期
     *
     * @param format
     * @param currDate
     * @param days     多少天之后
     * @return
     */
    public static String getPastorFutureDate(String format, String currDate, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar cd = Calendar.getInstance();

        try {
            cd.setTime(sdf.parse(currDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cd.add(Calendar.DATE, days);
        Date date = cd.getTime();
        return sdf.format(date);
    }

    /**
     * 获取多少个小时之后的日期
     *
     * @param format   日期格式
     * @param currDate 当前日期
     * @param hours    多少个小时之后
     * @return
     */
    public static String getPastorFutureHourDate(String format, String currDate, int hours) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();

        try {
            c.setTime(sdf.parse(currDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.add(Calendar.HOUR, hours);
        Date date = c.getTime();
        return sdf.format(date);
    }

    public static String getHyphenDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date());
    }

    /**
     * 获取格式化之后的时间
     *
     * @param time 转换之前的日期格式   2015-07-17 00:00:00
     * @return 转换之后的日期格式     2015年7月17日  00:00
     */
    public static String getFormatAfterDate(String time) {
        if ("".equals(time)) {
            return "";
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年M月d日 HH:mm");
        Date date = null;
        try {
            date = sdf1.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }//提取格式中的日期
        String newStr = sdf2.format(date); //改变格式
        return newStr;
    }

    /**
     * 计算得到当前传入日期之前days天的日期
     *
     * @param time
     * @param days
     * @return
     */
    public static String getStartDate(String time, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cd = Calendar.getInstance();

        try {
            cd.setTime(sdf.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cd.add(Calendar.DATE, days);
        Date date = cd.getTime();
        return sdf.format(date);
    }

    /**
     * 与上个方法类型不同
     *
     * @param time
     * @param days
     * @return
     */
    public static String getStarDate(String time, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cd = Calendar.getInstance();

        try {
            cd.setTime(sdf.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cd.add(Calendar.DATE, days);
        Date date = cd.getTime();
        return sdf.format(date);
    }

    public static int getDiffValue(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cd = Calendar.getInstance();
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = sdf.parse(date1);
            d2 = sdf.parse(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long intervalMilli = d1.getTime() - d2.getTime();

        return (int) (intervalMilli / (24 * 60 * 60 * 1000));
    }

    /**
     * 小时改为2位
     *
     * @return
     */
    public static String getStepCurrHour() {
        Date date = new Date();
        int currHour = date.getHours();
        String resp = String.valueOf(currHour);
        switch (currHour) {
            case 0:
                resp = "24";
                break;
            case 1:
                resp = "25";
                break;
            case 2:
                resp = "02";
                break;
            case 3:
                resp = "03";
                break;
            case 4:
                resp = "04";
                break;
            case 5:
                resp = "05";
                break;
            case 6:
                resp = "06";
                break;
            case 7:
                resp = "07";
                break;
            case 8:
                resp = "08";
                break;
            case 9:
                resp = "09";
                break;
            default:
                resp = String.valueOf(currHour);
                break;
        }
        return resp;
    }


    public static int getCurrHour() {
        Date date = new Date();
        int currHour = date.getHours();
        switch (currHour) {
            case 0:
                currHour = 24;
                break;
            case 1:
                currHour = 25;
            default:
                break;
        }
        return currHour;
    }

    /**
     * 格式化时间（输出类似于 刚刚, 4分钟前, 一小时前, 昨天这样的时间）
     *
     * @param time    需要格式化的时间 如"2014-07-14 19:01:45"
     * @param pattern 输入参数time的时间格式 如:"yyyy-MM-dd HH:mm:ss"
     *                <p/>如果为空则默认使用"yyyy-MM-dd HH:mm:ss"格式
     * @return time为null，或者时间格式不匹配，输出空字符""
     */
    public static String formatDisplayTime(String time, String pattern) {
        String display = "";
        int tMin = 60 * 1000;
        int tHour = 60 * tMin;
        int tDay = 24 * tHour;

        if (time != null) {
            try {
                Date tDate = new SimpleDateFormat(pattern).parse(time);
                Date today = new Date();
                SimpleDateFormat thisYearDf = new SimpleDateFormat("yyyy");
                SimpleDateFormat todayDf = new SimpleDateFormat("yyyy-M-d");
                Date thisYear = new Date(thisYearDf.parse(thisYearDf.format(today)).getTime());
                Date yesterday = new Date(todayDf.parse(todayDf.format(today)).getTime());
                Date beforeYes = new Date(yesterday.getTime() - tDay);
                if (tDate != null) {
                    SimpleDateFormat halfDf = new SimpleDateFormat("M月d日 HH:mm");
                    long dTime = today.getTime() - tDate.getTime();
                    if (tDate.before(thisYear)) {
                        display = new SimpleDateFormat("M月d日 HH:mm").format(tDate);
                    } else {

                        if (dTime < tMin) {  //刚刚
                            display = new SimpleDateFormat("HH:mm").format(tDate);
                        } else if (dTime < tHour) {  //分钟前
                            display = new SimpleDateFormat("HH:mm").format(tDate);
                        } else if (dTime < tDay && tDate.after(yesterday)) { //小时前
                            display = new SimpleDateFormat("HH:mm").format(tDate);
                        } else if (tDate.after(beforeYes) && tDate.before(yesterday)) { //昨天
                            display = "昨天" + new SimpleDateFormat("HH:mm").format(tDate);
                        } else {
                            display = halfDf.format(tDate);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return display;
    }

    /**
     * 获取当前日期的年
     *
     * @return
     */
    public static int getYear() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
        String currentDate = sdf.format(date); // 当期日期
        return Integer.parseInt(currentDate.split("-")[0]);
    }

    /**
     * 获取当前日期的月
     *
     * @return
     */
    public static int getMonth() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
        String currentDate = sdf.format(date); // 当期日期
        return Integer.parseInt(currentDate.split("-")[1]);
    }

    /**
     * 获取当前日期的日
     *
     * @return
     */
    public static int getDay() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
        String currentDate = sdf.format(date); // 当期日期
        return Integer.parseInt(currentDate.split("-")[2]);
    }

    /**
     * 获取系统格式的时间
     *
     * @return
     */
    public static String getSystemTimeFormat() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String currentTime = df.format(new Date()) + " 00:00:00";
        return currentTime;// new Date()为获取当前系统时间
    }

    /**
     * 获取系统的时间
     *
     * @return 2015-5-5
     */
    public static String getSystemTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
        String currentDate = sdf.format(date); // 当期日期
        int year_c = Integer.parseInt(currentDate.split("-")[0]);
        int month_c = Integer.parseInt(currentDate.split("-")[1]);
        int day_c = Integer.parseInt(currentDate.split("-")[2]);
        return year_c + "-" + month_c + "-" + day_c;
    }

}
