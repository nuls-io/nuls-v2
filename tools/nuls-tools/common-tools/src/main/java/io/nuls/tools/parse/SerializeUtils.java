/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.tools.parse;

import io.nuls.tools.basic.NulsData;
import io.nuls.tools.basic.VarInt;
import io.nuls.tools.constant.ToolsConstant;
import io.nuls.tools.crypto.Sha256Hash;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * @author tag
 */
public class SerializeUtils {

    public static final Charset CHARSET = Charset.forName(ToolsConstant.DEFAULT_ENCODING);
    private static final int MAGIC_8 = 8;
    private static final int MAGIC_0X80 = 0x80;
    /**
     * The string that prefixes all text messages signed using Bitcoin keys.
     */
    public static final String SIGNED_MESSAGE_HEADER = "RiceChain Signed Message:\n";
    public static final byte[] SIGNED_MESSAGE_HEADER_BYTES = SIGNED_MESSAGE_HEADER.getBytes(CHARSET);

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     *
     * @param hasLength can be set to false if the given array is missing the 4 byte length field
     */
    public static BigInteger decodeMPI(byte[] mpi, boolean hasLength) {
        byte[] buf;
        if (hasLength) {
            int length = (int) readUint32BE(mpi, 0);
            buf = new byte[length];
            System.arraycopy(mpi, 4, buf, 0, length);
        } else {
            buf = mpi;
        }
        if (buf.length == 0) {
            return BigInteger.ZERO;
        }
        boolean isNegative = (buf[0] & MAGIC_0X80) == MAGIC_0X80;
        if (isNegative) {
            buf[0] &= 0x7f;
        }
        BigInteger result = new BigInteger(buf);
        return isNegative ? result.negate() : result;
    }

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     *
     * @param includeLength indicates whether the 4 byte length field should be included
     */
    public static byte[] encodeMPI(BigInteger value, boolean includeLength) {
        if (value.equals(BigInteger.ZERO)) {
            if (!includeLength) {
                return new byte[]{};
            } else {
                return new byte[]{0x00, 0x00, 0x00, 0x00};
            }
        }
        boolean isNegative = value.signum() < 0;
        if (isNegative) {
            value = value.negate();
        }
        byte[] array = value.toByteArray();
        int length = array.length;
        if ((array[0] & MAGIC_0X80) == MAGIC_0X80) {
            length++;
        }
        if (includeLength) {
            byte[] result = new byte[length + 4];
            System.arraycopy(array, 0, result, length - array.length + 3, array.length);
            uint32ToByteArrayBE(length, result, 0);
            if (isNegative) {
                result[4] |= MAGIC_0X80;
            }
            return result;
        } else {
            byte[] result;
            if (length != array.length) {
                result = new byte[length];
                System.arraycopy(array, 0, result, 1, array.length);
            } else {
                result = array;
            }
            if (isNegative) {
                result[0] |= MAGIC_0X80;
            }
            return result;
        }
    }


    /**
     * Given a textual message, returns a byte buffer formatted as follows:</p>
     * <p>
     * <tt>[24] "Bitcoin Signed Message:\n" [message.length as a varint] message</p></tt>
     */
    public static byte[] formatMessageForSigning(String message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(SIGNED_MESSAGE_HEADER_BYTES.length);
            bos.write(SIGNED_MESSAGE_HEADER_BYTES);
            byte[] messageBytes = message.getBytes(CHARSET);
            VarInt size = new VarInt(messageBytes.length);
            bos.write(size.encode());
            bos.write(messageBytes);
            return bos.toByteArray();
        } catch (IOException e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in little endian format./从字节数组（以偏移量开始）解析2字节，以小端格式的无符号16位整数
     *
     * @param bytes  字节数组
     * @param offset 偏移量（数组下标）
     */
    public static int readUint16LE(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) |
                ((bytes[offset + 1] & 0xff) << 8);
    }

