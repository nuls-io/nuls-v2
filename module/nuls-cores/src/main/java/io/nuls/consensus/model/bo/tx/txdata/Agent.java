/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.consensus.model.bo.tx.txdata;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsHash;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Node information class
 * Node information class
 *
 * @author tag
 * 2018/11/6
 */
@ApiModel(name = "Node information")
public class Agent extends BaseNulsData {

    /**
     * Node address
     * agent address
     **/
    @ApiModelProperty(description = "Node address")
    private byte[] agentAddress;

    /**
     * Packaging address
     * packing address
     **/
    @ApiModelProperty(description = "Block address")
    private byte[] packingAddress;

    /**
     * Reward Address
     * reward address
     */
    @ApiModelProperty(description = "Reward Address")
    private byte[] rewardAddress;

    /**
     * Margin
     * deposit
     */
    @ApiModelProperty(description = "Margin")
    private BigInteger deposit;

    /**
     * commission rate
     * commission rate
     */
    @ApiModelProperty(description = "commission rate")
    private byte commissionRate;

    /**
     * Creation time
     * create time
     **/
    @ApiModelProperty(description = "Creation time")
    private transient long time;

    /**
     * Block height
     * block height
     */
    @ApiModelProperty(description = "Block height")
    private transient long blockHeight = -1L;

    /**
     * The height of the block where the node is deregistered is located
     * Block height where the node logs out
     */
    @ApiModelProperty(description = "Node deregistration height")
    private transient long delHeight = -1L;

    /**
     * 0:Pending consensus unConsensus, 1:In consensus consensus
     */
    @ApiModelProperty(description = "Status,0:Pending consensus unConsensus, 1:In consensus consensus")
    private transient int status;

    /**
     * Reputation value
     * credit value
     */
    @ApiModelProperty(description = "Reputation value")
    private transient double creditVal;

    /**
     * Total entrusted amount
     * Total amount entrusted
     */
    @ApiModelProperty(description = "Total entrusted amount of nodes")
    private transient BigInteger totalDeposit = BigInteger.ZERO;

    /**
     * Total commission amount, used for page display（Due to2.4.1Smart contractsBUGCausing temporary addition of fields, which need to be deleted in subsequent versions）
     */
    private transient BigInteger reTotalDeposit = BigInteger.ZERO;

    /**
     * transactionHASH
     * transaction hash
     */
    @ApiModelProperty(description = "Create transactions for this nodeHASH")
    private transient NulsHash txHash;

    /**
     * Number of participants in consensus
     * Participation in consensus
     */
    @ApiModelProperty(description = "Number of participants in consensus")
    private transient int memberCount;

    /**
     * Aliases are not serialized
     * Aliases not serialized
     */
    @ApiModelProperty(description = "net aliases")
    private transient String alais;

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBigInteger();
        size += this.agentAddress.length;
        size += this.rewardAddress.length;
        size += this.packingAddress.length;
        size += 1;
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBigInteger(deposit);
        stream.write(agentAddress);
        stream.write(packingAddress);
        stream.write(rewardAddress);
        stream.write(this.commissionRate);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.deposit = byteBuffer.readBigInteger();
        this.agentAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.packingAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.rewardAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.commissionRate = byteBuffer.readByte();
    }


    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(byte commissionRate) {
        this.commissionRate = commissionRate;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public double getCreditVal() {
        return creditVal < 0d ? 0D : this.creditVal;
    }

    public double getRealCreditVal() {
        return this.creditVal;
    }

    public void setTxHash(NulsHash txHash) {
        this.txHash = txHash;
    }

    public NulsHash getTxHash() {
        return txHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(long delHeight) {
        this.delHeight = delHeight;
    }

    public byte[] getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(byte[] agentAddress) {
        this.agentAddress = agentAddress;
    }

    public byte[] getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(byte[] rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public BigInteger getReTotalDeposit() {
        return reTotalDeposit;
    }

    public void setReTotalDeposit(BigInteger reTotalDeposit) {
        this.reTotalDeposit = reTotalDeposit;
    }

    /**
     * Even if there is a remaining commission amount for the node
     * Even if the remaining amount of the node can be delegated
     **/
    public BigInteger getAvailableDepositAmount(Chain chain) {
        BigInteger commissionMax = chain.getConfig().getCommissionMax();
        if (ProtocolGroupManager.getCurrentVersion(chain.getConfig().getChainId()) >= 23) {
            commissionMax = chain.getConfig().getCommissionMaxV23();
        }
        return commissionMax.subtract(this.getTotalDeposit());
    }

    /**
     * Determine whether the node can be delegated
     * Determine whether the node can be delegated
     */
    public boolean canDeposit(Chain chain) {
        int flag = getAvailableDepositAmount(chain).compareTo(chain.getConfig().getCommissionMin());
        if (flag >= 1) {
            return true;
        }
        return false;
    }

    public BigInteger getDeposit() {
        return deposit;
    }

    public void setDeposit(BigInteger deposit) {
        this.deposit = deposit;
    }

    public BigInteger getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(BigInteger totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    @Override
    public Agent clone() throws CloneNotSupportedException {
        return (Agent) super.clone();
    }

    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        addressSet.add(this.agentAddress);
        return addressSet;
    }

    public String getAlais() {
        return alais;
    }

    public void setAlais(String alais) {
        this.alais = alais;
    }
}
