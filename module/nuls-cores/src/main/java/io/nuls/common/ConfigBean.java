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

import io.nuls.core.model.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Transaction module chain setting
 *
 * @author: Charlie
 * @date: 2019/03/14
 */
public class ConfigBean {

    /**
     * chain id
     */
    private int chainId;
    /**
     * assets id
     */
    private int assetId;
    /*-------------------------[Block]-----------------------------*/
    /**
     * Block size threshold
     */
    private long blockMaxSize;
    /**
     * Network reset threshold
     */
    private long resetTime;
    /**
     * Chain switching occurs when the fork chain is several blocks higher than the main chain
     */
    private byte chainSwtichThreshold;
    /**
     * Forked chain、The maximum cache size of orphan chain blocks
     */
    private int cacheSize;
    /**
     * Scope of receiving new blocks
     */
    private int heightRange;
    /**
     * Maximum block size for each rollback
     */
    private int maxRollback;
    /**
     * Consistent node ratio
     */
    private byte consistencyNodePercent;
    /**
     * Minimum number of nodes for system operation
     */
    private byte minNodeAmount;
    /**
     * How many blocks are downloaded from a node each time
     */
    private byte downloadNumber;
    /**
     * The maximum length of the extended field in the block header
     */
    private int extendMaxSize;
    /**
     * To prevent malicious nodes from leaving the block prematurely,Set this parameter
     * Discard the block if its timestamp is greater than the current time
     */
    private int validBlockInterval;
    /**
     * How many cell blocks can be cached at most when the system is running normally and received from other nodes
     */
    private byte smallBlockCache;
    /**
     * Orphan Chain Maximum Age
     */
    private byte orphanChainMaxAge;
    /**
     * log level
     */
    private String logLevel;
    /**
     * The timeout for downloading a single block
     */
    private int singleDownloadTimeout;

    /**
     * Waiting for the time interval for network stability
     */
    private int waitNetworkInterval;

    /**
     * Genesis block configuration file path
     */
    private String genesisBlockPath;

    /**
     * Maximum number of cached block bytes during block synchronization process
     */
    private long cachedBlockSizeLimit;
    /*-------------------------[Protocol]-----------------------------*/
    /**
     * Statistical interval
     */
    private short interval;
    /**
     * The minimum effective ratio within each statistical interval
     */
    private byte effectiveRatioMinimum;
    /**
     * The minimum number of consecutive intervals that a protocol must meet in order to take effect
     */
    private short continuousIntervalCountMinimum;
    /*-------------------------[CrossChain]-----------------------------*/
    /**
     * Minimum number of links
     * Minimum number of links
     */
    private int minNodes;

    /**
     * Maximum number of links
     * */
    private int maxOutAmount;

    /**
     * Maximum number of links
     * */
    private int maxInAmount;

    /**
     * How many blocks are packaged for cross chain transactions and broadcast to other chains
     * */
    private int sendHeight;

    /**
     * Byzantine proportion
     * */
    private int byzantineRatio;

    /**
     * Minimum number of signatures
     * */
    private int minSignature;

    /**
     * Main network verifier information
     * */
    private String verifiers;

    /**
     * Main network Byzantine proportion
     * */
    private int mainByzantineRatio;

    /**
     * Maximum number of signature verifications on the main network
     * */
    private int maxSignatureCount;

    /**
     * List of main network validators
     * */
    private Set<String> verifierSet = new HashSet<>();

    /*-------------------------[Consensus]-----------------------------*/

    /**
     * Packaging interval time
     * Packing interval time
     */
    private long packingInterval;
    /**
     * Obtaining red card deposit lock up time
     * Lock-in time to get a red card margin
     */
    private long redPublishLockTime;
    /**
     * Cancellation of node margin locking time
     * Log-off node margin locking time
     */
    private long stopAgentLockTime;
    /**
     * The minimum value of commission ratio
     * Minimum commission ratio
     */
    private byte commissionRateMin;
    /**
     * Maximum commission ratio
     * Maximum commission ratio
     */
    private byte commissionRateMax;
    /**
     * Minimum margin value for creating nodes
     * Minimum margin for creating nodes
     */
    private BigInteger depositMin;
    /**
     * Maximum margin value for creating nodes
     * Maximum margin for creating nodes
     */
    private BigInteger depositMax;
    /**
     * Minimum commissioned amount for node block output
     * Minimum Delegation Amount of Node Block
     */
    private BigInteger commissionMin;
    /**
     * Maximum amount of node delegation
     * Maximum Node Delegation Amount
     */
    private BigInteger commissionMax;

