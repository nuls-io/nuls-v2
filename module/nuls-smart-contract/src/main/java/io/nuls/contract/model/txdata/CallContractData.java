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
package io.nuls.contract.model.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.contract.vm.program.ProgramMultyAssetValue;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * @Author: PierreLuo
 */
public class CallContractData extends BaseNulsData implements ContractData {

    private byte[] sender;
    private byte[] contractAddress;
    private BigInteger value;
    private long gasLimit;
    private long price;
    private String methodName;
    private String methodDesc;
    private short argsCount;
    private String[][] args;
    private transient List<ProgramMultyAssetValue> multyAssetValues;

    @Override
    public int size() {
        int size = 0;
        size += Address.ADDRESS_LENGTH;
        size += Address.ADDRESS_LENGTH;
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();

        size += SerializeUtils.sizeOfString(methodName);
        size += SerializeUtils.sizeOfString(methodDesc);
        size += 1;
        if (args != null) {
            for (String[] arg : args) {
                if (arg == null) {
                    size += 1;
                } else {
                    size += 1;
                    for (String str : arg) {
                        size += SerializeUtils.sizeOfString(str);
                    }
                }
            }
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(sender);
        stream.write(contractAddress);
        stream.writeBigInteger(value);
        stream.writeInt64(gasLimit);
        stream.writeInt64(price);

        stream.writeString(methodName);
        stream.writeString(methodDesc);
        stream.writeUint8(argsCount);
        if (args != null) {
            for (String[] arg : args) {
                if (arg == null) {
                    stream.writeUint8((short) 0);
                } else {
                    stream.writeUint8((short) arg.length);
                    for (String str : arg) {
                        stream.writeString(str);
                    }
                }
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.sender = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.contractAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.value = byteBuffer.readBigInteger();
        this.gasLimit = byteBuffer.readInt64();
        this.price = byteBuffer.readInt64();

        this.methodName = byteBuffer.readString();
        this.methodDesc = byteBuffer.readString();
        this.argsCount = byteBuffer.readUint8();
        short length = this.argsCount;
        this.args = new String[length][];
        for (short i = 0; i < length; i++) {
            short argCount = byteBuffer.readUint8();
            if (argCount == 0) {
                args[i] = new String[0];
            } else {
                String[] arg = new String[argCount];
                for (short k = 0; k < argCount; k++) {
                    arg[k] = byteBuffer.readString();
                }
                args[i] = arg;
            }
        }
    }

    @Override
    public byte[] getSender() {
        return sender;
    }

    @Override
    public byte[] getCode() {
        return null;
    }

    public void setSender(byte[] sender) {
        this.sender = sender;
    }

    @Override
    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    @Override
    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    @Override
    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    @Override
    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public short getArgsCount() {
        return argsCount;
    }

    public void setArgsCount(short argsCount) {
        this.argsCount = argsCount;
    }

    @Override
    public String[][] getArgs() {
        return args;
    }

    public void setArgs(String[][] args) {
        this.args = args;
    }

    public void setAssetId(){}

    public List<ProgramMultyAssetValue> getMultyAssetValues() {
        return multyAssetValues;
    }

    public void setMultyAssetValues(List<ProgramMultyAssetValue> multyAssetValues) {
        this.multyAssetValues = multyAssetValues;
    }
}
