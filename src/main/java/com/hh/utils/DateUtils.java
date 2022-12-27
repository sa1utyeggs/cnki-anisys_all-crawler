package com.hh.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ab875
 */
public class DateUtils {

    public static final String YEAR_MONTH_DAY = "yyyy-MM-dd";

    public static final String YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = "yyyy-MM-dd HH:mm:ss";

    public static Integer differentDays(Date start, Date end) {
        return Math.abs((int) ((end.getTime() - start.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 获得过了 offset 秒 的时间
     * @param date base time
     * @param offset 秒
     * @return Date
     */
    public static Date getDateAfter(Date date, long offset){
        return new Date(date.getTime() + offset * 1000);
    }


    /**
     * 将时间段，根据粒度分割，向上取整
     *
     * @param start       开始时间
     * @param end         结束时间
     * @param granularity 时间粒度
     * @return 有几个分区
     */
    public static Integer splitDate(Date start, Date end, Integer granularity) {
        double dividend = end.getTime() - start.getTime();
        double divisor = 1000 * 3600 * 24;
        // 向上取整，并保证为正值
        // 由于 1000 * 3600 * 24 会溢出 所以 改为连除
        return Math.abs((int) Math.ceil(dividend / divisor / granularity));
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    /**
     * 获得明天0点的 Date 对象
     *
     * @return Date
     */
    public static Date getDaysAfterToday(Integer days) {
        Calendar calendar = getZeroClockOfToday();
        // 向后移 days 天 roll 只移动日，不会加到月上
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static Calendar getZeroClockOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // 设置为 0 点
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

}
