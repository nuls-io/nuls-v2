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

package io.nuls.poc.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import java.io.IOException;

/**
 * @author: Niels Wang
 */
public class DepositPo extends BaseNulsData {


    private NulsDigestData txHash;
    private String deposit;
    private NulsDigestData agentHash;
    private byte[] address;
    private long time;
    private long blockHeight = -1L;
    private long delHeight = -1L;

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(deposit);
        stream.writeNulsData(agentHash);
        stream.write(address);
        stream.writeUint48(time);
        stream.writeNulsData(txHash);
        stream.writeVarInt(blockHeight);
        stream.writeVarInt(delHeight);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.deposit = byteBuffer.readString();
        this.agentHash = byteBuffer.readHash();
        this.address = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.time = byteBuffer.readUint48();
        this.txHash = byteBuffer.readHash();
        this.blockHeight = byteBuffer.readVarInt();
        this.delHeight = byteBuffer.readVarInt();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(deposit); // deposit.getValue()
        size += SerializeUtils.sizeOfNulsData(agentHash);
        size += address.length;
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfNulsData(txHash);
        size += SerializeUtils.sizeOfVarInt(blockHeight);  // blockHeight
        size += SerializeUtils.sizeOfVarInt(delHeight);  // delHeight
        return size;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public NulsDigestData getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(NulsDigestData agentHash) {
        this.agentHash = agentHash;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public NulsDigestData getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(long delHeight) {
        this.delHeight = delHeight;
    }
}
