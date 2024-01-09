package io.nuls.core.model;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author tag
 * */
public class ByteUtils {
    /**
     * Splice the array into a large array containing all arrays in the order passed in
     * Splices the array into a large array containing all of the arrays in the incoming order.
     *
     * @param arrays The set of arrays you want to concatenate、A collection of arrays that you want to concatenate.
     * @return Splicing results、 the result of the Joining together
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
     * Is the byte array empty
     * @param array Verified byte array
     * @return  If the byte array isnullOr length is0returntrueOtherwise, returnfalse
     * */
    public static final boolean isEmptyOrNull(byte[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * Compare whether two byte arrays are equal
     * @param array1 Byte array to be compared
     * @param array2 Byte array to be compared
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
     * Convert byte arrays to strings
     * @param value  Byte array to be converted
     * @return  The string obtained by direct array conversion
     * */
    public static String asString(byte[] value) {
        return (value == null) ? null : new String(value, UTF_8);
    }

    /**
     * byte[]turnshort
     *
     * @param b Byte array
     * @short Convertedshort
     */
    public static short bytesToShort(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }

    /**
     * Convert byte arrays to integers
     * @param bytes  Byte array to be converted
     * @return  Converted integer
     * */
    public static int bytesToInt(byte[] bytes) {
        int num = 0;
        int temp;
        temp = (0x000000ff & (bytes[0]));
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
     * Byte array tolongConversion of.
     * @param b Byte array
     */
    public static long byteToLong(byte[] b) {
        return (b[0] & 0xffL) |
                ((b[ 1] & 0xffL) << 8) |
                ((b[ 2] & 0xffL) << 16) |
                ((b[ 3] & 0xffL) << 24) |
                ((b[4] & 0xffL) << 32) |
                ((b[5] & 0xffL) << 40) |
                ((b[6] & 0xffL) << 48) |
                ((b[7] & 0xffL) << 56);
    }
    
    /**
     * Byte array conversionBigInteger
     * @param b  Need to convert to byte array
     * @return BigInteger
     * */
    public static BigInteger bytesToBigInteger(byte[] b) {
        if (b[0] < 0) {
            byte[] temp = new byte[b.length + 1];
            temp[0] = 0;
            System.arraycopy(b, 0, temp, 1, b.length);
            return new BigInteger(temp);
        }
        return new BigInteger(b);
    }

    /***
     * Byte array to string conversion
     * @param bytearray  Byte array
     * @return  Convert all strings
     * */
    public static String bytesToString(byte[] bytearray) {
        StringBuilder result = new StringBuilder();
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result.append(temp);
        }
        return result.toString();
    }

    /**
     * holdbyte[]turndouble
     *
     * @return double
     */
    public static double bytesToDouble(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    /**
     * shortConversion to Byte Array.
     * @param num The integer to be converted
     */
    public static byte[] shortToBytes(short num) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (0xff & (num));
        bytes[1] = (byte) (0xff & (num >> 8));
        return bytes;
    }

    /**
     * Convert integers to corresponding byte arrays
     * @param num The integer to be converted
     * @return    Converted byte array
     * */
    public static byte[] intToBytes(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (0xff & (num));
        bytes[1] = (byte) (0xff & (num >> 8));
        bytes[2] = (byte) (0xff & (num >> 16));
        bytes[3] = (byte) (0xff & (num >> 24));
        return bytes;
    }

    /**
     * Convert long integers to corresponding byte arrays
     * @param num  Long integers to be converted
     * @return  Byte array obtained after conversion
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
     * holddoubleConvert tobyte
     *
     * @return byte[]
     */
    public static byte[] doubleToBytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }

    /**
     * Intercept byte array
     * @param input       Source byte array
     * @param startIndex  Start truncating index
     * @param length      Cut length
     * @return            The extracted array of oneself
     * */
    public static byte[] subBytes(byte[] input, int startIndex, int length) {
        byte[] bt = new byte[length];
        System.arraycopy(input, startIndex, bt, 0, length);
        return bt;
    }

    /**
     * Byte array in reverse order
     * @param bytes Source byte array
     * @return  Reverse byte array
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
     * Byte array replication
     * @param in      Source byte array
     * @param length  Copy length
     * @return        The copied byte array
     * */
    public static byte[] copyOf(byte[] in, int length) {
        byte[] out = new byte[length];
        System.arraycopy(in, 0, out, 0, Math.min(length, in.length));
        return out;
    }

    /**
     * Convert byte arrays to strings according to specified encoding
     * @param bytes        Byte array
     * @param charsetName  Encoding rules
     * */
    public static String toString(byte[] bytes, String charsetName) {
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert strings to byte arrays according to specified encoding rules
     * @param str          character string
     * @param charsetName  Encoding rules
     * */
    public static byte[] toBytes(CharSequence str, String charsetName) {
        try {
            return str.toString().getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determine whether a byte array is included in the byte array
     * Determine whether or not a byte array is included in the byte array
     *
     * @param byteList
     * @param bytes
     * */
    public static boolean contains(List<byte[]> byteList,byte[] bytes){
        if(byteList.isEmpty() || bytes == null){
            return false;
        }
        for (byte[] bytesTemp:byteList) {
            if(arrayEquals(bytes,bytesTemp)){
                return true;
            }
        }
        return false;
    }

    /**
     * Convert byte array list to string list
     * Byte Array List to String List
     *
     * @param byteList
     * */
    public static List<String> bytesToStrings(List<byte[]> byteList){
        if(byteList.isEmpty()){
            return null;
        }
        List<String> stringList = new ArrayList<>();
        for (byte[] bytes:byteList) {
            stringList.add(asString(bytes));
        }
        return stringList;
    }
}
