package io.nuls.core.model;

import java.text.DateFormat;
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
    public final static long SECOND_TIME = 1000;
    public final static long TIME_ZONE;
    public final static String TIME_ZONE_STRING;
    public static final long TEN_MINUTE_TIME = 10 * MINUTE_TIME;
    private static final ThreadLocal<DateFormat> DATE_FORMATTER_17 = ThreadLocal.withInitial(() -> new SimpleDateFormat(DEFAULT_TIMESTAMP_PATTERN));
    private static final ThreadLocal<DateFormat> DATE_FORMATTER_14 = ThreadLocal.withInitial(() -> new SimpleDateFormat(DEFAULT_PATTERN));

    static {
        Calendar cal = Calendar.getInstance();
        int offset = cal.get(Calendar.ZONE_OFFSET);
        cal.add(Calendar.MILLISECOND, -offset);
        long timeStampUTC = cal.getTimeInMillis();
        long timeStamp = System.currentTimeMillis();
        long timeZone = (timeStamp - timeStampUTC) / HOUR_TIME;
        TIME_ZONE = timeZone + 1;
        TIME_ZONE_STRING = String.valueOf(TIME_ZONE);
    }


    /**
     * Converts minutes to millis
     *
     * @param minutes time in minutes
     * @return corresponding millis value
     */
    public static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }

    /**
     * Converts seconds to millis
     *
     * @param seconds time in seconds
     * @return corresponding millis value
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }

    /**
     * Converts millis to minutes
     *
     * @param millis time in millis
     * @return time in minutes
     */
    public static long millisToMinutes(long millis) {
        return Math.round(millis / 60.0 / 1000.0);
    }

    /**
     * Converts millis to seconds
     *
     * @param millis time in millis
     * @return time in seconds
     */
    public static long millisToSeconds(long millis) {
        return Math.round(millis / 1000.0);
    }

    /**
     * Returns timestamp in the future after some millis passed from now
     *
     * @param millis millis count
     * @return future timestamp
     */
    public static long timeAfterMillis(long millis) {
        return System.currentTimeMillis() + millis;
    }

    public static String toGMTString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    /**
     * Convert date toyyyy-MM-dd HH:mm:ssformat
     *
     * @param date
     * @return String
     */
    public static String convertDate(Date date) {
        if (date == null) {
            return EMPTY_SRING;
        }
        return DATE_FORMATTER_14.get().format(date);
    }

    /**
     * Convert date topatternformat
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
            return DATE_FORMATTER_14.get().parse(date);
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
     * Determine if the incoming date is the first day of the month
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
     * Determine if the incoming date is the first day of the year
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
     * Return after removing hours, minutes, and seconds
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
     * Add timedayReturn in days. If negative numbers are passed, they represent subtractiondayday
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
     * How many months ago today
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
     * Get the first day of the previous month
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
     * Get the first day of this month
     *
     * @return Date
     */
    public static Date getFirstDayOfMonth() {
        return getFirstDayOfMonth(new Date());
    }

    /**
     * Get the first day of this month
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
     * Obtain the first day of the previous year
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
     * Get a list of dates that differ between two time periods
     *
     * @param beginDate start time
     * @param endDate   End time
     * @param type      1Represents obtaining the difference between two times“month-date”List,2Indicating the difference between two times“Year-month”list
     * @return List of time differences between two time periods
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
     * Determine if the object is empty
     *
     * @param obj object
     * @return If empty, returntrueOtherwise, returnfalse
     */
    public static boolean isEmpty(Object obj) {
        return obj == null || EMPTY_SRING.equals(obj);
    }

    /**
     * Get the day of the week
     *
     * @param c
     * @return String
     */
    public static String getWeekDay(Calendar c) {
        if (c == null) {
            return "Monday";
        }
        if (Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "Monday";
        }
        if (Calendar.TUESDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "Tuesday";
        }
        if (Calendar.WEDNESDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "Wednesday";
        }
        if (Calendar.THURSDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "Thursday";
        }
        if (Calendar.FRIDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "Friday";
        }
        if (Calendar.SATURDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "Saturday";
        }
        if (Calendar.SUNDAY == c.get(Calendar.DAY_OF_WEEK)) {
            return "Sunday";
        }
        return "Monday";
    }

    /**
     * Timestamp  Return the interval between two times
     *
     * @param startTime  start time
     * @param endTime    End time
     * @param showSuffix Do you want to add it“ago”/“after”label
     * @return String
     */
    public static String convertLebalFull(Date startTime, Date endTime, boolean showSuffix) {
        if (startTime == null || endTime == null) {
            return EMPTY_SRING;
        }
        // The number of seconds of difference
        long time = (startTime.getTime() - endTime.getTime()) / 1000;
        String label = analyzeTime(time, true);
        if (showSuffix) {
            label += (time > 0) ? "ago" : "after";
        }
        return label;
    }

    /**
     * Timestamp  Return the interval between two times
     *
     * @param startTime  start time
     * @param endTime    End time
     * @param showSuffix Do you want to add it“ago”/“after”label
     * @return String   Returned timestamp
     */
    public static String convertLebal(Date startTime, Date endTime,
                                      boolean showSuffix) {
        if (startTime == null || endTime == null) {
            return EMPTY_SRING;
        }
        // The number of seconds of difference
        long time = (startTime.getTime() - endTime.getTime()) / 1000;
        String label = analyzeTime(time, false);
        if (showSuffix) {
            label += (time > 0) ? "ago" : "after";
        }
        return label;
    }


    /**
     * Convert timestamp to specified display format
     *
     * @param time     time
     * @param showFull Is it all displayed
     * @return Display string of time
     */
    public static String analyzeTime(long time, boolean showFull) {
        String remark = EMPTY_SRING;
        long tempTime = Math.abs(time);
        if (tempTime < 60) {
            remark = String.format("%ssecond", tempTime);
        } else if (tempTime < 3600) {
            remark = String.format("%sbranch%ssecond", tempTime / 60, tempTime % 60);
        } else if (tempTime / 3600 < 24) {
            if (showFull) {
                remark = String.format("%shour%sbranch%ssecond", tempTime / 3600,
                        (tempTime / 60) % 60, tempTime % 60);
            } else {
                remark = String.format("%shour%sbranch", tempTime / 3600,
                        (tempTime / 60) % 60);
            }
        } else if (tempTime / (3600 * 24L) < 30) {
            if (showFull) {
                remark = String.format("%sday%shour%sbranch%ssecond",
                        tempTime / (3600 * 24L), (tempTime / 3600) % 24,
                        (tempTime / 60) % 60, tempTime % 60);
            } else {
                remark = String.format("%sday%shour", tempTime / (3600 * 24L),
                        (tempTime / 3600) % 24);
            }
        } else if (tempTime / (3600 * 24 * 30L) <= 12) {
            if (showFull) {
                remark = String.format("%Months%sday%shour", tempTime
                                / (3600 * 24 * 30L), tempTime / (3600 * 24L),
                        (tempTime / 3600) % 24);
            } else {
                remark = tempTime / (3600 * 24 * 30L) + "Months" + tempTime
                        / (3600 * 24L) % 30 + "day";
            }
        } else if (tempTime / (3600 * 24 * 30L) < 12) {

        }
        return remark;
    }

    /**
     * Get the date of the current time
     *
     * @return date
     */
    public static Date getToday() {
        return rounding(new Date());
    }

    /**
     * Get yesterday's date
     *
     * @return date
     */
    public static Date getYesterday() {
        return rounding(dateAdd(new Date(), -1));
    }

    /**
     * Get the number of days between two dates
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
     * Get tomorrow's date
     *
     * @return date
     */
    public static Date getTomorrow() {
        return rounding(dateAdd(new Date(), 1));
    }

    /**
     * Check if the incoming time is after the current time hours
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
     * Convert a timestamp to its corresponding size‘Hours, minutes, seconds, milliseconds’Different combinations of strings
     *
     * @param offsetTime time
     * @return A string that combines time, minutes, and seconds
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
     * Time plus or minus hours
     *
     * @param startDate The time to process,NullThen it is the current time
     * @param hours     Hours of addition and subtraction
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
     * Time plus or minus minutes
     *
     * @param startDate The time to process,NullThen it is the current time
     * @param minutes   Minute addition and subtraction
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
     * Time plus or minus seconds
     *
     * @param startDate The time to process,NullThen it is the current time
     * @param seconds   Seconds of addition and subtraction
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
     * Time plus or minus days
     *
     * @param startDate The time to process,NullThen it is the current time
     * @param days      Days of addition and subtraction
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
     * Time plus or minus months
     *
     * @param startDate The time to process,NullThen it is the current time
     * @param months    Months of addition and subtraction
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
     * Time plus or minus years
     *
     * @param startDate The time to process,NullThen it is the current time
     * @param years     Years of addition and subtraction
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
     * Time comparison（IfmyDate>compareDatereturn1,<return-1, equal return0）
     *
     * @param myDate      time
     * @param compareDate Time to compare
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
     * Obtain the smallest of two times
     *
     * @param date        Compared time
     * @param compareDate Compare time
     * @return result
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
     * Obtain the largest of two times
     *
     * @param date        Compared time
     * @param compareDate Compare time
     * @return result
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
     * Obtain the year of the date and time, such as2017-02-13, return to2017
     *
     * @param date
     * @return
     */
    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    /**
     * Obtain the month of the date and time, such as2017year2month13Day, return2
     *
     * @param date
     * @return
     */
    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    /**
     * What day is the date and time obtained（That is, the date of returndd）, such as2017-02-13, return to13
     *
     * @param date
     * @return
     */
    public static int getDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DATE);
    }

    /**
     * Convert timestamp to date default format string
     *
     * @param time time stamp
     * @return Date Format String
     */
    public static String timeStamp2DateStr(long time) {
        return DATE_FORMATTER_17.get().format(new Date(time));
    }


    /**
     * Convert timestamp to date format string
     *
     * @param time   time stamp
     * @param format Date string format
     * @return Date string
     */
    public static String timeStamp2DateStr(long time, String format) {
        if (format == null || format.isEmpty()) {
            format = DEFAULT_TIMESTAMP_PATTERN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(time));
    }

    /**
     * Obtain time zone information
     */
    public static long getTimeZone() {
        return TIME_ZONE;
    }

    /**
     * Obtain time zone information
     */
    public static String getTimeZoneString() {
        return TIME_ZONE_STRING;
    }
}
