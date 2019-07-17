/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.deserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.*;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.model.ApiModelProperty;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2019-07-15
 */
public class TxDeserialization {

    @Test
    public void test() throws NulsException, JsonProcessingException {
        String txStr1 = "130037fa2b5d00004600011702000186675865bcb74ff2d599bffce52f7638b712dfcd020001008f380b0000000000000000000000000000000000000000000000000000000000000000000000000000";
        String txStr2 = "130037fa2b5d00004600011702000186675865bcb74ff2d599bffce52f7638b712dfcd0200010097840c0000000000000000000000000000000000000000000000000000000000000000000000000000";

        Transaction tx1 = new Transaction();
        tx1.parse(new NulsByteBuffer(HexUtil.decode(txStr1)));
        CoinData coinData1 = tx1.getCoinDataInstance();

        System.out.println(JSONUtils.obj2PrettyJson(tx1));

        Transaction tx2 = new Transaction();
        tx2.parse(new NulsByteBuffer(HexUtil.decode(txStr2)));
        CoinData coinData2 = tx2.getCoinDataInstance();

        System.out.println(JSONUtils.obj2PrettyJson(tx2));
    }

    @Test
    public void test1() throws Exception {
        String txStr = "150037fa2b5d005700409452a3030000000000000000000000000000000000000000000000000000020002aefee5362dad1a404814709bfe1a9d91e988d6ef5b281974bdc4ac6b0590ea93079e4ef52ceecdb7db37737b32eb5e8b9e60e6618c0117020002aefee5362dad1a404814709bfe1a9d91e988d6ef0200010000409452a30300000000000000000000000000000000000000000000000000000890f46669c10fb6cd000117020002aefee5362dad1a404814709bfe1a9d91e988d6ef0200010000409452a3030000000000000000000000000000000000000000000000000000ffffffffffffffff00";
        Transaction tx = new Transaction();
        tx.parse(new NulsByteBuffer(HexUtil.decode(txStr)));
        CoinData coinData1 = tx.getCoinDataInstance();
        Deposit deposit = new Deposit();
        deposit.parse(new NulsByteBuffer(tx.getTxData()));
        System.out.println(JSONUtils.obj2PrettyJson(deposit));
        System.out.println(JSONUtils.obj2PrettyJson(tx));
    }

    class Deposit extends BaseNulsData {
        @ApiModelProperty(description = "委托金额")
        private BigInteger deposit;
        @ApiModelProperty(description = "委托的节点HASH")
        private NulsHash agentHash;
        @ApiModelProperty(description = "委托账户")
        private byte[] address;
        @ApiModelProperty(description = "委托时间")
        private transient long time;
        @ApiModelProperty(description = "状态")
        private transient int status;
        @ApiModelProperty(description = "委托交易HASH")
        private transient NulsHash txHash;
        @ApiModelProperty(description = "委托交易被打包的高度")
        private transient long blockHeight = -1L;
        @ApiModelProperty(description = "退出委托高度")
        private transient long delHeight = -1L;

        /**
         * serialize important field
         */
        @Override
        protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
            stream.writeBigInteger(deposit);
            stream.write(address);
            stream.write(agentHash.getBytes());

        }

        @Override
        public void parse(NulsByteBuffer byteBuffer) throws NulsException {
            this.deposit = byteBuffer.readBigInteger();
            this.address = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
            this.agentHash = byteBuffer.readHash();
        }

        @Override
        public int size() {
            int size = 0;
            size += SerializeUtils.sizeOfBigInteger();
            size += Address.ADDRESS_LENGTH;
            size += NulsHash.HASH_LENGTH;
            return size;
        }

        public BigInteger getDeposit() {
            return deposit;
        }

        public void setDeposit(BigInteger deposit) {
            this.deposit = deposit;
        }

        public NulsHash getAgentHash() {
            return agentHash;
        }

        public void setAgentHash(NulsHash agentHash) {
            this.agentHash = agentHash;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public NulsHash getTxHash() {
            return txHash;
        }

        public void setTxHash(NulsHash txHash) {
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

        public byte[] getAddress() {
            return address;
        }

        public void setAddress(byte[] address) {
            this.address = address;
        }

        public Set<byte[]> getAddresses() {
            Set<byte[]> addressSet = new HashSet<>();
            addressSet.add(this.address);
            return addressSet;
        }

        @Override
        public Deposit clone() throws CloneNotSupportedException {
            return (Deposit) super.clone();
        }
    }

    class Agent extends BaseNulsData {

        /**
         * 节点地址
         * agent address
         **/
        @ApiModelProperty(description = "节点地址")
        private byte[] agentAddress;

        /**
         * 打包地址
         * packing address
         **/
        @ApiModelProperty(description = "出块地址")
        private byte[] packingAddress;

        /**
         * 奖励地址
         * reward address
         * */
        @ApiModelProperty(description = "奖励地址")
        private byte[] rewardAddress;

        /**
         * 保证金
         * deposit
         * */
        @ApiModelProperty(description = "保证金")
        private BigInteger deposit;

        /**
         * 佣金比例
         * commission rate
         * */
        @ApiModelProperty(description = "佣金比例")
        private byte commissionRate;

        /**
         * 创建时间
         * create time
         **/
        @ApiModelProperty(description = "创建时间")
        private transient long time;

        /**
         * 所在区块高度
         * block height
         * */
        @ApiModelProperty(description = "所在区块高度")
        private transient long blockHeight = -1L;

        /**
         * 该节点注销所在区块高度
         * Block height where the node logs out
         * */
        @ApiModelProperty(description = "节点注销高度")
        private transient long delHeight = -1L;

        /**
         *0:待共识 unConsensus, 1:共识中 consensus
         * */
        @ApiModelProperty(description = "状态，0:待共识 unConsensus, 1:共识中 consensus")
        private transient int status;

        /**
         * 信誉值
         * credit value
         * */
        @ApiModelProperty(description = "信誉值")
        private transient double creditVal;

        /**
         *  总委托金额
         *Total amount entrusted
         * */
        @ApiModelProperty(description = "节点总委托金额")
        private transient BigInteger totalDeposit = BigInteger.ZERO;

        /**
         * 交易HASH
         * transaction hash
         * */
        @ApiModelProperty(description = "创建该节点的交易HASH")
        private transient NulsHash txHash;

        /**
         * 参与共识人数
         * Participation in consensus
         * */
        @ApiModelProperty(description = "参与共识人数")
        private transient int memberCount;

        /**
         *别名不序列化
         * Aliases not serialized
         * */
        @ApiModelProperty(description = "节点别名")
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

        public double getRealCreditVal(){
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
}
