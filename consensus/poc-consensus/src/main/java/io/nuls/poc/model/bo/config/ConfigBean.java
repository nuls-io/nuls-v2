package io.nuls.poc.model.bo.config;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 共识模块配置类
 * Consensus Module Configuration Class
 *
 * @author tag
 * 2018/11/7
 * */
public class ConfigBean implements Serializable {
    /**
     * 打包间隔时间
     * Packing interval time
     * */
    private long packingInterval;
    /**
     * 区块大小
     * block size
     * */
    private int blockSize;
    /**
     * 出块最小金额
     * Minimum amount of output
     * */
    private BigInteger packingAmount;
    /**
     * 奖励金锁定块数
     * Number of Bonus Locking Blocks
     * */
    private int coinbaseUnlockHeight;
    /**
     * 获得红牌保证金锁定时间
     * Lock-in time to get a red card margin
     * */
    private long redPublishLockTime;
    /**
     * 注销节点保证金锁定时间
     * Log-off node margin locking time
     * */
    private long stopAgentLockTime;
    /**
     * 佣金比例的最小值
     * Minimum commission ratio
     * */
    private double commissionRateMin;
    /**
     * 佣金比例的最大值
     * Maximum commission ratio
     * */
    private double commissionRateMax;
    /**
     * 创建节点的保证金最小值
     * Minimum margin for creating nodes
     * */
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
     * 节点最多能被多少人委托
     * How many people can a node be delegated at most
     */
    private int depositNumberMax;
    /**
     * 种子节点
     * Seed node
     */
    private String seedNodes;

    /**
     * 资产ID
     * assets id
     */
    private int assetsId;

    /**
     * chain id
     */
    private int chainId;

    public long getPackingInterval() {
        return packingInterval;
    }

    public void setPackingInterval(long packingInterval) {
        this.packingInterval = packingInterval;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public BigInteger getPackingAmount() {
        return packingAmount;
    }

    public void setPackingAmount(BigInteger packingAmount) {
        this.packingAmount = packingAmount;
    }

    public int getCoinbaseUnlockHeight() {
        return coinbaseUnlockHeight;
    }

    public void setCoinbaseUnlockHeight(int coinbaseUnlockHeight) {
        this.coinbaseUnlockHeight = coinbaseUnlockHeight;
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

    public double getCommissionRateMin() {
        return commissionRateMin;
    }

    public void setCommissionRateMin(double commissionRateMin) {
        this.commissionRateMin = commissionRateMin;
    }

    public double getCommissionRateMax() {
        return commissionRateMax;
    }

    public void setCommissionRateMax(double commissionRateMax) {
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

    public int getDepositNumberMax() {
        return depositNumberMax;
    }

    public void setDepositNumberMax(int depositNumberMax) {
        this.depositNumberMax = depositNumberMax;
    }

    public String getSeedNodes() {
        return seedNodes;
    }

    public void setSeedNodes(String seedNodes) {
        this.seedNodes = seedNodes;
    }

    public int getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(int assetsId) {
        this.assetsId = assetsId;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }
}
