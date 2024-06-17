package io.nuls.core.model;

import java.math.BigInteger;

/**
 * @author tag
 * */
public class LongUtils {
    /**
     * TwolongType data addition
     * @param val1  Addend
     * @param val2  Addend
     * @return      and
     * */
    public static long add(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.add(value2).longValue();
    }


    /**
     * TwolongType data difference
     * @param val1  Subtracted number
     * @param val2  Subtraction
     * @return      difference
     * */
    public static long sub(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.subtract(value2).longValue();
    }

    /**
     * TwolongMultiplying Type Data
     * @param val1  Multiplicand
     * @param val2  multiplier
     * @return      product
     * */
    public static long mul(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.multiply(value2).longValue();
    }


    /**
     * TwolongDividing data by type, retaining default decimal places（8position）
     * @param val1  Dividend
     * @param val2  Divisor
     * @return      merchant
     * */
    public static double exactDiv(long val1, long val2) {
        return DoubleUtils.div(val1, val2);
    }


    /**
     * TwolongDividing data by removing decimal places
     * @param val1  Dividend
     * @param val2  Divisor
     * @return      merchant
     * */
    public static long div(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.divide(value2).longValue();
    }

    /**
     * TwolongRemainder calculation for type data
     * @param val1  Dividend
     * @param val2  Divisor
     * @return      Remainder
     * */
    public static long mod(long val1, long val2) {
        BigInteger value1 = BigInteger.valueOf(val1);
        BigInteger value2 = BigInteger.valueOf(val2);
        return value1.mod(value2).longValue();
    }

    /**
     * Randomly generate aLongType data
     * @return  long
     * */
    public static long randomLong() {
        return (long) (Math.random() * Long.MAX_VALUE);
    }
}