    /**
     * Minimum amount entrusted
     * Minimum amount entrusted
     */
    private BigInteger entrusterDepositMin;

    /**
     * Seed node
     * Seed node
     */
    private String seedNodes;

    /**
     * Block node password
     * */
    private String password;

    /**
     * Consensus reward obtained by packaging a block
     * Annual inflation/Annual output of blocks
     * */
    private BigInteger blockReward;


    /**
     * Create node assetsID
     * agent assets id
     */
    private int agentAssetId;

    /**
     * Create a node asset chainID
     * Create node asset chain ID
     */
    private int agentChainId;


    /**
     * Consensus reward assetsID
     * Award asset chain ID
     */
    private int awardAssetId;

    /**
     * Total shrinkage
     * Total inflation amount
     */
    private BigInteger totalInflationAmount;

    /**
     * Initial inflation amount
     * Initial Inflation Amount
     */
    private BigInteger inflationAmount;

    /**
     * Inflation start time
     * */
    private long initTime;

    /**
     * Deflationary ratio
     * */
    private double deflationRatio;

    /**
     * Deflation interval time
     * */
    private long deflationTimeInterval;

    /*-------------------------[SmartContract]-----------------------------*/
    /**
     * viewMethod maximum consumptiongas
     */
    private long maxViewGas;

    /*-------------------------[Transaction]-----------------------------*/
    /** Maximum value of individual transaction data(B)*/
    private long txMaxSize;
    /**
     * The time threshold for module unified verification after obtaining transactions during packaging,
     * Including the need to re validate transactions that have been filtered out through unified verification, etc.
     */
    private int moduleVerifyPercent;
    /** Package to obtain transactions forRPCReserve time for transmission to consensus,If the timeout occurs, the transaction needs to be processed to restore the queue to be packaged*/
    private int packageRpcReserveTime;
    /** The maximum capacity of receiving new transaction queues in the network Unprocessed transaction queue**/
    private long txUnverifiedQueueSize;
    /** Orphan Trading Lifetime,Exceeding will be cleared**/
    private int orphanTtl;

    private String feeAssets;
    private String feeUnit;

    private String feeCoefficient;

    private Map<String, Long> feeUnitMap;
    private Map<String, Double> feeCoeffMap;
    /*----------------------------------------------------------------------*/

    private void initFeeUnitMap() {
        if (StringUtils.isNotBlank(feeAssets) && StringUtils.isNotBlank(feeUnit) && feeUnitMap == null) {
            feeUnitMap = new HashMap<>();
            String[] keys = feeAssets.split(",");
            String[] vals = feeUnit.split(",");
            for (int i = 0; i < keys.length; i++) {
                feeUnitMap.put(keys[i], Long.parseLong(vals[i]));
            }
        }
        if (StringUtils.isNotBlank(feeAssets) && StringUtils.isNotBlank(feeCoefficient) && feeCoeffMap == null) {
            feeCoeffMap = new HashMap<>();
            String[] keys = feeAssets.split(",");
            String[] vals = feeCoefficient.split(",");
            for (int i = 0; i < keys.length; i++) {
                feeCoeffMap.put(keys[i], Double.parseDouble(vals[i]));
            }
        }
    }


    public Long getFeeUnit(int chainId, int assetId) {
        return getFeeUnit(NCUtils.getTokenId(chainId, assetId));
    }

    public Long getFeeUnit(String tokenId) {
        if (null == feeUnitMap) {
            initFeeUnitMap();
        }
        return feeUnitMap.get(tokenId);
    }

    public Double getFeeCoefficient(int chainId, int assetId) {
        return getFeeCoefficient(NCUtils.getTokenId(chainId, assetId));
    }

    public Double getFeeCoefficient(String tokenId) {
        if (null == feeCoeffMap) {
            initFeeUnitMap();
        }
        return feeCoeffMap.get(tokenId);
    }

    public void setFeeAssets(String feeAssets) {
        this.feeAssets = feeAssets;
    }

    public Set<String> getFeeAssetsSet() {
        if (null == feeUnitMap) {
            initFeeUnitMap();
        }
        return feeUnitMap.keySet();
    }

    public void setFeeUnit(String feeUnit) {
        this.feeUnit = feeUnit;
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
