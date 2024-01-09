/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.core.parse;

import io.nuls.core.basic.NulsData;
import io.nuls.core.basic.VarInt;
import io.nuls.core.constant.ToolsConstant;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

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

    public static short readUint8LE(byte[] bytes, int offset) {
        return (short) (bytes[offset] & 0xff);
    }
    /**
     * Parse 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in little endian format./From byte array（Starting with offset）analysis2Bytes, unsigned in small end format16Bit integer
     *
     * @param bytes  Byte array
     * @param offset Offset（Array index）
     */
    public static int readUint16LE(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) |
                ((bytes[offset + 1] & 0xff) << 8);
    }

    /**
     * Parse 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in big endian format./From byte array（Starting with offset）analysis2Bytes, unsigned in large end format32Bit integer
     *
     * @param bytes  Byte array
     * @param offset Offset（Array index）
     */
    public static int readUint16BE(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 8) |
                (bytes[offset + 1] & 0xff);
    }

    /**
     * Parse 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in little endian format.
     * From byte array（Starting with offset）analysis2Bytes, unsigned in end format16Bit integer
     *
     * @param bytes  byte[]
     * @param offset intOffset
     * @return int
     */
    public static int readUint16(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) |
                ((bytes[offset + 1] & 0xff) << 8);
    }

    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format./From byte array（Starting with offset）analysis4Bytes, unsigned in small end format32Bit integer
     *
     * @param bytes  Byte array
     * @param offset Offset（Array index）
     */
    public static long readUint32LE(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL) |
                ((bytes[offset + 1] & 0xffL) << 8) |
                ((bytes[offset + 2] & 0xffL) << 16) |
                ((bytes[offset + 3] & 0xffL) << 24);
    }


    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format./From byte array（Starting with offset）analysis4Bytes, unsigned in small end format32Bit integer
     *
     * @param bytes  Byte array
     * @param offset Offset（Array index）
     */
    public static int readInt32LE(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) |
                ((bytes[offset + 1] & 0xff) << 8) |
                ((bytes[offset + 2] & 0xff) << 16) |
                ((bytes[offset + 3] & 0xff) << 24);
    }

    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in big endian format./From byte array（Starting with offset）analysis4Bytes, unsigned in large end format32Bit integer
     *
     * @param bytes  Byte array
     * @param offset Offset（Array index）
     */
    public static long readUint32BE(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xffL) << 24) |
                ((bytes[offset + 1] & 0xffL) << 16) |
                ((bytes[offset + 2] & 0xffL) << 8) |
                (bytes[offset + 3] & 0xffL);
    }

    /**
     * Parse 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format.
     * From byte array（Starting with offset）analysis4Bytes, unsigned in end format32Bit integer
     *
     * @param bytes  byte[]
     * @param offset intOffset
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
     * From byte array（Starting with offset）analysis6Bytes, unsigned in end format48Bit integer
     *
     * @param bytes  byte[]
     * @param offset intOffset
     * @return int
     */
    public static long readUint48(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffL) |
                ((bytes[offset + 1] & 0xffL) << 8) |
                ((bytes[offset + 2] & 0xffL) << 16) |
                ((bytes[offset + 3] & 0xffL) << 24) |
                ((bytes[offset + 4] & 0xffL) << 32) |
                ((bytes[offset + 5] & 0xffL) << 40);
    }

    /**
     * Parse 8 bytes from the byte array (starting at the offset) as unsigned 64-bit integer in little endian format./From byte array（Starting with offset）analysis8Bytes, unsigned in small end format64Bit integer
     *
     * @param bytes  Byte array
     * @param offset Offset（Array index）
     */
    public static long readInt64LE(byte[] bytes, int offset) {
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
     * Parse 8 bytes from the byte array (starting at the offset) as unsigned 64-bit integer in little endian format.
     * From byte array（Starting with offset）analysis8Bytes, unsigned in end format64Bit integer
     *
     * @param bytes  byte[]
     * @param offset intOffset
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
     * intturnbyte[] (Only rotateintTwo bytes out of four data bytes)
     *
     * @param x int
     * @return byte[]
     */
    public static byte[] int16ToBytes(int x) {
        byte[] bb = new byte[2];
        bb[1] = (byte) (0xFF & x >> 8);
        bb[0] = (byte) (0xFF & x);
        return bb;
    }

    /**
     * intturnbyte[]
     *
     * @param x int
     * @return byte[]
     */
    public static byte[] int32ToBytes(int x) {
        byte[] bb = new byte[4];
        bb[3] = (byte) (0xFF & x >> 24);
        bb[2] = (byte) (0xFF & x >> 16);
        bb[1] = (byte) (0xFF & x >> 8);
        bb[0] = (byte) (0xFF & x);
        return bb;
    }

    /**
     * longData conversionbyte[](Only before turning6Bytes)
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
     * longData conversionbyte[]
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
     * @param input Byte array
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
     * Add ashortType data is stored in small end format as a continuous offset specified in a specified byte array2Out of bytes
     *
     * @param val    shortdata
     * @param out    depositshortByte array of type data
     * @param offset Offset
     */
    public static void int16ToByteArrayLE(short val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
    }

    /**
     * Add aLongType data is stored in large end format as a continuous offset specified in a specified byte array4Out of bytes
     *
     * @param val    longdata
     * @param out    depositLongByte array of type data
     * @param offset Offset
     */
    public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & (val >> 24));
        out[offset + 1] = (byte) (0xFF & (val >> 16));
        out[offset + 2] = (byte) (0xFF & (val >> 8));
        out[offset + 3] = (byte) (0xFF & val);
    }

    /**
     * Add aLongType data is stored in small end format as a continuous offset specified in a specified byte array4Out of bytes
     *
     * @param val    longdata
     * @param out    depositLongByte array of type data
     * @param offset Offset
     */
    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    /**
     * Add aintType data is stored in small end format as a continuous offset specified in a specified byte array4Out of bytes
     *
     * @param val    intdata
     * @param out    depositintByte array of type data
     * @param offset Offset
     */
    public static void int32ToByteArrayLE(int val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    /**
     * Add aLongType data is stored in small end format as a continuous offset specified in a specified byte array8Out of bytes
     *
     * @param val    longdata
     * @param out    depositLongByte array of type data
     * @param offset Offset
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

    public static void uint8ToByteStreamLE(short val, OutputStream stream) throws IOException {
        stream.write((short) (0xFF & val));
    }
    /**
     * Write 2 bytes to the output stream as unsigned 16-bit short in little endian format./take2Byte write output stream as unsigned16positionshortType data, in small end format
     *
     * @param val    shortdata
     * @param stream Output stream
     */
    public static void int16ToByteStreamLE(short val, OutputStream stream) throws IOException {
        stream.write((byte) (0xFF & val));
        stream.write((byte) (0xFF & (val >> 8)));
    }

    /**
     * Write 2 bytes to the output stream as unsigned 16-bit integer in little endian format./take2Byte write output stream as unsigned16Bit integer, in small end format
     *
     * @param val    intdata
     * @param stream Output stream
     */
    public static void uint16ToByteStreamLE(int val, OutputStream stream) throws IOException {
        stream.write((int) (0xFF & val));
        stream.write((int) (0xFF & (val >> 8)));
    }

    /**
     * Write 4 bytes to the output stream as unsigned 32-bit long in little endian format./take4Byte write output stream as unsigned32positionlongType data, in small end format
     *
     * @param val    longdata
     * @param stream Output stream
     */
    public static void uint32ToByteStreamLE(long val, OutputStream stream) throws IOException {
        stream.write((int) (0xFF & val));
        stream.write((int) (0xFF & (val >> 8)));
        stream.write((int) (0xFF & (val >> 16)));
        stream.write((int) (0xFF & (val >> 24)));
    }

    /**
     * Write 8 bytes to the output stream as unsigned 64-bit long in little endian format./take8Byte write output stream as unsigned64positionlongType data, in small end format
     *
     * @param val    longdata
     * @param stream Output stream
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
     * Write 8 bytes to the output stream as unsigned 64-bit BigInteger in little endian format./take8Byte write output stream as unsigned64positionBigIntegerType data, in small end format
     *
     * @param val    BigIntegerdata
     * @param stream Output stream
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
     * takeDoubleWrite type data to output stream
     *
     * @param val    BigIntegerdata
     * @param stream Output stream
     */
    public static void doubleToByteStream(double val, OutputStream stream) throws IOException {
        stream.write(ByteUtils.doubleToBytes(val));
    }

    public static int sizeOfDouble(Double val) {
        return MAGIC_8;
    }

    /**
     * Convert string tobyte[]The length of
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
            Log.error(e.getMessage());
            throw new NulsRuntimeException(e);
        }
        return sizeOfBytes(bytes);
    }

    /**
     * calculateLongHow many bytes does type data occupy
     *
     * @param val Long
     * @return int
     */
    public static int sizeOfVarInt(Long val) {
        return VarInt.sizeOf(val);
    }

    /**
     * obtainInt16Bytes occupied by data
     *
     * @return int
     */
    public static int sizeOfInt16() {
        return 2;
    }

    /**
     * obtainBigIntegerBytes occupied by data
     *
     * @return int
     */
    public static int sizeOfBigInteger() {
        return 32;
    }

    /**
     * obtainUint16Bytes occupied by data
     *
     * @return int
     */
    public static int sizeOfUint16() {
        return 2;
    }
    /**
     * obtainUint8Bytes occupied by data
     *
     * @return int
     */
    public static int sizeOfUint8() {
        return 1;
    }
    /**
     * obtainInt32Bytes occupied by data
     *
     * @return int
     */
    public static int sizeOfInt32() {
        return 4;
    }

    /**
     * obtainUint32Bytes occupied by data
     *
     * @return int
     */
    public static int sizeOfUint32() {
        return 4;
    }

    /**
     * obtainUnit48Bytes occupied by data
     *
     * @return int
     */
    public static int sizeOfUint48() {
        return ToolsConstant.INT48_VALUE_LENGTH;
    }

    /**
     * obtainInt64Bytes occupied by data
     *
     * @return int
     */
    public static int sizeOfInt64() {
        return 8;
    }

    /**
     * Get specifiedIntegerMinimum number of bytes occupied
     *
     * @param val Integer
     * @return int
     */
    public static int sizeOfVarInt(Integer val) {
        return VarInt.sizeOf(val);
    }

    /**
     * obtainBooleanBytes occupied by data
     *
     * @return int
     */
    public static int sizeOfBoolean() {
        return 1;
    }


    /**
     * obtainnonceBytes occupied by data
     *
     * @return int
     */
    public static int sizeOfNonce() {
        return 8;
    }

    /**
     * obtainbyte[]Minimum number of bytes occupied（byte[]The minimum number of bytes occupied by length+byte[]length）
     */
    public static int sizeOfBytes(byte[] val) {
        if (null == val) {
            return 1;
        }
        return VarInt.sizeOf((val).length) + (val).length;
    }

    /**
     * obtainNulsDataByte length occupied by the object
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


    /**
     * Generate corresponding large integers based on byte arrays
     * Generate corresponding large integers from byte arrays
     *
     * @param array Byte array of small end sequence/Small-endian byte array
     * @return Large integer
     */
    public static BigInteger bigIntegerFromBytes(byte[] array) {
        array = arrayReverse(array);
        return new BigInteger(array);
    }

    /**
     * Convert large integers to byte arrays, resulting in a small end order byte array
     * Converting large integers to byte arrays results in small endian byte arrays
     *
     * @param value Large integer、BigInteger
     * @return Byte array
     */
    public static byte[] bigInteger2Bytes(BigInteger value) {
        byte[] bytes = new byte[32];
        byte[] oBytes = value.toByteArray();
        oBytes = arrayReverse(oBytes);
        if (oBytes.length > 32) {
            throw new RuntimeException("The number is too large!");
        }
        System.arraycopy(oBytes, 0, bytes, 0, oBytes.length);
        return bytes;
    }


    /**
     * The array inversion tool method returns a new byte array in reverse order
     * The array inversion tool method returns a new byte array in reverse order
     *
     * @param bytes Byte array that needs to be reversed
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
