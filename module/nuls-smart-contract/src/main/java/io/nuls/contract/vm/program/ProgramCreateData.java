/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.vm.program;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * @Author: PierreLuo
 */
public class ProgramCreateData extends BaseNulsData {

    private final byte hard = (byte) 255;
    private byte[] sender;
    private byte[] salt;
    private byte[] codeHash;
    //private short argsCount;
    //private String[][] args;


    public ProgramCreateData(byte[] sender, byte[] salt, byte[] codeHash) {
        this.sender = sender;
        this.salt = salt;
        this.codeHash = codeHash;
    }

    //public ProgramCreateData(byte[] sender, byte[] salt, byte[] code, short argsCount, String[][] args) {
    //    this.sender = sender;
    //    this.salt = salt;
    //    this.code = code;
    //    this.argsCount = argsCount;
    //    this.args = args;
    //}

    @Override
    public int size() {
        int size = 1;
        size += Address.ADDRESS_LENGTH;
        size += SerializeUtils.sizeOfBytes(salt);
        size += SerializeUtils.sizeOfBytes(codeHash);
        //size += 1;
        //if (args != null) {
        //    for (String[] arg : args) {
        //        if (arg == null) {
        //            size += 1;
        //        } else {
        //            size += 1;
        //            for (String str : arg) {
        //                size += SerializeUtils.sizeOfString(str);
        //            }
        //        }
        //    }
        //}
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(hard);
        stream.write(sender);
        stream.writeBytesWithLength(salt);
        stream.writeBytesWithLength(codeHash);
        //stream.writeUint8(argsCount);
        //if (args != null) {
        //    for (String[] arg : args) {
        //        if (arg == null) {
        //            stream.writeUint8((short) 0);
        //        } else {
        //            stream.writeUint8((short) arg.length);
        //            for (String str : arg) {
        //                stream.writeString(str);
        //            }
        //        }
        //    }
        //}
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        byteBuffer.readByte();
        this.sender = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.salt = byteBuffer.readByLengthByte();
        this.codeHash = byteBuffer.readByLengthByte();
        //this.argsCount = byteBuffer.readUint8();
        //short length = this.argsCount;
        //this.args = new String[length][];
        //for (short i = 0; i < length; i++) {
        //    short argCount = byteBuffer.readUint8();
        //    if (argCount == 0) {
        //        args[i] = new String[0];
        //    } else {
        //        String[] arg = new String[argCount];
        //        for (short k = 0; k < argCount; k++) {
        //            arg[k] = byteBuffer.readString();
        //        }
        //        args[i] = arg;
        //    }
        //}
    }

    public byte getHard() {
        return hard;
    }

    public byte[] getSender() {
        return sender;
    }

    public void setSender(byte[] sender) {
        this.sender = sender;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(byte[] codeHash) {
        this.codeHash = codeHash;
    }

    //public short getArgsCount() {
    //    return argsCount;
    //}
    //
    //public void setArgsCount(short argsCount) {
    //    this.argsCount = argsCount;
    //}
    //
    //public String[][] getArgs() {
    //    return args;
    //}
    //
    //public void setArgs(String[][] args) {
    //    this.args = args;
    //}
}
