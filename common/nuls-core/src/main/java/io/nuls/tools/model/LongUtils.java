package io.nuls.tools.model;

import java.math.BigInteger;

/**
 * @author tag
 * */
public class LongUtils {
    /**
     * 两个long型数据相加
     * @param val1  被加数
     * @param val2  加数
     * @return      和
     * */
    public static long add(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.add(value2).longValue();
    }


    /**
     * 两个long型数据相差
     * @param val1  被减数
     * @param val2  减数
     * @return      差
     * */
    public static long sub(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.subtract(value2).longValue();
    }

    /**
     * 两个long型数据相乘
     * @param val1  被乘数
     * @param val2  乘数
     * @return      积
     * */
    public static long mul(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.multiply(value2).longValue();
    }


    /**
     * 两个long型数据相除，保留默认小数位（8位）
     * @param val1  被除数
     * @param val2  除数
     * @return      商
     * */
    public static double exactDiv(long val1, long val2) {
        return DoubleUtils.div(val1, val2);
    }


    /**
     * 两个long型数据相除，去掉小数位
     * @param val1  被除数
     * @param val2  除数
     * @return      商
     * */
    public static long div(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.divide(value2).longValue();
    }

    /**
     * 两个long型数据求余数
     * @param val1  被除数
     * @param val2  除数
     * @return      余数
     * */
    public static long mod(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.mod(value2).longValue();
    }

    /**
     * 随机生成一个Long型数据
     * @return  long
     * */
    public static long randomLong() {
        return (long) (Math.random() * Long.MAX_VALUE);
    }
}
