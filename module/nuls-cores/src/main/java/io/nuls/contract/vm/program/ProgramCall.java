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
package io.nuls.contract.vm.program;


import io.nuls.base.basic.AddressTool;
import io.nuls.core.crypto.HexUtil;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class ProgramCall {

    /**
     * 当前块编号
     */
    private long number;

    /**
     * 调用者
     */
    private byte[] sender;
    private byte[] senderPublicKey;

    /**
     * 交易向合约转入的NULS的金额
     */
    private BigInteger value;
    /**
     * 交易向合约转入的其他资产的金额
     */
    private List<ProgramMultyAssetValue> multyAssetValues;

    /**
     * 最大Gas消耗
     */
    private long gasLimit;

    /**
     * 执行合约单价
     */
    private long price;

    /**
     * 合约地址
     */
    private byte[] contractAddress;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法签名，如果方法名不重复，可以不传
     */
    private String methodDesc;

    /**
     * 参数列表
     */
    private String[][] args;

    /**
     * 是否估计Gas
     */
    private boolean estimateGas;

    private boolean viewMethod;

    private boolean internalCall;

    public void args(String... args) {
        setArgs(args);
    }

    public String[][] getArgs() {
        return args;
    }

    public void setArgs(String[][] args) {
        this.args = args;
    }

    public void setArgs(String[] args) {
        this.args = twoDimensionalArray(args);
    }

    public static String[][] twoDimensionalArray(String[] args) {
        if (args == null) {
            return null;
        } else {
            String[][] two = new String[args.length][0];
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg != null) {
                    two[i] = new String[]{arg};
                }
            }
            return two;
        }
    }

    public ProgramCall() {
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public byte[] getSender() {
        return sender;
    }

    public void setSender(byte[] sender) {
        this.sender = sender;
    }

    public byte[] getSenderPublicKey() {
        return senderPublicKey;
    }

    public void setSenderPublicKey(byte[] senderPublicKey) {
        this.senderPublicKey = senderPublicKey;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public boolean isEstimateGas() {
        return estimateGas;
    }

    public void setEstimateGas(boolean estimateGas) {
        this.estimateGas = estimateGas;
    }

    public boolean isViewMethod() {
        return viewMethod;
    }

    public void setViewMethod(boolean viewMethod) {
        this.viewMethod = viewMethod;
    }

    public boolean isInternalCall() {
        return internalCall;
    }

    public void setInternalCall(boolean internalCall) {
        this.internalCall = internalCall;
    }

    public List<ProgramMultyAssetValue> getMultyAssetValues() {
        return multyAssetValues;
    }

    public void setMultyAssetValues(List<ProgramMultyAssetValue> multyAssetValues) {
        this.multyAssetValues = multyAssetValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgramCall that = (ProgramCall) o;

        if (number != that.number) return false;
        if (gasLimit != that.gasLimit) return false;
        if (price != that.price) return false;
        if (estimateGas != that.estimateGas) return false;
        if (viewMethod != that.viewMethod) return false;
        if (internalCall != that.internalCall) return false;
        if (!Arrays.equals(sender, that.sender)) return false;
        if (!Arrays.equals(senderPublicKey, that.senderPublicKey)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (multyAssetValues != null ? !Arrays.deepEquals(multyAssetValues.toArray(), that.multyAssetValues != null ? that.multyAssetValues.toArray() : null) : that.multyAssetValues != null) return false;
        if (!Arrays.equals(contractAddress, that.contractAddress)) return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (methodDesc != null ? !methodDesc.equals(that.methodDesc) : that.methodDesc != null) return false;
        if (!Arrays.deepEquals(args, that.args)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (number ^ (number >>> 32));
        result = 31 * result + Arrays.hashCode(sender);
        result = 31 * result + Arrays.hashCode(senderPublicKey);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (int) (gasLimit ^ (gasLimit >>> 32));
        result = 31 * result + (int) (price ^ (price >>> 32));
        result = 31 * result + Arrays.hashCode(contractAddress);
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (methodDesc != null ? methodDesc.hashCode() : 0);
        result = 31 * result + (multyAssetValues != null ? Arrays.deepHashCode(multyAssetValues.toArray()) : 0);
        result = 31 * result + Arrays.deepHashCode(args);
        result = 31 * result + (estimateGas ? 1 : 0);
        result = 31 * result + (viewMethod ? 1 : 0);
        result = 31 * result + (internalCall ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"number\":")
                .append(number);
        sb.append(",\"sender\":")
                .append(AddressTool.getStringAddressByBytes(sender));
        sb.append(",\"senderPublicKey\":")
                .append(HexUtil.encode(senderPublicKey));
        sb.append(",\"value\":")
                .append(value);
        sb.append(",\"value\":")
                .append(multyAssetValues != null ? Arrays.deepToString(multyAssetValues.toArray()) : "null");
        sb.append(",\"gasLimit\":")
                .append(gasLimit);
        sb.append(",\"price\":")
                .append(price);
        sb.append(",\"contractAddress\":")
                .append(AddressTool.getStringAddressByBytes(contractAddress));
        sb.append(",\"methodName\":")
                .append('\"').append(methodName).append('\"');
        sb.append(",\"methodDesc\":")
                .append('\"').append(methodDesc).append('\"');
        sb.append(",\"args\":")
                .append(Arrays.deepToString(args));
        sb.append(",\"estimateGas\":")
                .append(estimateGas);
        sb.append(",\"viewMethod\":")
                .append(viewMethod);
        sb.append(",\"internalCall\":")
                .append(internalCall);
        sb.append('}');
        return sb.toString();
    }
}