    /**
     * Parse 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in big endian format./从字节数组（以偏移量开始）解析2字节，以大端格式的无符号32位整数
     *
     * @param bytes  字节数组
     * @param offset 偏移量（数组下标）
     */
    public static int readUint16BE(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 8) |
                (bytes[offset + 1] & 0xff);
    }

    /**
     * Parse 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in little endian format.
     * 从字节数组（以偏移量开始）解析2字节，以端格式的无符号16位整数
     *
     * @param bytes  byte[]
     * @param offset int偏移量
     * @return int
     */
    public static int readUint16(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) |
                ((bytes[offset + 1] & 0xff) << 8);
    }

    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format./从字节数组（以偏移量开始）解析4字节，以小端格式的无符号32位整数
     *
     * @param bytes  字节数组
     * @param offset 偏移量（数组下标）
     */
    public static long readUint32LE(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL) |
                ((bytes[offset + 1] & 0xffL) << 8) |
                ((bytes[offset + 2] & 0xffL) << 16) |
                ((bytes[offset + 3] & 0xffL) << 24);
    }


    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format./从字节数组（以偏移量开始）解析4字节，以小端格式的无符号32位整数
     *
     * @param bytes  字节数组
     * @param offset 偏移量（数组下标）
     */
    public static int readInt32LE(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) |
                ((bytes[offset + 1] & 0xff) << 8) |
                ((bytes[offset + 2] & 0xff) << 16) |
                ((bytes[offset + 3] & 0xff) << 24);
    }

    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in big endian format./从字节数组（以偏移量开始）解析4字节，以大端格式的无符号32位整数
     *
     * @param bytes  字节数组
     * @param offset 偏移量（数组下标）
     */
    public static long readUint32BE(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xffL) << 24) |
                ((bytes[offset + 1] & 0xffL) << 16) |
                ((bytes[offset + 2] & 0xffL) << 8) |
                (bytes[offset + 3] & 0xffL);
    }

    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format.
     * 从字节数组（以偏移量开始）解析4字节，以端格式的无符号32位整数
     *
     * @param bytes  byte[]
     * @param offset int偏移量
     * @return int
     */
    public static long readUint32(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL) |
                ((bytes[offset + 1] & 0xffL) << 8) |
                ((bytes[offset + 2] & 0xffL) << 16) |
                ((bytes[offset + 3] & 0xffL) << 24);
    }

    /**
     * Parse 6 bytes from the byte array (starting at the offset) as unsigned 48-bit integer in little endian format.
     * 从字节数组（以偏移量开始）解析6字节，以端格式的无符号48位整数
     *
     * @param bytes  byte[]
     * @param offset int偏移量
     * @return int
     */
    public static long readUint48(byte[] bytes, int offset) {
        return  (bytes[offset] & 0xffL) |
                ((bytes[offset + 1] & 0xffL) << 8) |
                ((bytes[offset + 2] & 0xffL) << 16) |
                ((bytes[offset + 3] & 0xffL) << 24) |
                ((bytes[offset + 4] & 0xffL) << 32) |
                ((bytes[offset + 5] & 0xffL) << 40) ;
    }

    /**
     * Parse 8 bytes from the byte array (starting at the offset) as unsigned 64-bit integer in little endian format./从字节数组（以偏移量开始）解析8字节，以小端格式的无符号64位整数
     *
     * @param bytes  字节数组
     * @param offset 偏移量（数组下标）
     */
    public static long readInt64LE(byte[] bytes, int offset) {
        return  (bytes[offset] & 0xffL) |
                ((bytes[offset + 1] & 0xffL) << 8) |
                ((bytes[offset + 2] & 0xffL) << 16) |
                ((bytes[offset + 3] & 0xffL) << 24) |
                ((bytes[offset + 4] & 0xffL) << 32) |
                ((bytes[offset + 5] & 0xffL) << 40) |
                ((bytes[offset + 6] & 0xffL) << 48) |
                ((bytes[offset + 7] & 0xffL) << 56);
    }

    /**
     * Parse 8 bytes from the byte array (starting at the offset) as unsigned 64-bit integer in little endian format.
     * 从字节数组（以偏移量开始）解析8字节，以端格式的无符号64位整数
     *
     * @param bytes  byte[]
     * @param offset int偏移量
     * @return int
     */
    public static long readUint64(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL) |
                ((bytes[offset + 1] & 0xffL) << 8) |
                ((bytes[offset + 2] & 0xffL) << 16) |
                ((bytes[offset + 3] & 0xffL) << 24) |
                ((bytes[offset + 4] & 0xffL) << 32) |
                ((bytes[offset + 5] & 0xffL) << 40) |
                ((bytes[offset + 6] & 0xffL) << 48) |
                ((bytes[offset + 7] & 0xffL) << 56);
    }

    /**
     * int转byte[] (只转int数据四个字节中的两个字节)
     *
     * @param x int
     * @return byte[]
     */
    public static byte[] int16ToBytes(int x) {
        byte[] bb = new byte[2];
        bb[1] = (byte) (0xFF & x >> 8);
        bb[0] = (byte) (0xFF & x >> 0);
        return bb;
    }

    /**
     * int转byte[]
     *
     * @param x int
     * @return byte[]
     */
    public static byte[] int32ToBytes(int x) {
        byte[] bb = new byte[4];
        bb[3] = (byte) (0xFF & x >> 24);
        bb[2] = (byte) (0xFF & x >> 16);
        bb[1] = (byte) (0xFF & x >> 8);
        bb[0] = (byte) (0xFF & x >> 0);
        return bb;
    }

    /**
     * long数据转byte[](只转前6个字节)
     *
     * @param val long
     * @return byte[]
     */
    public static byte[] uint48ToBytes(long val) {
        byte[] bytes = new byte[SerializeUtils.sizeOfUint48()];
        bytes[0] = (byte) (0xFF & val);
        bytes[1] = (byte) (0xFF & (val >> 8));
        bytes[2] = (byte) (0xFF & (val >> 16));
        bytes[3] = (byte) (0xFF & (val >> 24));
        bytes[4] = (byte) (0xFF & (val >> 32));
        bytes[5] = (byte) (0xFF & (val >> 40));
        return bytes;
    }

    /**
     * long数据转byte[]
     *
     * @param val long
     * @return byte[]
     */
    public static byte[] uint64ToByteArray(long val) {
        byte[] out = new byte[8];
        out[0] = (byte) (0xFF & val);
        out[1] = (byte) (0xFF & (val >> 8));
        out[2] = (byte) (0xFF & (val >> 16));
        out[3] = (byte) (0xFF & (val >> 24));
        out[4] = (byte) (0xFF & (val >> 32));
        out[5] = (byte) (0xFF & (val >> 40));
        out[6] = (byte) (0xFF & (val >> 48));
        out[7] = (byte) (0xFF & (val >> 56));
        return out;
    }

    /**
     * Calculates RIPEMD160(SHA256(input)). This is used in Address calculations.
     *
     * @param input 字节数组
     */
    public static byte[] sha256hash160(byte[] input) {
        byte[] sha256 = Sha256Hash.hash(input);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }

    /**
     * The regular {@link BigInteger#toByteArray()} method isn't quite what we often need: it appends a
     * leading zero to indicate that the number is positive and may need padding.
     *
     * @param b        the integer to format into a byte array
     * @param numBytes the desired size of the resulting byte array
     * @return numBytes byte long array.
     */
    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        if (b == null) {
            return null;
        }
        byte[] bytes = new byte[numBytes];
        byte[] biBytes = b.toByteArray();
        int start = (biBytes.length == numBytes + 1) ? 1 : 0;
        int length = Math.min(biBytes.length, numBytes);
        System.arraycopy(biBytes, start, bytes, numBytes - length, length);
        return bytes;
    }


    /**
     * 将一个short型数据以小端格式存储到指定字节数组指定的偏移量的连续2个字节中
     *
     * @param val    short数据
     * @param out    存放short型数据的字节数组
     * @param offset 偏移量
     */
    public static void int16ToByteArrayLE(short val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
    }

    /**
     * 将一个Long型数据以大端格式存储到指定字节数组指定的偏移量的连续4个字节中
     *
     * @param val    long数据
     * @param out    存放Long型数据的字节数组
     * @param offset 偏移量
     */
    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >> 8));
        out[offset + 3] = (byte) (0xFF & val);
    }

    /**
     * 将一个Long型数据以小端格式存储到指定字节数组指定的偏移量的连续4个字节中
     *
     * @param val    long数据
     * @param out    存放Long型数据的字节数组
     * @param offset 偏移量
     */
    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    /**
     * 将一个int型数据以小端格式存储到指定字节数组指定的偏移量的连续4个字节中
     *
     * @param val    int数据
     * @param out    存放int型数据的字节数组
     * @param offset 偏移量
     */
    public static void int32ToByteArrayLE(int val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    /**
     * 将一个Long型数据以小端格式存储到指定字节数组指定的偏移量的连续8个字节中
     *
     * @param val    long数据
     * @param out    存放Long型数据的字节数组
     * @param offset 偏移量
     */
    public static void uint64ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
        out[offset + 4] = (byte) (0xFF & (val >> 32));
        out[offset + 5] = (byte) (0xFF & (val >> 40));
        out[offset + 6] = (byte) (0xFF & (val >> 48));
        out[offset + 7] = (byte) (0xFF & (val >> 56));
    }

    /**
     * Write 2 bytes to the output stream as unsigned 16-bit short in little endian format./将2字节写入输出流作为无符号16位short型数据，以小端格式
     *
     * @param val    short数据
     * @param stream 输出流
     */
    public static void int16ToByteStreamLE(short val, OutputStream stream) throws IOException {
        stream.write((byte) (0xFF & val));
        stream.write((byte) (0xFF & (val >> 8)));
    }

    /**
     * Write 2 bytes to the output stream as unsigned 16-bit integer in little endian format./将2字节写入输出流作为无符号16位整数，以小端格式
     *
     * @param val    int数据
     * @param stream 输出流
     */
    public static void uint16ToByteStreamLE(int val, OutputStream stream) throws IOException {
        stream.write((int) (0xFF & val));
        stream.write((int) (0xFF & (val >> 8)));
    }

    /**
     * Write 4 bytes to the output stream as unsigned 32-bit long in little endian format./将4字节写入输出流作为无符号32位long型数据，以小端格式
     *
     * @param val    long数据
     * @param stream 输出流
     */
    public static void uint32ToByteStreamLE(long val, OutputStream stream) throws IOException {
        stream.write((int) (0xFF & val));
        stream.write((int) (0xFF & (val >> 8)));
        stream.write((int) (0xFF & (val >> 16)));
        stream.write((int) (0xFF & (val >> 24)));
    }

    /**
     * Write 8 bytes to the output stream as unsigned 64-bit long in little endian format./将8字节写入输出流作为无符号64位long型数据，以小端格式
     *
     * @param val    long数据
     * @param stream 输出流
     */
    public static void int64ToByteStreamLE(long val, OutputStream stream) throws IOException {
        stream.write((int) (0xFF & val));
        stream.write((int) (0xFF & (val >> 8)));
        stream.write((int) (0xFF & (val >> 16)));
        stream.write((int) (0xFF & (val >> 24)));
        stream.write((int) (0xFF & (val >> 32)));
        stream.write((int) (0xFF & (val >> 40)));
        stream.write((int) (0xFF & (val >> 48)));
        stream.write((int) (0xFF & (val >> 56)));
    }


    /**
     * Write 8 bytes to the output stream as unsigned 64-bit BigInteger in little endian format./将8字节写入输出流作为无符号64位BigInteger型数据，以小端格式
     *
     * @param val    BigInteger数据
     * @param stream 输出流
     */
    public static void uint64ToByteStreamLE(BigInteger val, OutputStream stream) throws IOException {
        byte[] bytes = val.toByteArray();
        if (bytes.length > MAGIC_8) {
            throw new RuntimeException("Input too large to encode into a uint64");
        }
        bytes = ByteUtils.reverseBytes(bytes);
        stream.write(bytes);
        if (bytes.length < MAGIC_8) {
            for (int i = 0; i < MAGIC_8 - bytes.length; i++) {
                stream.write(0);
            }
        }
    }

    /**
     * 将Double类型数据写入输出流
     *
     * @param val    BigInteger数据
     * @param stream 输出流
     */
    public static void doubleToByteStream(double val, OutputStream stream) throws IOException {
        stream.write(ByteUtils.doubleToBytes(val));
    }

    public static int sizeOfDouble(Double val) {
        return MAGIC_8;
    }

    /**
     * 字符串转为byte[]的长度
     *
     * @param val String
     * @return int
     */
    public static int sizeOfString(String val) {
        if (null == val) {
            return 1;
        }
        byte[] bytes;
        try {
            bytes = val.getBytes(ToolsConstant.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        return sizeOfBytes(bytes);
    }

    /**
     * 计算Long型数据占几个字节
     *
     * @param val Long
     * @return int
     */
    public static int sizeOfVarInt(Long val) {
        return VarInt.sizeOf(val);
    }

    /**
     * 获取Int16数据占的字节数
     *
     * @return int
     */
    public static int sizeOfInt16() {
        return 2;
    }

    /**
     * 获取Uint16数据占的字节数
     *
     * @return int
     */
    public static int sizeOfUint16() {
        return 2;
    }

    /**
     * 获取Int32数据占的字节数
     *
     * @return int
     */
    public static int sizeOfInt32() {
        return 4;
    }

    /**
     * 获取Uint32数据占的字节数
     *
     * @return int
     */
    public static int sizeOfUint32() {
        return 4;
    }

    /**
     * 获取Unit48数据占的字节数
     *
     * @return int
     */
    public static int sizeOfUint48() {
        return ToolsConstant.INT48_VALUE_LENGTH;
    }

    /**
     * 获取Int64数据占的字节数
     *
     * @return int
     */
    public static int sizeOfInt64() {
        return 8;
    }

    /**
     * 获取指定Integer占用的最小字节数
     *
     * @param val Integer
     * @return int
     */
    public static int sizeOfVarInt(Integer val) {
        return VarInt.sizeOf(val);
    }

    /**
     * 获取Boolean数据占的字节数
     *
     * @return int
     */
    public static int sizeOfBoolean() {
        return 1;
    }

    /**
     * 获取byte[]占用的最小字节数（byte[]长度占用的最小字节数+byte[]长度）
     */
    public static int sizeOfBytes(byte[] val) {
        if (null == val) {
            return 1;
        }
        return VarInt.sizeOf((val).length) + (val).length;
    }

    /**
     * 获取NulsData对象所占字节长度
     *
     * @param val NulsData
     * @return int
     */
    public static int sizeOfNulsData(NulsData val) {
        if (null == val) {
            return ToolsConstant.PLACE_HOLDER.length;
        }
        int size = val.size();
        return size == 0 ? 1 : size;
    }
}
