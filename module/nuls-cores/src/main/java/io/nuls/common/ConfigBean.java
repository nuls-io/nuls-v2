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
package io.nuls.common;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Transaction module chain setting
 * @author: Charlie
 * @date: 2019/03/14
 */
public class ConfigBean {

    /** chain id*/
    private int chainId;
    /** assets id*/
    private int assetId;
    /*-------------------------[Block]-----------------------------*/
    /**
     * 区块大小阈值
     */
    private long blockMaxSize;
    /**
     * 网络重置阈值
     */
    private long resetTime;
    /**
     * 分叉链比主链高几个区块就进行链切换
     */
    private byte chainSwtichThreshold;
    /**
     * 分叉链、孤儿链区块最大缓存数量
     */
    private int cacheSize;
    /**
     * 接收新区块的范围
     */
    private int heightRange;
    /**
     * 每次回滚区块最大值
     */
    private int maxRollback;
    /**
     * 一致节点比例
     */
    private byte consistencyNodePercent;
    /**
     * 系统运行最小节点数
     */
    private byte minNodeAmount;
    /**
     * 每次从一个节点下载多少区块
     */
    private byte downloadNumber;
    /**
     * 区块头中扩展字段的最大长度
     */
    private int extendMaxSize;
    /**
     * 为阻止恶意节点提前出块,设置此参数
     * 区块时间戳大于当前时间多少就丢弃该区块
     */
    private int validBlockInterval;
    /**
     * 系统正常运行时最多缓存多少个从别的节点接收到的小区块
     */
    private byte smallBlockCache;
    /**
     * 孤儿链最大年龄
     */
    private byte orphanChainMaxAge;
    /**
     * 日志级别
     */
    private String logLevel;
    /**
     * 下载单个区块的超时时间
     */
    private int singleDownloadTimeout;

    /**
     * 等待网络稳定的时间间隔
     */
    private int waitNetworkInterval;

    /**
     * 创世区块配置文件路径
     */
    private String genesisBlockPath;

    /**
     * 区块同步过程中缓存的区块字节数上限
     */
    private long cachedBlockSizeLimit;
    /*-------------------------[Protocol]-----------------------------*/
    /**
     * 统计区间
     */
    private short interval;
    /**
     * 每个统计区间内的最小生效比例
     */
    private byte effectiveRatioMinimum;
    /**
     * 协议生效要满足的连续区间数最小值
     */
    private short continuousIntervalCountMinimum;
    /*-------------------------[CrossChain]-----------------------------*/
    /**
     * 最小链接数
     * Minimum number of links
     * */
    private int minNodes;

    /**
     * 最大链接数
     * */
    private int maxOutAmount;

    /**
     * 最大被链接数
     * */
    private int maxInAmount;

    /**
     * 跨链交易被打包多少块之后广播给其他链
     * */
    private int sendHeight;

    /**
     * 拜占庭比例
     * */
    private int byzantineRatio;

    /**
     * 最小签名数
     * */
    private int minSignature;

    /**
     * 主网验证人信息
     * */
    private String verifiers;

    /**
     * 主网拜占庭比例
     * */
    private int mainByzantineRatio;

    /**
     * 主网最大签名验证数
     * */
    private int maxSignatureCount;

    /**
     * 主网验证人列表
     * */
    private Set<String> verifierSet = new HashSet<>();

    /*-------------------------[Consensus]-----------------------------*/

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
     * 出块节点密码
     * */
    private String password;

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


    /**
     * 共识奖励资产ID
     * Award asset chain ID
     */
    private int awardAssetId;

    /**
     * 交易手续费单价
     * Transaction fee unit price
     */
    private long feeUnit;

    /**
     * 总通缩量
     * Total inflation amount
     */
    private BigInteger totalInflationAmount;

    /**
     * 初始通胀金额
     * Initial Inflation Amount
     */
    private BigInteger inflationAmount;

    /**
     * 通胀开始时间
     * */
    private long initTime;

    /**
     * 通缩比例
     * */
    private double deflationRatio;

    /**
     * 通缩间隔时间
     * */
    private long deflationTimeInterval;

    /*-------------------------[SmartContract]-----------------------------*/
    /**
     * view方法最大消耗gas
     */
    private long maxViewGas;

