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
package io.nuls.contract.vm.program.impl;

import io.nuls.contract.vm.program.ProgramMultyAssetValue;

import java.math.BigInteger;
import java.util.List;

public class ProgramInvoke {

    /**
     * Contract address
     * When creating a contract, it is necessary to pass in the generated new address
     */
    private byte[] contractAddress;

    private String address;

    /**
     * Transaction initiator address
     */
    private byte[] sender;
    private byte[] senderPublicKey;

    /**
     * Transaction initiator configuredgasprice
     */
    private long price;

    /**
     * Provided by the initiator of the transactiongas
     */
    private long gasLimit;

    /**
     * Transactions transferred to contractsNULSmoney
     */
    private BigInteger value;

    /**
     * The amount of other assets transferred from transactions to contracts
     */
    private List<ProgramMultyAssetValue> multyAssetValues;

    /**
     * Current block number
     */
    private long number;

    /**
     * When creating a contract, pass in the code
     */
    private byte[] data;

    /**
     * Call method name
     */
    private String methodName;

    /**
     * Calling method signature
     */
    private String methodDesc;

    /**
     * Calling method parameters
     */
    private String[][] args;

    /**
     * Is it estimatedGas
     */
    private boolean estimateGas;

    private boolean viewMethod;

    private boolean create;

    private boolean internalCall;
    private boolean internalCreate;

    public boolean isInternalCreate() {
        return internalCreate;
    }

    public void setInternalCreate(boolean internalCreate) {
        this.internalCreate = internalCreate;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public List<ProgramMultyAssetValue> getMultyAssetValues() {
        return multyAssetValues;
    }

    public void setMultyAssetValues(List<ProgramMultyAssetValue> multyAssetValues) {
        this.multyAssetValues = multyAssetValues;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
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

    public String[][] getArgs() {
        return args;
    }

    public void setArgs(String[][] args) {
        this.args = args;
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

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isInternalCall() {
        return internalCall;
    }

    public void setInternalCall(boolean internalCall) {
        this.internalCall = internalCall;
    }

}
