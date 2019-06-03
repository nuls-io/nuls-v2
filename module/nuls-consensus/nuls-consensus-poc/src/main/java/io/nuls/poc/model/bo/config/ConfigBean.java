package io.nuls.poc.model.bo.config;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * 共识模块配置类
 * Consensus Module Configuration Class
 *
 * @author tag
 * 2018/11/7
 */
public class ConfigBean extends BaseNulsData {
    /**
     * 本链资产ID
     * assets id
     */
    private int assetId;

    /**
     * 本链链ID
     * chain id
     */
    private int chainId;

    /**
     * 打包间隔时间
     * Packing interval time
     */
    private long packingInterval;
    /**
     * 获得红牌保证金锁定时间
     * Lock-in time to get a red card margin
     */
    private long redPublishLockTime;
    /**
     * 注销节点保证金锁定时间
     * Log-off node margin locking time
     */
    private long stopAgentLockTime;
    /**
     * 佣金比例的最小值
     * Minimum commission ratio
     */
    private byte commissionRateMin;
    /**
     * 佣金比例的最大值
     * Maximum commission ratio
     */
    private byte commissionRateMax;
    /**
     * 创建节点的保证金最小值
     * Minimum margin for creating nodes
     */
    private BigInteger depositMin;
    /**
     * 创建节点的保证金最大值
     * Maximum margin for creating nodes
     */
    private BigInteger depositMax;
    /**
     * 节点出块委托金额最小值
     * Minimum Delegation Amount of Node Block
     */
    private BigInteger commissionMin;
    /**
     * 节点委托金额最大值
     * Maximum Node Delegation Amount
     */
    private BigInteger commissionMax;

    /**
     * 委托最小金额
     * Minimum amount entrusted
     */
    private BigInteger entrusterDepositMin;

    /**
     * 种子节点
     * Seed node
     */
    private String seedNodes;

    /**
     * 节点委托金额最大值
     * Maximum Node Delegation Amount
     */
    private BigInteger inflationAmount;

    /**
     * 出块节点密码
     * */
    private String password;

    /**
     * 打包区块最大值
     * */
    private long blockMaxSize;

    /**
     * 打包一个区块获得的共识奖励
     * 每年通胀/每年出块数
     * */
    private BigInteger blockReward;


    /**
     * 创建节点资产ID
     * agent assets id
     */
    private int agentAssetId;

    /**
     * 创建节点资产链ID
     * Create node asset chain ID
     */
    private int agentChainId;



    public long getPackingInterval() {
        return packingInterval;
    }

    public void setPackingInterval(long packingInterval) {
        this.packingInterval = packingInterval;
    }


    public long getRedPublishLockTime() {
        return redPublishLockTime;
    }

    public void setRedPublishLockTime(long redPublishLockTime) {
        this.redPublishLockTime = redPublishLockTime;
    }

    public long getStopAgentLockTime() {
        return stopAgentLockTime;
    }

    public void setStopAgentLockTime(long stopAgentLockTime) {
        this.stopAgentLockTime = stopAgentLockTime;
    }

    public byte getCommissionRateMin() {
        return commissionRateMin;
    }

    public void setCommissionRateMin(byte commissionRateMin) {
        this.commissionRateMin = commissionRateMin;
    }

    public byte getCommissionRateMax() {
        return commissionRateMax;
    }

    public void setCommissionRateMax(byte commissionRateMax) {
        this.commissionRateMax = commissionRateMax;
    }

    public BigInteger getDepositMin() {
        return depositMin;
    }

    public void setDepositMin(BigInteger depositMin) {
        this.depositMin = depositMin;
    }

    public BigInteger getDepositMax() {
        return depositMax;
    }

    public void setDepositMax(BigInteger depositMax) {
        this.depositMax = depositMax;
    }

    public BigInteger getCommissionMin() {
        return commissionMin;
    }

    public void setCommissionMin(BigInteger commissionMin) {
        this.commissionMin = commissionMin;
    }

    public BigInteger getCommissionMax() {
        return commissionMax;
    }

    public void setCommissionMax(BigInteger commissionMax) {
        this.commissionMax = commissionMax;
    }

    public BigInteger getEntrusterDepositMin() {
        return entrusterDepositMin;
    }

    public void setEntrusterDepositMin(BigInteger entrusterDepositMin) {
        this.entrusterDepositMin = entrusterDepositMin;
    }

    public String getSeedNodes() {
        return seedNodes;
    }

    public void setSeedNodes(String seedNodes) {
        this.seedNodes = seedNodes;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public BigInteger getInflationAmount() {
        return inflationAmount;
    }

    public void setInflationAmount(BigInteger inflationAmount) {
        this.inflationAmount = inflationAmount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getBlockMaxSize() {
        return blockMaxSize;
    }

    public void setBlockMaxSize(long blockMaxSize) {
        this.blockMaxSize = blockMaxSize;
    }

    public BigInteger getBlockReward() {
        return blockReward;
    }

    public void setBlockReward(BigInteger blockReward) {
        this.blockReward = blockReward;
    }

    public int getAgentAssetId() {
        return agentAssetId;
    }

    public void setAgentAssetId(int agentAssetId) {
        this.agentAssetId = agentAssetId;
    }

    public int getAgentChainId() {
        return agentChainId;
    }

    public void setAgentChainId(int agentChainId) {
        this.agentChainId = agentChainId;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(packingInterval);
        stream.writeUint32(redPublishLockTime);
        stream.writeUint32(stopAgentLockTime);
        stream.writeByte(commissionRateMin);
        stream.writeByte(commissionRateMax);
        stream.writeBigInteger(depositMin);
        stream.writeBigInteger(depositMax);
        stream.writeBigInteger(commissionMin);
        stream.writeBigInteger(commissionMax);
        stream.writeBigInteger(entrusterDepositMin);
        stream.writeString(seedNodes);
        stream.writeUint16(assetId);
        stream.writeUint16(chainId);
        stream.writeBigInteger(inflationAmount);
        stream.writeString(password);
        stream.writeUint48(blockMaxSize);
        stream.writeBigInteger(blockReward);
        stream.writeUint16(agentAssetId);
        stream.writeUint16(agentChainId);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.packingInterval = byteBuffer.readUint32();
        this.redPublishLockTime = byteBuffer.readUint32();
        this.stopAgentLockTime = byteBuffer.readUint32();
        this.commissionRateMin = byteBuffer.readByte();
        this.commissionRateMax = byteBuffer.readByte();
        this.depositMin = byteBuffer.readBigInteger();
        this.depositMax = byteBuffer.readBigInteger();
        this.commissionMin = byteBuffer.readBigInteger();
        this.commissionMax = byteBuffer.readBigInteger();
        this.entrusterDepositMin = byteBuffer.readBigInteger();
        this.seedNodes = byteBuffer.readString();
        this.assetId = byteBuffer.readUint16();
        this.chainId = byteBuffer.readUint16();
        this.inflationAmount = byteBuffer.readBigInteger();
        this.password = byteBuffer.readString();
        this.blockMaxSize = byteBuffer.readUint48();
        this.blockReward = byteBuffer.readBigInteger();
        this.agentAssetId = byteBuffer.readUint16();
        this.agentChainId = byteBuffer.readUint16();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfUint32() * 3;
        size += 2;
        size += SerializeUtils.sizeOfBigInteger() * 7;
        size += SerializeUtils.sizeOfString(seedNodes);
        size += SerializeUtils.sizeOfUint16() * 4;
        size += SerializeUtils.sizeOfString(password);
        return size;
    }
}
