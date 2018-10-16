package io.nuls.tools.data;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author tag
 * */
public class ByteUtils {
    /**
     * 按照传入的顺序拼接数组为一个包含所有数组的大数组
     * Splices the array into a large array containing all of the arrays in the incoming order.
     *
     * @param arrays 想要拼接的数组集合、A collection of arrays that you want to concatenate.
     * @return 拼接结果、 the result of the Joining together
     */
    public static final byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] t = new byte[length];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, t, offset, array.length);
            offset += array.length;
        }
        return t;
    }

    /**
     * 字节数组是否为空
     * @param array 校验的字节数组
     * @return  如果字节数组为null或长度为0返回true否则返回false
     * */
    public static final boolean isEmptyOrNull(byte[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * 比较两个字节数组是否相等
     * @param array1 需比较的字节数组
     * @param array2 需比较的字节数组
     * @return
     * */
    public static boolean arrayEquals(byte[] array1, byte[] array2) {
        return Arrays.equals(array1, array2);
    }

    public static byte caculateXor(byte[] data) {
        byte xor = 0x00;
        if (data == null || data.length == 0) {
            return xor;
        }
        for (int i = 0; i < data.length; i++) {
            xor ^= data[i];
        }
        return xor;
    }

    /**
     * 将字节数组转为字符串
     * @param value  需转换的字节数组
     * @return  直接数组转换得到的字符串
     * */
    public static String asString(byte[] value) {
        return (value == null) ? null : new String(value, UTF_8);
    }

    /**
     * 将整型数转为对应的字节数组
     * @param num 需转换的整型数
     * @return    转换后的字节数组
     * */
    public static byte[] intToBytes(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (0xff & (num >> 0));
        bytes[1] = (byte) (0xff & (num >> 8));
        bytes[2] = (byte) (0xff & (num >> 16));
        bytes[3] = (byte) (0xff & (num >> 24));
        return bytes;
    }

    /**
     * 将字节数组转为整型数
     * @param bytes  需转换的字节数组
     * @return  转换得到的整型数
     * */
    public static int byteToInt(byte[] bytes) {
        int num = 0;
        int temp;
        temp = (0x000000ff & (bytes[0])) << 0;
        num = num | temp;
        temp = (0x000000ff & (bytes[1])) << 8;
        num = num | temp;
        temp = (0x000000ff & (bytes[2])) << 16;
        num = num | temp;
        temp = (0x000000ff & (bytes[3])) << 24;
        num = num | temp;
        return num;
    }


    /**
     * 将长整型数转为对应的字节数组
     * @param num  需转换的长整型数
     * @return  转换后得到的字节数组
     * */
    public static byte[] longToBytes(long num) {
        int length = 8;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (0xff & (num >> (i * 8)));
        }
        return bytes;
    }

    /**
     * 将BigInteger类型数据转为byte[]
     * @param n  需要转换的数据
     * @return
     * */
    public static byte[] byteConvert32Bytes(BigInteger n) {
        byte[] temp;
        if (n == null) {
            return null;
        }
        int length = 32;
        if (n.toByteArray().length == length+1) {
            temp = new byte[length];
            System.arraycopy(n.toByteArray(), 1, temp, 0, 32);
        } else if (n.toByteArray().length == length) {
            temp = n.toByteArray();
        } else {
            temp = new byte[length];
            for (int i = 0; i < length - n.toByteArray().length; i++) {
                temp[i] = 0;
            }
            System.arraycopy(n.toByteArray(), 0, temp, length - n.toByteArray().length, n.toByteArray().length);
        }
        return temp;
    }

    /**
     * 字节数组转BigInteger
     * @param b  需转换在字节数组
     * @return BigInteger
     * */
    public static BigInteger byteConvertInteger(byte[] b) {
        if (b[0] < 0) {
            byte[] temp = new byte[b.length + 1];
            temp[0] = 0;
            System.arraycopy(b, 0, temp, 1, b.length);
            return new BigInteger(temp);
        }
        return new BigInteger(b);
    }


    /**
     * Hex解码时使用，字符的下标
     * @param c  字符
     * @return 字符下标
     * */
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /***
     * 字节数组转字符串
     * @param bytearray  字节数组
     * @return  转换都的字符串
     * */
    public static String byteToString(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }

    /**
     * 截取字节数组
     * @param input       源字节数组
     * @param startIndex  开始截取的下标
     * @param length      截取的长度
     * @return            截取出的自己数组
     * */
    public static byte[] subByte(byte[] input, int startIndex, int length) {
        byte[] bt = new byte[length];
        for (int i = 0; i < length; i++) {
            bt[i] = input[i + startIndex];
        }
        return bt;
    }

    /**
     * 字节数组逆序
     * @param bytes 源字节数组
     * @return  逆序字节数组
     * */
    public static byte[] reverseBytes(byte[] bytes) {
        // We could use the XOR trick here but it's easier to understand if we don't. If we find this is really a
        // performance issue the matter can be revisited.
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            buf[i] = bytes[bytes.length - 1 - i];
        }
        return buf;
    }


    /**
     * 字节数组复制
     * @param in      源字节数组
     * @param length  复制的长度
     * @return        复制出的字节数组
     * */
    public static byte[] copyOf(byte[] in, int length) {
        byte[] out = new byte[length];
        System.arraycopy(in, 0, out, 0, Math.min(length, in.length));
        return out;
    }

    /**
     * 字节数组按指定编码转为字符串
     * @param bytes        字节数组
     * @param charsetName  编码规则
     * */
    public static String toString(byte[] bytes, String charsetName) {
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串按指定编码规则转为字节数组
     * @param str          字符串
     * @param charsetName  编码规则
     * */
    public static byte[] toBytes(CharSequence str, String charsetName) {
        try {
            return str.toString().getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(Integer.toHexString(15));
        System.out.println(Integer.toHexString(255));
        System.out.println(Integer.toString((15 & 0xff) + 0x100, 16).substring(1));
    }
}
