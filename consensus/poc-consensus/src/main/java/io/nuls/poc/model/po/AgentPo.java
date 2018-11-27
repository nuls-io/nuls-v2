/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
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
 * @author ln
 */
public class AgentPo extends BaseNulsData {

    private transient NulsDigestData hash;

    private byte[] agentAddress;

    private byte[] packingAddress;

    private byte[] rewardAddress;

    private String deposit;

    private double commissionRate;

    private long time;

    private long blockHeight = -1L;

    private long delHeight = -1L;

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(hash);
        stream.write(agentAddress);
        stream.write(packingAddress);
        stream.write(rewardAddress);
        stream.writeString(deposit);
        stream.writeDouble(commissionRate);
        stream.writeUint48(time);
        stream.writeVarInt(blockHeight);
        stream.writeVarInt(delHeight);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.hash = byteBuffer.readHash();
        this.agentAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.packingAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.rewardAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.deposit = byteBuffer.readString();
        this.commissionRate = byteBuffer.readDouble();
        this.time = byteBuffer.readUint48();
        this.blockHeight = byteBuffer.readVarInt();
        this.delHeight = byteBuffer.readVarInt();
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfNulsData(hash);
        size += Address.ADDRESS_LENGTH * 3;
        size += SerializeUtils.sizeOfString(deposit);
        size += SerializeUtils.sizeOfDouble(commissionRate);
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfVarInt(blockHeight);
        size += SerializeUtils.sizeOfVarInt(delHeight);
        return size;
    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public byte[] getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(byte[] agentAddress) {
        this.agentAddress = agentAddress;
    }

    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public byte[] getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(byte[] rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String  deposit) {
        this.deposit = deposit;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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