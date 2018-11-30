package io.nuls.tools.data;

import io.nuls.tools.crypto.HexUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Collections;

/**
 * @author tag
 * 2018/11/27
 */
public class BigIntegerUtils {
    public static final String ZERO = String.valueOf(0);

    /**
     * String 转 BigInteger
     *
     * @param str 数字字符串
     */
    public static BigInteger stringToBigInteger(String str) {
        BigInteger bigInteger = new BigInteger("0");
        if (!StringUtils.isBlank(str)) {
            bigInteger = new BigInteger(str);
        }
        return bigInteger;
    }

    /**
     * BigInteger 转 String
     *
     * @param bigInteger 数字
     */
    public static String bigIntegerToString(BigInteger bigInteger) {
        return String.valueOf(bigInteger);
    }

    /**
     * 两个数字字符串相加
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
     * 两个数字字符串相减
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
     * 两个数字字符串相乘
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
     * 两个数字字符串相除
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
     * 两个数字字符串取模
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
     * 两个数字字符串求余
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
     * 两个数字字符串比较大小
     */
    public static int compare(String str1, String str2) {
        return stringToBigInteger(str1).compareTo(stringToBigInteger(str2));
    }

    /**
     * 第一个是等于第二个数
     */
    public static boolean isEqual(String str1, String str2) {
        if (compare(str1, str2) == 0) {
            return true;
        }
        return false;
    }

    /**
     * 第一个是小于第二个数
     */
    public static boolean isLessThan(String str1, String str2) {
        if (compare(str1, str2) >= 0) {
            return false;
        }
        return true;
    }

    /**
     * 第一个是大于第二个数
     */
    public static boolean isGreaterThan(String str1, String str2) {
        if (compare(str1, str2) <= 0) {
            return false;
        }
        return true;
    }

    /**
     * 根据字节数组生成对应的大整数
     * Generate corresponding large integers from byte arrays
     *
     * @param array 小端序的字节数组/Small-endian byte array
     * @return 大整数
     */
    public static BigInteger fromBytes(byte[] array) {
        array = arrayReverse(array);
        return new BigInteger(array);
    }

    /**
     * 将大整数转为字节数组，结果是小端序字节数组
     * Converting large integers to byte arrays results in small endian byte arrays
     *
     * @param value 大整数、BigInteger
     * @return 字节数组
     */
    public static byte[] toBytes(BigInteger value) {
        byte[] bytes = new byte[16];
        byte[] oBytes = value.toByteArray();
        oBytes = arrayReverse(oBytes);
        System.arraycopy(oBytes, 0, bytes, 0, oBytes.length);
        return bytes;
    }

    /**
     * 数组反转工具方法，会返回一个顺序颠倒的新的字节数组
     * The array inversion tool method returns a new byte array in reverse order
     *
     * @param bytes 需要反转的字节数组
     * @return a new byte array in reverse order
     */
    private static byte[] arrayReverse(byte[] bytes) {
        int length = bytes.length;
        byte[] array = new byte[length];
        for (int x = 0; x < length; x++) {
            array[x] = bytes[length - 1 - x];
        }
        return array;
    }

}
