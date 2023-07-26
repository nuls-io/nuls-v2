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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Transaction module chain setting
 * @author: Charlie
 * @date: 2019/03/14
 */
public class ConfigBean extends BaseNulsData {

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
     * 减少保证金锁定时间
     * Reduce margin lock-in time
     */
    private long reducedDepositLockTime;

    /**
     * 创建节点的保证金最小值
     * Minimum margin for creating nodes
     */
    private BigInteger depositMin;

    /**
     * 节点的保证金最大值
     * Maximum margin for creating nodes
     */
    private BigInteger depositMax;

    /**
     * 节点参与共识节点竞选最小委托金
     * Minimum Trust Fund for node participating in consensus node campaign
     */
    private BigInteger packDepositMin;

    /**
     * 委托最小金额
     * Minimum amount entrusted
     */
    private BigInteger entrustMin;

    /**
     * 种子节点
     * Seed node
     */
    private String seedNodes;

    /**
     * 种子节点对应公钥
     */
    private String pubKeyList;

    /**
     * 出块节点密码
     */
    private String password;

    /**
     * 打包区块最大值
     */
    private long blockConsensusMaxSize;

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
     */
    private long initHeight;

    /**
     * 通缩比例
     */
    private double deflationRatio;

    /**
     * 通缩间隔时间
     */
    private long deflationHeightInterval;
    /**
     * 追加保证金最小金额
     * Minimum amount of additional margin
     */
    private BigInteger appendAgentDepositMin;

    /**
     * 退出保证金最小金额
     * Minimum amount of withdrawal deposit
     */
    private BigInteger reduceAgentDepositMin;

    private int byzantineRate;
    /**
     * 共识节点最大数量
     */
    private int agentCountMax;

    /**
     * 本链主资产的权重基数
     */
    private double localAssertBase;

    /**
     * 节点保证金基数
     */
    private double agentDepositBase;
    /**
     * 虚拟银行保证金基数
     */
    private double superAgentDepositBase;
    /**
     * 后备节点保证金基数
     */
    private double reservegentDepositBase;


    private int maxCoinToOfCoinbase;
    private long minRewardHeight;
    private long depositAwardChangeHeight;
    private long depositVerifyHeight;
    private Long v1_3_0Height;
    private Long v1_6_0Height;
    private Long v1_7_0Height;
    private BigInteger minStakingAmount;
    private BigInteger minAppendAndExitAmount;
    private Integer exitStakingLockHours;


    public Map<String, Integer> weightMap = new HashMap<>();

    public void putWeight(int chainId, int assetId, int weight) {
        weightMap.put(chainId + "-" + assetId, weight);
    }

    public int getWeight(int chainId, int assetId) {
        Integer weight = weightMap.get(chainId + "-" + assetId);
        if (null == weight) {
            return 1;
        }
        return weight.intValue();
    }

    public long getDepositAwardChangeHeight() {
        return depositAwardChangeHeight;
    }

    public void setDepositAwardChangeHeight(long depositAwardChangeHeight) {
        this.depositAwardChangeHeight = depositAwardChangeHeight;
    }

    public long getDepositVerifyHeight() {
        return depositVerifyHeight;
    }

    public void setDepositVerifyHeight(long depositVerifyHeight) {
        this.depositVerifyHeight = depositVerifyHeight;
    }

    public int getMaxCoinToOfCoinbase() {
        return maxCoinToOfCoinbase;
    }

    public void setMaxCoinToOfCoinbase(int maxCoinToOfCoinbase) {
        this.maxCoinToOfCoinbase = maxCoinToOfCoinbase;
    }

    public long getMinRewardHeight() {
        return minRewardHeight;
    }