    /*-------------------------[Transaction]-----------------------------*/
    /** 单个交易数据最大值(B)*/
    private long txMaxSize;
    /**
     * 打包时在获取交易之后留给模块统一验证的时间阈值,
     * 包括统一验证有被过滤掉的交易时需要重新验证等.
     */
    private int moduleVerifyPercent;
    /** 打包获取交易给RPC传输到共识的预留时间,超时则需要处理交易还原待打包队列*/
    private int packageRpcReserveTime;
    /** 接收网络新交易队列的最大容量 未处理的交易队列**/
    private long txUnverifiedQueueSize;
    /** 孤儿交易生命时间,超过会被清理**/
    private int orphanTtl;
    /*----------------------------------------------------------------------*/

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public long getBlockMaxSize() {
        return blockMaxSize;
    }

    public void setBlockMaxSize(long blockMaxSize) {
        this.blockMaxSize = blockMaxSize;
    }

    public long getResetTime() {
        return resetTime;
    }

    public void setResetTime(long resetTime) {
        this.resetTime = resetTime;
    }

    public byte getChainSwtichThreshold() {
        return chainSwtichThreshold;
    }

    public void setChainSwtichThreshold(byte chainSwtichThreshold) {
        this.chainSwtichThreshold = chainSwtichThreshold;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public int getHeightRange() {
        return heightRange;
    }

    public void setHeightRange(int heightRange) {
        this.heightRange = heightRange;
    }

    public int getMaxRollback() {
        return maxRollback;
    }

    public void setMaxRollback(int maxRollback) {
        this.maxRollback = maxRollback;
    }

    public byte getConsistencyNodePercent() {
        return consistencyNodePercent;
    }

    public void setConsistencyNodePercent(byte consistencyNodePercent) {
        this.consistencyNodePercent = consistencyNodePercent;
    }

    public byte getMinNodeAmount() {
        return minNodeAmount;
    }

    public void setMinNodeAmount(byte minNodeAmount) {
        this.minNodeAmount = minNodeAmount;
    }

    public byte getDownloadNumber() {
        return downloadNumber;
    }

    public void setDownloadNumber(byte downloadNumber) {
        this.downloadNumber = downloadNumber;
    }

    public int getExtendMaxSize() {
        return extendMaxSize;
    }

    public void setExtendMaxSize(int extendMaxSize) {
        this.extendMaxSize = extendMaxSize;
    }

    public int getValidBlockInterval() {
        return validBlockInterval;
    }

    public void setValidBlockInterval(int validBlockInterval) {
        this.validBlockInterval = validBlockInterval;
    }

    public byte getSmallBlockCache() {
        return smallBlockCache;
    }

    public void setSmallBlockCache(byte smallBlockCache) {
        this.smallBlockCache = smallBlockCache;
    }

    public byte getOrphanChainMaxAge() {
        return orphanChainMaxAge;
    }

    public void setOrphanChainMaxAge(byte orphanChainMaxAge) {
        this.orphanChainMaxAge = orphanChainMaxAge;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public int getSingleDownloadTimeout() {
        return singleDownloadTimeout;
    }

    public void setSingleDownloadTimeout(int singleDownloadTimeout) {
        this.singleDownloadTimeout = singleDownloadTimeout;
    }

    public int getWaitNetworkInterval() {
        return waitNetworkInterval;
    }

    public void setWaitNetworkInterval(int waitNetworkInterval) {
        this.waitNetworkInterval = waitNetworkInterval;
    }

    public String getGenesisBlockPath() {
        return genesisBlockPath;
    }

    public void setGenesisBlockPath(String genesisBlockPath) {
        this.genesisBlockPath = genesisBlockPath;
    }

    public long getCachedBlockSizeLimit() {
        return cachedBlockSizeLimit;
    }

    public void setCachedBlockSizeLimit(long cachedBlockSizeLimit) {
        this.cachedBlockSizeLimit = cachedBlockSizeLimit;
    }

    public short getInterval() {
        return interval;
    }

    public void setInterval(short interval) {
        this.interval = interval;
    }

    public byte getEffectiveRatioMinimum() {
        return effectiveRatioMinimum;
    }

    public void setEffectiveRatioMinimum(byte effectiveRatioMinimum) {
        this.effectiveRatioMinimum = effectiveRatioMinimum;
    }

    public short getContinuousIntervalCountMinimum() {
        return continuousIntervalCountMinimum;
    }

    public void setContinuousIntervalCountMinimum(short continuousIntervalCountMinimum) {
        this.continuousIntervalCountMinimum = continuousIntervalCountMinimum;
    }

    public int getMinNodes() {
        return minNodes;
    }

    public void setMinNodes(int minNodes) {
        this.minNodes = minNodes;
    }

    public int getMaxOutAmount() {
        return maxOutAmount;
    }

    public void setMaxOutAmount(int maxOutAmount) {
        this.maxOutAmount = maxOutAmount;
    }

    public int getMaxInAmount() {
        return maxInAmount;
    }

    public void setMaxInAmount(int maxInAmount) {
        this.maxInAmount = maxInAmount;
    }

    public int getSendHeight() {
        return sendHeight;
    }

    public void setSendHeight(int sendHeight) {
        this.sendHeight = sendHeight;
    }

    public int getByzantineRatio() {
        return byzantineRatio;
    }

    public void setByzantineRatio(int byzantineRatio) {
        this.byzantineRatio = byzantineRatio;
    }

    public int getMinSignature() {
        return minSignature;
    }

    public void setMinSignature(int minSignature) {
        this.minSignature = minSignature;
    }

    public String getVerifiers() {
        return verifiers;
    }

    public void setVerifiers(String verifiers) {
        this.verifiers = verifiers;
    }

    public int getMainByzantineRatio() {
        return mainByzantineRatio;
    }

    public void setMainByzantineRatio(int mainByzantineRatio) {
        this.mainByzantineRatio = mainByzantineRatio;
    }

    public int getMaxSignatureCount() {
        return maxSignatureCount;
    }

    public void setMaxSignatureCount(int maxSignatureCount) {
        this.maxSignatureCount = maxSignatureCount;
    }

    public Set<String> getVerifierSet() {
        return verifierSet;
    }

    public void setVerifierSet(Set<String> verifierSet) {
        this.verifierSet = verifierSet;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public int getAwardAssetId() {
        return awardAssetId;
    }

    public void setAwardAssetId(int awardAssetId) {
        this.awardAssetId = awardAssetId;
    }

    public long getFeeUnit() {
        return feeUnit;
    }

    public void setFeeUnit(long feeUnit) {
        this.feeUnit = feeUnit;
    }

    public BigInteger getTotalInflationAmount() {
        return totalInflationAmount;
    }

    public void setTotalInflationAmount(BigInteger totalInflationAmount) {
        this.totalInflationAmount = totalInflationAmount;
    }

    public BigInteger getInflationAmount() {
        return inflationAmount;
    }

    public void setInflationAmount(BigInteger inflationAmount) {
        this.inflationAmount = inflationAmount;
    }

    public long getInitTime() {
        return initTime;
    }

    public void setInitTime(long initTime) {
        this.initTime = initTime;
    }

    public double getDeflationRatio() {
        return deflationRatio;
    }

    public void setDeflationRatio(double deflationRatio) {
        this.deflationRatio = deflationRatio;
    }

    public long getDeflationTimeInterval() {
        return deflationTimeInterval;
    }

    public void setDeflationTimeInterval(long deflationTimeInterval) {
        this.deflationTimeInterval = deflationTimeInterval;
    }

    public long getMaxViewGas() {
        return maxViewGas;
    }

    public void setMaxViewGas(long maxViewGas) {
        this.maxViewGas = maxViewGas;
    }

    public long getTxMaxSize() {
        return txMaxSize;
    }

    public void setTxMaxSize(long txMaxSize) {
        this.txMaxSize = txMaxSize;
    }

    public int getModuleVerifyPercent() {
        return moduleVerifyPercent;
    }

    public void setModuleVerifyPercent(int moduleVerifyPercent) {
        this.moduleVerifyPercent = moduleVerifyPercent;
    }

    public int getPackageRpcReserveTime() {
        return packageRpcReserveTime;
    }

    public void setPackageRpcReserveTime(int packageRpcReserveTime) {
        this.packageRpcReserveTime = packageRpcReserveTime;
    }

    public long getTxUnverifiedQueueSize() {
        return txUnverifiedQueueSize;
    }

    public void setTxUnverifiedQueueSize(long txUnverifiedQueueSize) {
        this.txUnverifiedQueueSize = txUnverifiedQueueSize;
    }

    public int getOrphanTtl() {
        return orphanTtl;
    }

    public void setOrphanTtl(int orphanTtl) {
        this.orphanTtl = orphanTtl;
    }
}
