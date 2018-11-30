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
package io.nuls.base.basic;

import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.basic.VarInt;
import io.nuls.tools.constant.ToolsConstant;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * @author Niels
 */
public class NulsOutputStreamBuffer {

    private final OutputStream out;

    public NulsOutputStreamBuffer(OutputStream out) {
        this.out = out;
    }

    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    public void write(int val) throws IOException {
        out.write(val);
    }

    public void writeVarInt(int val) throws IOException {
        out.write(new VarInt(val).encode());
    }

    public void writeVarInt(long val) throws IOException {
        out.write(new VarInt(val).encode());
    }


    public void writeBytesWithLength(byte[] bytes) throws IOException {
        if (null == bytes || bytes.length == 0) {
            out.write(new VarInt(0).encode());
        } else {
            out.write(new VarInt(bytes.length).encode());
            out.write(bytes);
        }
    }

    public void writeBoolean(boolean val) throws IOException {
        out.write(val ? 1 : 0);
    }

    public void writeShort(short val) throws IOException {
        SerializeUtils.int16ToByteStreamLE(val, out);
    }

    public void writeUint16(int val) throws IOException {
        SerializeUtils.uint16ToByteStreamLE(val, out);
    }

    public void writeUint32(long val) throws IOException {
        SerializeUtils.uint32ToByteStreamLE(val, out);
    }

    public void writeUint48(long time) throws IOException {
        this.write(SerializeUtils.uint48ToBytes(time));
    }

    public void writeBigInteger(BigInteger val) throws IOException{
        this.write(SerializeUtils.bigInteger2Bytes(val));
    }

    public void writeInt64(long val) throws IOException {
        SerializeUtils.int64ToByteStreamLE(val, out);
    }

    public void writeDouble(double val) throws IOException {
        out.write(ByteUtils.doubleToBytes(val));
    }

    public void writeString(String val) {
        if (StringUtils.isBlank(val)) {
            try {
                out.write(new VarInt(0).encode());
            } catch (IOException e) {
                Log.error(e);
                throw new NulsRuntimeException(e);
            }
            return;
        }
        try {
            this.writeBytesWithLength(val.getBytes(ToolsConstant.DEFAULT_ENCODING));
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
    }

    public void writeNulsData(BaseNulsData data) throws IOException {
        if (null == data) {
            write(ToolsConstant.PLACE_HOLDER);
        } else {
            this.write(data.serialize());
        }
    }
}
