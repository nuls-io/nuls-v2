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
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: PierreLuo
 */
public class ContractTransferData extends BaseNulsData implements ContractData {

    private NulsHash orginTxHash;
    private byte[] contractAddress;

    public ContractTransferData() {
    }

    public ContractTransferData(NulsHash orginTxHash, byte[] contractAddress) {
        this.orginTxHash = orginTxHash;
        this.contractAddress = contractAddress;
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        size += Address.ADDRESS_LENGTH;
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(orginTxHash.getBytes());
        stream.write(contractAddress);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.orginTxHash = byteBuffer.readHash();
        this.contractAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
    }


    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        addressSet.add(contractAddress);
        return addressSet;
    }

    @Override
    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public NulsHash getOrginTxHash() {
        return orginTxHash;
    }

    public void setOrginTxHash(NulsHash orginTxHash) {
        this.orginTxHash = orginTxHash;
    }

    @Override
    public long getGasLimit() {
        return 0L;
    }

    @Override
    public byte[] getSender() {
        return null;
    }

    @Override
    public byte[] getCode() {
        return null;
    }

    @Override
    public long getPrice() {
        return 0L;
    }

    @Override
    public BigInteger getValue() {
        return BigInteger.ZERO;
    }

    @Override
    public String getMethodName() {
        return null;
    }

    @Override
    public String getMethodDesc() {
        return null;
    }

    @Override
    public String[][] getArgs() {
        return new String[0][];
    }


}
