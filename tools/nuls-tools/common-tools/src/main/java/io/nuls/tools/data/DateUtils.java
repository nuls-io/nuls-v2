package io.nuls.tools.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {
    public final static String EMPTY_SRING = "";
    public final static String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public final static String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.sss";
    public final static long DATE_TIME = 1000 * 24 * 60 * 60;
    public final static long HOUR_TIME = 1000 * 60 * 60;
    public final static long MINUTE_TIME = 1000 * 60;

    public static String toGMTString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    /**
     * 把日期转换成yyyy-MM-dd HH:mm:ss格式
     *
     * @param date
     * @return String
     */
    public static String convertDate(Date date) {
        if (date == null) {
            return EMPTY_SRING;
        }
        return new SimpleDateFormat(DEFAULT_PATTERN).format(date);
    }

    /**
     * 把日期转换成pattern格式
     *
     * @param date
     * @param pattern
     * @return String
     */
    public static String convertDate(Date date, String pattern) {
        if (date == null) {
            return EMPTY_SRING;
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * @param date
     * @return Date
     */
    public static Date convertStringToDate(String date) {
        try {
            return new SimpleDateFormat(DEFAULT_PATTERN).parse(date);
        } catch (ParseException e) {
        }
        return new Date();
    }

    /**
     * @param date
     * @param pattern
     * @return Date
     */
    public static Date convertStringToDate(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 判断传入的日期是不是当月的第一天
     *
     * @param date
     * @return boolean
     */
    public static boolean isFirstDayInMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    /**
     * 判断传入的日期是不是当年的第一天
     *
     * @param date
     * @return boolean
     */
    public static boolean isFirstDayInYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_YEAR) == 1;
    }

    /**
     * 去掉时分秒后返回
     *
     * @param date
     * @return Date
     */
    public static Date rounding(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 把时间加上day天后返回，如果传负数代表减day天
     *
     * @param date
     * @param day
     * @return Date
     */
    public static Date dateAdd(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + day);
        return calendar.getTime();
    }

    /**
     * 多少个月前后的今天
     *
     * @param date
     * @param month
     * @return Date
     */
    public static Date dateAddMonth(Date date, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + month);
        return calendar.getTime();
    }

    /**
     * 获取上一个月的第一天
     *
     * @return Date
     */
    public static Date getFirstDayOfPreviousMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取本月的第一天
     *
     * @return Date
     */
    public static Date getFirstDayOfMonth() {
        return getFirstDayOfMonth(new Date());
    }

    /**
     * 获取本月的第一天
     *
     * @param date
     * @return Date
     */
    public static Date getFirstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取上一年的第一天
     *
     * @return Date
     */
    public static Date getFirstDayOfPreviousYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }


    /**
     * 获取两个时间段之间相差日期列表
     *
     * @param beginDate 开始时间
     * @param endDate   结束时间
     * @param type      1表示获取两个时间之间相差的“月份-日期”列表，2表示两个时间之间相差的“年份-月份”列表
     * @return 两个时间段之间相差的时间列表
     */
    public static List<String> getDateRange(String beginDate, String endDate,
                                            int type) {
        List<String> list = new ArrayList<String>();
        if (isEmpty(beginDate) || isEmpty(endDate)) {
            return list;
        }
        if (type == 1) {
            Date begin = convertStringToDate(beginDate, "yyyy-MM-dd");
            Date end = convertStringToDate(endDate, "yyyy-MM-dd");
            if (begin == null || end == null) {
                return list;
            }
            while (begin.equals(end) || begin.before(end)) {
                list.add(convertDate(begin, "MM-dd"));
                begin = dateAdd(begin, 1);
            }
        } else if (type == 2) {
            Date begin = convertStringToDate(beginDate, "yyyy-MM-dd");
            Date end = convertStringToDate(endDate, "yyyy-MM-dd");
            if (begin == null || end == null) {
                return list;
            }
            Calendar beginCalendar = Calendar.getInstance();
            beginCalendar.setTime(begin);
            beginCalendar.set(Calendar.DAY_OF_MONTH, 1);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(end);
            endCalendar.set(Calendar.DAY_OF_MONTH, 1);

            while (beginCalendar.getTime().equals(endCalendar.getTime())
                    || beginCalendar.getTime().before(endCalendar.getTime())) {
                list.add(convertDate(beginCalendar.getTime(), "yyyy-MM"));
                beginCalendar.set(Calendar.MONTH,
                        beginCalendar.get(Calendar.MONTH) + 1);
            }
        }
        return list;
    }

    /**
     * 判断对象是否为空
     *
     * @param obj 对象
     * @return 如果为空返回true，否则返回false
     */
    public static boolean isEmpty(Object obj) {
        return obj == null || EMPTY_SRING.equals(obj);
    }

    /**
     * 获取星期几
     *
     * @param c
     * @return String
     */
    public static String getWeekDay(Calendar c) {
        if (c == null) {
            return "星期一";
        }
        if (Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期一";
        }
        if (Calendar.TUESDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期二";
        }
        if (Calendar.WEDNESDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期三";
        }
        if (Calendar.THURSDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期四";
        }
        if (Calendar.FRIDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期五";
        }
        if (Calendar.SATURDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期六";
        }
        if (Calendar.SUNDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "星期日";
        }
        return "星期一";
    }

    /**
     * 时间标签  返回两个时间的间隔
     *
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param showSuffix 是否添加“前”/“后”标签
     * @return String
     */
    public static String convertLebalFull(Date startTime, Date endTime, boolean showSuffix) {
        if (startTime == null || endTime == null) {
            return EMPTY_SRING;
        }
        // 相差的秒数
        long time = (startTime.getTime() - endTime.getTime()) / 1000;
        String label = analyzeTime(time, true);
        if (showSuffix) {
            label += (time > 0) ? "前" : "后";
        }
        return label;
    }

    /**
     * 时间标签  返回两个时间的间隔
     *
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param showSuffix 是否添加“前”/“后”标签
     * @return String   返回的时间标签
     */
    public static String convertLebal(Date startTime, Date endTime,
                                      boolean showSuffix) {
        if (startTime == null || endTime == null) {
            return EMPTY_SRING;
        }
        // 相差的秒数
        long time = (startTime.getTime() - endTime.getTime()) / 1000;
        String label = analyzeTime(time, false);
        if (showSuffix) {
            label += (time > 0) ? "前" : "后";
        }
        return label;
    }


    /**
     * 把时间戳转为指定显示格式
     *
     * @param time     时间
     * @param showFull 是否全显示
     * @return 时间的显示字符串
     */
    public static String analyzeTime(long time, boolean showFull) {
        String remark = EMPTY_SRING;
        long tempTime = Math.abs(time);
        if (tempTime < 60) {
            remark = String.format("%s秒", tempTime);
        } else if (tempTime < 3600) {
            remark = String.format("%s分%s秒", tempTime / 60, tempTime % 60);
        } else if (tempTime / 3600 < 24) {
            if (showFull) {
                remark = String.format("%s小时%s分%s秒", tempTime / 3600,
                        (tempTime / 60) % 60, tempTime % 60);
            } else {
                remark = String.format("%s小时%s分", tempTime / 3600,
                        (tempTime / 60) % 60);
            }
        } else if (tempTime / (3600 * 24L) < 30) {
            if (showFull) {
                remark = String.format("%s天%s小时%s分%s秒",
                        tempTime / (3600 * 24L), (tempTime / 3600) % 24,
                        (tempTime / 60) % 60, tempTime % 60);
            } else {
                remark = String.format("%s天%s小时", tempTime / (3600 * 24L),
                        (tempTime / 3600) % 24);
            }
        } else if (tempTime / (3600 * 24 * 30L) <= 12) {
            if (showFull) {
                remark = String.format("%个月%s天%s小时", tempTime
                                / (3600 * 24 * 30L), tempTime / (3600 * 24L),
                        (tempTime / 3600) % 24);
            } else {
                remark = tempTime / (3600 * 24 * 30L) + "个月" + tempTime
                        / (3600 * 24L) % 30 + "天";
            }
        } else if (tempTime / (3600 * 24 * 30L) < 12) {

        }
        return remark;
    }

    /**
     * 获取当前时间的日期
     *
     * @return 日期
     */
    public static Date getToday() {
        return rounding(new Date());
    }

    /**
     * 获取昨天的日期
     *
     * @return 日期
     */
    public static Date getYesterday() {
        return rounding(dateAdd(new Date(), -1));
    }

    /**
     * 获取两个日期之间的间隔天数
     *
     * @param startTime
     * @param endTime
     * @return int
     */
    public static int getBetweenDateDays(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long to = startTime.getTime();
        long from = endTime.getTime();

        return (int) ((from - to) / (1000L * 60 * 60 * 24));
    }

    /**
     * 获取明天的日期
     *
     * @return 日期
     */
    public static Date getTomorrow() {
        return rounding(dateAdd(new Date(), 1));
    }

    /**
     * 检查传入的时间是否在当前时间小时数之后
     *
     * @param date
     * @param time
     * @return boolean
     */
    public static boolean checkAfterTime(Date date, String time) {
        Date dateTime = convertStringToDate(convertDate(date, "yyyy-MM-dd").concat(" ").concat(time));
        return dateTime.before(date);
    }


    /**
     * 将一个时间戳，根据其大小转换为对应的‘时分秒毫秒’不同组合的字符串
     *
     * @param offsetTime 时间
     * @return 时分秒组合的字符串
     */
    public static String getOffsetStringDate(long offsetTime) {
        int p = offsetTime > 0 ? 1 : -1;

        offsetTime = Math.abs(offsetTime);
        if (offsetTime < 1000) {
            return p * offsetTime + "ms";
        } else if (offsetTime < MINUTE_TIME) {
            long sec = offsetTime % DATE_TIME % HOUR_TIME % MINUTE_TIME / 1000;
            return p * sec + "s";
        } else if (offsetTime < HOUR_TIME) {
            long minute = offsetTime % DATE_TIME % HOUR_TIME / MINUTE_TIME;
            long sec = offsetTime % DATE_TIME % HOUR_TIME % MINUTE_TIME / 1000;
            if (minute >= 10) {
                return p * minute + "m";
            } else {
                return p * minute + "m" + sec + "s";
            }
        } else {
            long hour = offsetTime % DATE_TIME / HOUR_TIME;
            long minute = offsetTime % DATE_TIME % HOUR_TIME / MINUTE_TIME;
            if (hour >= 5) {
                return p * hour + "h";
            } else {
                return p * hour + "h" + minute + "m";
            }
        }
    }


    /**
     * 时间加减小时
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param hours     加减的小时
     * @return Date
     */
    public static Date dateAddHours(Date startDate, int hours) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        return c.getTime();
    }

    /**
     * 时间加减分钟
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param minutes   加减的分钟
     * @return
     */
    public static Date dateAddMinutes(Date startDate, int minutes) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + minutes);
        return c.getTime();
    }

    /**
     * 时间加减秒数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param seconds   加减的秒数
     * @return
     */
    public static Date dateAddSeconds(Date startDate, int seconds) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.SECOND, c.get(Calendar.SECOND) + seconds);
        return c.getTime();
    }

    /**
     * 时间加减天数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param days      加减的天数
     * @return Date
     */
    public static Date dateAddDays(Date startDate, int days) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.DATE, c.get(Calendar.DATE) + days);
        return c.getTime();
    }

    /**
     * 时间加减月数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param months    加减的月数
     * @return Date
     */
    public static Date dateAddMonths(Date startDate, int months) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + months);
        return c.getTime();
    }

    /**
     * 时间加减年数
     * @param startDate 要处理的时间，Null则为当前时间
     * @param years     加减的年数
     * @return Date
     */
    public static Date dateAddYears(Date startDate, int years) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) + years);
        return c.getTime();
    }

    /**
     * 时间比较（如果myDate>compareDate返回1，<返回-1，相等返回0）
     *
     * @param myDate      时间
     * @param compareDate 要比较的时间
     * @return int
     */
    public static int dateCompare(Date myDate, Date compareDate) {
        Calendar myCal = Calendar.getInstance();
        Calendar compareCal = Calendar.getInstance();
        myCal.setTime(myDate);
        compareCal.setTime(compareDate);
        return myCal.compareTo(compareCal);
    }

    /**
     * 获取两个时间中最小的一个时间
     *
     * @param date         被比较时间
     * @param compareDate  比较时间
     * @return             结果
     */
    public static Date dateMin(Date date, Date compareDate) {
        if (date == null) {
            return compareDate;
        }
        if (compareDate == null) {
            return date;
        }
        if (1 == dateCompare(date, compareDate)) {
            return compareDate;
        } else if (-1 == dateCompare(date, compareDate)) {
            return date;
        }
        return date;
    }

    /**
     * 获取两个时间中最大的一个时间
     *
     * @param date              被比较时间
     * @param compareDate       比较时间
     * @return                  结果
     */
    public static Date dateMax(Date date, Date compareDate) {
        if (date == null) {
            return compareDate;
        }
        if (compareDate == null) {
            return date;
        }
        if (1 == dateCompare(date, compareDate)) {
            return date;
        } else if (-1 == dateCompare(date, compareDate)) {
            return compareDate;
        }
        return date;
    }

     /**
      * 获取日期时间的年份，如2017-02-13，返回2017
      * @param date
      * @return
      */
            public static int getYear(Date date) {
              Calendar cal = Calendar.getInstance();
              cal.setTime(date);
             return cal.get(Calendar.YEAR);
     }

            /**
     * 获取日期时间的月份，如2017年2月13日，返回2
     * @param date
     * @return
      */
            public static int getMonth(Date date) {
               Calendar cal = Calendar.getInstance();
               cal.setTime(date);
            return cal.get(Calendar.MONTH) + 1;
   }

      /**
      * 获取日期时间的第几天（即返回日期的dd），如2017-02-13，返回13
      * @param date
      * @return
      */
       public static int getDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
         return cal.get(Calendar.DATE);
     }

    /**
     * 时间戳转换成日期默认格式字符串
     * @param time 时间戳
     * @return     日期格式字符串
     */
    public static String timeStamp2DateStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIMESTAMP_PATTERN);
        return sdf.format(new Date(time));
    }


    /**
     * 时间戳转换成日期格式字符串
     * @param time    时间戳
     * @param format  日期字符串格式
     * @return        日期字符串
     */
     public static String timeStamp2DateStr(long time,String format) {
         if(format == null || format.isEmpty()){
             format = DEFAULT_TIMESTAMP_PATTERN;
         }
         SimpleDateFormat sdf = new SimpleDateFormat(format);
         return sdf.format(new Date(time));
    }

    /**
     * 获取时区信息
     * */
    public static long getTimeZone(){
        Calendar cal = Calendar.getInstance();
        int offset = cal.get(Calendar.ZONE_OFFSET);
        cal.add(Calendar.MILLISECOND, -offset);
        Long timeStampUTC = cal.getTimeInMillis();
        Long timeStamp = System.currentTimeMillis();
        Long timeZone = (timeStamp - timeStampUTC) / (1000 * 3600);
        return timeZone+1;

    }
}
