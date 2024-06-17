package io.nuls.core.model;

import java.math.BigInteger;

/**
 * @author tag
 * 2018/11/27
 */
public class BigIntegerUtils {
    public static final String ZERO = String.valueOf(0);

    /**
     * String turn BigInteger
     *
     * @param str Number string
     */
    public static BigInteger stringToBigInteger(String str) {
        BigInteger bigInteger = new BigInteger("0");
        if (!StringUtils.isBlank(str)) {
            bigInteger = new BigInteger(str);
        }
        return bigInteger;
    }

    /**
     * BigInteger turn String
     *
     * @param bigInteger number
     */
    public static String bigIntegerToString(BigInteger bigInteger) {
        return String.valueOf(bigInteger);
    }

    public static String bigIntegerToString(BigInteger bigInteger, int size) {
        String symbol = "";
        if (bigInteger.compareTo(BigInteger.ZERO) < 0) {
            symbol = "-";
            bigInteger = bigInteger.abs();
        }
        String value = bigInteger.toString();
        int length = size - value.length();
        if (length > 0) {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < length; i++) {
                buffer.append(0);
            }
            value = buffer.append(value).toString();
        }
        return symbol + value;
    }

    /**
     * Adding Two Numeric Strings
     *
     * @param str1
     * @param str2
     */
    public static String addToString(String str1, String str2) {
        return bigIntegerToString(stringToBigInteger(str1).add(stringToBigInteger(str2)));
    }

    public static BigInteger addToBigInteger(String str1, String str2) {
        return stringToBigInteger(str1).add(stringToBigInteger(str2));
    }

    /**
     * Subtracting two numeric strings
     *
     * @param str1
     * @param str2
     */
    public static String subToString(String str1, String str2) {
        return bigIntegerToString(stringToBigInteger(str1).subtract(stringToBigInteger(str2)));
    }

    public static BigInteger subToBigInteger(String str1, String str2) {
        return stringToBigInteger(str1).subtract(stringToBigInteger(str2));
    }

    /**
     * Multiplying two numeric strings
     *
     * @param str1
     * @param str2
     */
    public static String mulToString(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return "0";
        }
        return bigIntegerToString(stringToBigInteger(str1).multiply(stringToBigInteger(str2)));
    }

    public static BigInteger mulToInteger(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return new BigInteger("0");
        }
        return stringToBigInteger(str1).multiply(stringToBigInteger(str2));
    }

    /**
     * Dividing Two Numeric Strings
     *
     * @param str1
     * @param str2
     */
    public static String divToString(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return "0";
        }
        return bigIntegerToString(stringToBigInteger(str1).divide(stringToBigInteger(str2)));
    }

    public static BigInteger divToBigInteger(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return new BigInteger("0");
        }
        return stringToBigInteger(str1).divide(stringToBigInteger(str2));
    }

    /**
     * Module two numeric strings
     *
     * @param str1
     * @param str2
     */
    public static String modToString(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return "0";
        }
        return bigIntegerToString(stringToBigInteger(str1).mod(stringToBigInteger(str2)));
    }

    public static BigInteger modToBigIntegr(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return new BigInteger("0");
        }
        return stringToBigInteger(str1).mod(stringToBigInteger(str2));
    }

    /**
     * Taking the remainder of two numeric strings
     *
     * @param str1
     * @param str2
     */
    public static String remToString(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return "0";
        }
        return bigIntegerToString(stringToBigInteger(str1).remainder(stringToBigInteger(str2)));
    }

    public static BigInteger remToBigInteger(String str1, String str2) {
        if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
            return new BigInteger("0");
        }
        return stringToBigInteger(str1).remainder(stringToBigInteger(str2));
    }

    /**
     * Comparing the size of two numeric strings
     */
    public static int compare(String str1, String str2) {
        return stringToBigInteger(str1).compareTo(stringToBigInteger(str2));
    }

    /**
     * The first number is equal to the second number
     */
    public static boolean isEqual(String str1, String str2) {
        return compare(str1, str2) == 0;
    }

    public static boolean isEqual(BigInteger b1, BigInteger b2) {
        return b1.compareTo(b2) == 0;
    }

    /**
     * The first number is less than the second number
     */
    public static boolean isLessThan(String str1, String str2) {
        return compare(str1, str2) < 0;
    }

    public static boolean isLessThan(BigInteger b1, BigInteger b2) {
        return b1.compareTo(b2) < 0;
    }

    /**
     * The first number is greater than the second number
     */
    public static boolean isGreaterThan(String str1, String str2) {
        return compare(str1, str2) > 0;
    }

    public static boolean isGreaterThan(BigInteger b1, BigInteger b2) {
        return b1.compareTo(b2) > 0;
    }

    /**
     * The first number is greater than or equal to the second number
     */
    public static boolean isEqualOrGreaterThan(BigInteger b1, BigInteger b2) {
        return b1.compareTo(b2) >= 0;
    }

    /**
     * The first number is less than or equal to the second number
     */
    public static boolean isEqualOrLessThan(BigInteger b1, BigInteger b2) {
        return b1.compareTo(b2) <= 0;
    }
}