    public void setMinRewardHeight(long minRewardHeight) {
        this.minRewardHeight = minRewardHeight;
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

    public String getSeedNodes() {
        //不再配置种子节点地址，而是从公钥计算得到
        if (StringUtils.isBlank(seedNodes)) {
            String[] pubkeys = this.pubKeyList.split(",");
            StringBuilder ss = new StringBuilder("");
            for (String pub : pubkeys) {
                ss.append(",").append(AddressTool.getAddressString(HexUtil.decode(pub), this.chainId));
            }
            this.seedNodes = ss.toString().substring(1);
        }
        return seedNodes;
    }

    public String getPubKeyList() {
        return pubKeyList;
    }

    public void setPubKeyList(String pubKeyList) {
        this.pubKeyList = pubKeyList;
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

    public long getBlockConsensusMaxSize() {
        return blockConsensusMaxSize;
    }

    public void setBlockConsensusMaxSize(long blockConsensusMaxSize) {
        this.blockConsensusMaxSize = blockConsensusMaxSize;
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

    public long getInitHeight() {
        return initHeight;
    }

    public void setInitHeight(long initHeight) {
        this.initHeight = initHeight;
    }

    public long getDeflationHeightInterval() {
        return deflationHeightInterval;
    }

    public void setDeflationHeightInterval(long deflationHeightInterval) {
        this.deflationHeightInterval = deflationHeightInterval;
    }

    public double getAgentDepositBase() {
        return agentDepositBase;
    }

    public void setAgentDepositBase(double agentDepositBase) {
        this.agentDepositBase = agentDepositBase;
    }

    public double getSuperAgentDepositBase() {
        return superAgentDepositBase;
    }

    public void setSuperAgentDepositBase(double superAgentDepositBase) {
        this.superAgentDepositBase = superAgentDepositBase;
    }

    public double getDeflationRatio() {
        return deflationRatio;
    }

    public void setDeflationRatio(double deflationRatio) {
        this.deflationRatio = deflationRatio;
    }

    public BigInteger getTotalInflationAmount() {
        return totalInflationAmount;
    }

    public void setTotalInflationAmount(BigInteger totalInflationAmount) {
        this.totalInflationAmount = totalInflationAmount;
    }

    public BigInteger getPackDepositMin() {
        return packDepositMin;
    }

    public void setPackDepositMin(BigInteger packDepositMin) {
        this.packDepositMin = packDepositMin;
    }


    public long getReducedDepositLockTime() {
        return reducedDepositLockTime;
    }

    public void setReducedDepositLockTime(long reducedDepositLockTime) {
        this.reducedDepositLockTime = reducedDepositLockTime;
    }

    public BigInteger getEntrustMin() {
        return entrustMin;
    }

    public void setEntrustMin(BigInteger entrustMin) {
        this.entrustMin = entrustMin;
    }

    public BigInteger getAppendAgentDepositMin() {
        return appendAgentDepositMin;
    }

    public void setAppendAgentDepositMin(BigInteger appendAgentDepositMin) {
        this.appendAgentDepositMin = appendAgentDepositMin;
    }

    public BigInteger getReduceAgentDepositMin() {
        return reduceAgentDepositMin;
    }

    public void setReduceAgentDepositMin(BigInteger reduceAgentDepositMin) {
        this.reduceAgentDepositMin = reduceAgentDepositMin;
    }

    public int getByzantineRate() {
        return byzantineRate;
    }

    public void setByzantineRate(int byzantineRate) {
        this.byzantineRate = byzantineRate;
    }

    public int getAgentCountMax() {
        return agentCountMax;
    }

    public void setAgentCountMax(int agentCountMax) {
        this.agentCountMax = agentCountMax;
    }

    public double getLocalAssertBase() {
        return localAssertBase;
    }

    public void setLocalAssertBase(double localAssertBase) {
        this.localAssertBase = localAssertBase;
    }

    public long getPackingIntervalMills() {
        return 1000 * this.getPackingInterval();
    }

    public double getReservegentDepositBase() {
        return reservegentDepositBase;
    }

    public void setReservegentDepositBase(double reservegentDepositBase) {
        this.reservegentDepositBase = reservegentDepositBase;
    }

    public void setV130Height(Long v130Height) {
        this.v1_3_0Height = v130Height;
    }

    public Long getV130Height() {
        return v1_3_0Height;
    }

    public Long getV1_7_0Height() {
        return v1_7_0Height;
    }

    public void setV1_7_0Height(Long v1_7_0Height) {
        this.v1_7_0Height = v1_7_0Height;
    }

    public Long getV1_6_0Height() {
        return v1_6_0Height;
    }

    public void setV1_6_0Height(Long v1_6_0Height) {
        this.v1_6_0Height = v1_6_0Height;
    }

    public void setMinStakingAmount(BigInteger minStakingAmount) {
        this.minStakingAmount = minStakingAmount;
    }

    public BigInteger getMinStakingAmount() {
        return minStakingAmount;
    }

    public void setMinAppendAndExitAmount(BigInteger minAppendAndExitAmount) {
        this.minAppendAndExitAmount = minAppendAndExitAmount;
    }

    public BigInteger getMinAppendAndExitAmount() {
        return minAppendAndExitAmount;
    }

    public void setExitStakingLockHours(Integer exitStakingLockHours) {
        this.exitStakingLockHours = exitStakingLockHours;
    }

    public Integer getExitStakingLockHours() {
        return exitStakingLockHours;
    }


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // block
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeUint32(blockMaxSize);
        stream.writeUint32(resetTime);
        stream.writeByte(chainSwtichThreshold);
        stream.writeUint16(cacheSize);
        stream.writeUint16(heightRange);
        stream.writeUint16(maxRollback);
        stream.writeByte(consistencyNodePercent);
        stream.writeByte(minNodeAmount);
        stream.writeByte(downloadNumber);
        stream.writeUint16(extendMaxSize);
        stream.writeUint16(validBlockInterval);
        stream.writeByte(smallBlockCache);
        stream.writeByte(orphanChainMaxAge);
        stream.writeString(logLevel);
        stream.writeUint16(singleDownloadTimeout);
        stream.writeUint16(waitNetworkInterval);
        stream.writeString(genesisBlockPath);
        stream.writeUint32(cachedBlockSizeLimit);
        // protocol
        stream.writeShort(interval);
        stream.writeByte(effectiveRatioMinimum);
        stream.writeShort(continuousIntervalCountMinimum);
        // cross
        stream.writeUint16(minNodes);
        stream.writeUint16(maxOutAmount);
        stream.writeUint16(maxInAmount);
        stream.writeUint16(sendHeight);
        stream.writeUint16(byzantineRatio);
        stream.writeUint16(minSignature);
        stream.writeString(verifiers);
        stream.writeUint16(mainByzantineRatio);
        stream.writeUint16(maxSignatureCount);
        int registerCount = verifierSet == null ? 0 : verifierSet.size();
        stream.writeVarInt(registerCount);
        if(verifierSet != null){
            for (String registerAgent:verifierSet) {
                stream.writeString(registerAgent);
            }
        }
        // consensus
        stream.writeUint32(packingInterval);
        stream.writeUint32(redPublishLockTime);
        stream.writeUint32(stopAgentLockTime);
        stream.writeUint32(reducedDepositLockTime);
        stream.writeBigInteger(depositMin);
        stream.writeBigInteger(depositMax);
        stream.writeBigInteger(packDepositMin);
        stream.writeBigInteger(entrustMin);
        stream.writeString(seedNodes);
        stream.writeString(pubKeyList);
        stream.writeString(password);
        stream.writeUint48(blockConsensusMaxSize);
        stream.writeUint16(agentAssetId);
        stream.writeUint16(agentChainId);
        stream.writeUint16(awardAssetId);
        stream.writeUint32(feeUnit);
        stream.writeBigInteger(totalInflationAmount);
        stream.writeBigInteger(inflationAmount);
        stream.writeUint32(initHeight);
        stream.writeDouble(deflationRatio);
        stream.writeUint32(deflationHeightInterval);
        stream.writeBigInteger(appendAgentDepositMin);
        stream.writeBigInteger(reduceAgentDepositMin);
        stream.writeUint16(byzantineRate);
        stream.writeUint16(agentCountMax);
        stream.writeDouble(0);
        stream.writeDouble(localAssertBase);
        stream.writeDouble(agentDepositBase);
        stream.writeDouble(superAgentDepositBase);
        stream.writeDouble(reservegentDepositBase);
        stream.writeUint32(this.maxCoinToOfCoinbase);
        stream.writeUint32(this.minRewardHeight);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        //block
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.blockMaxSize = byteBuffer.readUint32();
        this.resetTime = byteBuffer.readUint32();
        this.chainSwtichThreshold = byteBuffer.readByte();
        this.cacheSize = byteBuffer.readUint16();
        this.heightRange = byteBuffer.readUint16();
        this.maxRollback = byteBuffer.readUint16();
        this.consistencyNodePercent = byteBuffer.readByte();
        this.minNodeAmount = byteBuffer.readByte();
        this.downloadNumber = byteBuffer.readByte();
        this.extendMaxSize = byteBuffer.readUint16();
        this.validBlockInterval = byteBuffer.readUint16();
        this.smallBlockCache = byteBuffer.readByte();
        this.orphanChainMaxAge = byteBuffer.readByte();
        this.logLevel = byteBuffer.readString();
        this.singleDownloadTimeout = byteBuffer.readUint16();
        this.waitNetworkInterval = byteBuffer.readUint16();
        this.genesisBlockPath = byteBuffer.readString();
        this.cachedBlockSizeLimit = byteBuffer.readUint32();
        //protocol
        this.interval = byteBuffer.readShort();
        this.effectiveRatioMinimum = byteBuffer.readByte();
        this.continuousIntervalCountMinimum = byteBuffer.readShort();
        //cross
        this.minNodes = byteBuffer.readUint16();
        this.maxOutAmount = byteBuffer.readUint16();
        this.maxInAmount = byteBuffer.readUint16();
        this.sendHeight = byteBuffer.readUint16();
        this.byzantineRatio = byteBuffer.readUint16();
        this.minNodes = byteBuffer.readUint16();
        this.verifiers = byteBuffer.readString();
        this.mainByzantineRatio = byteBuffer.readUint16();
        this.maxSignatureCount = byteBuffer.readUint16();
        int registerCount = (int) byteBuffer.readVarInt();
        if(registerCount > 0){
            Set<String> verifierSet = new HashSet<>();
            for (int i = 0; i < registerCount; i++) {
                verifierSet.add(byteBuffer.readString());
            }
            this.verifierSet = verifierSet;
        }
        this.packingInterval = byteBuffer.readUint32();
        this.redPublishLockTime = byteBuffer.readUint32();
        this.stopAgentLockTime = byteBuffer.readUint32();
        this.reducedDepositLockTime = byteBuffer.readUint32();
        this.depositMin = byteBuffer.readBigInteger();
        this.depositMax = byteBuffer.readBigInteger();
        this.packDepositMin = byteBuffer.readBigInteger();
        this.entrustMin = byteBuffer.readBigInteger();
        this.seedNodes = byteBuffer.readString();
        this.pubKeyList = byteBuffer.readString();
        this.password = byteBuffer.readString();
        this.blockConsensusMaxSize = byteBuffer.readUint48();
        this.agentAssetId = byteBuffer.readUint16();
        this.agentChainId = byteBuffer.readUint16();
        this.awardAssetId = byteBuffer.readUint16();
        this.feeUnit = byteBuffer.readUint32();
        this.totalInflationAmount = byteBuffer.readBigInteger();
        this.inflationAmount = byteBuffer.readBigInteger();
        this.initHeight = byteBuffer.readUint32();
        this.deflationRatio = byteBuffer.readDouble();
        this.deflationHeightInterval = byteBuffer.readUint32();
        this.appendAgentDepositMin = byteBuffer.readBigInteger();
        this.reduceAgentDepositMin = byteBuffer.readBigInteger();
        this.byzantineRate = byteBuffer.readUint16();
        this.agentCountMax = byteBuffer.readUint16();
        byteBuffer.readDouble();
        this.localAssertBase = byteBuffer.readDouble();
        this.agentDepositBase = byteBuffer.readDouble();
        this.superAgentDepositBase = byteBuffer.readDouble();
        this.reservegentDepositBase = byteBuffer.readDouble();
        this.maxCoinToOfCoinbase = (int) byteBuffer.readUint32();
        this.minRewardHeight = byteBuffer.readUint32();
    }

    @Override
    public int size() {
        // block
        int size = 36;
        size += SerializeUtils.sizeOfString(logLevel);
        size += SerializeUtils.sizeOfString(genesisBlockPath);
        // protocol
        size += 5;
        // cross
        size += SerializeUtils.sizeOfUint16() * 8;
        size += SerializeUtils.sizeOfString(verifiers);
        size += SerializeUtils.sizeOfVarInt(verifierSet == null ? 0 : verifierSet.size());
        if(verifierSet != null){
            for (String verifier:verifierSet) {
                size += SerializeUtils.sizeOfString(verifier);
            }
        }
        // consensus
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfUint32() * 6;
        size += SerializeUtils.sizeOfDouble(deflationRatio);
        size += SerializeUtils.sizeOfBigInteger() * 8;
        size += SerializeUtils.sizeOfString(seedNodes);
        size += SerializeUtils.sizeOfString(pubKeyList);
        size += SerializeUtils.sizeOfUint16() * 5;
        size += SerializeUtils.sizeOfString(password);
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfDouble(localAssertBase);
        size += SerializeUtils.sizeOfDouble(0.0);
        size += SerializeUtils.sizeOfDouble(agentDepositBase);
        size += SerializeUtils.sizeOfDouble(superAgentDepositBase);
        size += SerializeUtils.sizeOfDouble(reservegentDepositBase);
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        return  size;
    }

    public ConfigBean() {
    }

    public ConfigBean(int chainId, int assetId) {
        this.chainId = chainId;
        this.assetId = assetId;
    }

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
}
