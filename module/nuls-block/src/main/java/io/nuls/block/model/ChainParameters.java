/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.block.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 链的运行时参数
 *
 * @author captain
 * @version 1.0
 * @date 19-2-26 上午10:49
 */
public class ChainParameters extends BaseNulsData {

    /**
     * 链名
     */
    private String chainName;
    /**
     * 链ID
     */
    private int chainId;
    /**
     * 默认资产ID
     */
    private int assetId;
    /**
     * 区块大小阈值
     */
    private int blockMaxSize;
    /**
     * 网络重置阈值
     */
    private int resetTime;
    /**
     * 分叉链比主链高几个区块就进行链切换
     */
    private int chainSwtichThreshold;
    /**
     * 分叉链、孤儿链区块最大缓存数量
     */
    private int cacheSize;
    /**
     * 接收新区块的范围
     */
    private int heightRange;
    /**
     * 批量下载区块时,如果收到CompleteMessage时,区块还没有保存完,每一个区块预留多长等待时间
     */
    private int waitInterval;
    /**
     * 每次回滚区块最大值
     */
    private int maxRollback;
    /**
     * 一致节点比例
     */
    private int consistencyNodePercent;
    /**
     * 系统运行最小节点数
     */
    private int minNodeAmount;
    /**
     * 每次从一个节点下载多少区块
     */
    private int downloadNumber;
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
    private int smallBlockCache;
    /**
     * 孤儿链最大年龄
     */
    private int orphanChainMaxAge;
    /**
     * 日志级别
     */
    private String logLevel;
    /**
     * 下载单个区块的超时时间
     */
    private int singleDownloadTimeout;

    /**
     * 下载多个区块的超时时间
     */
    private int batchDownloadTimeout;

    /**
     * 批量下载区块时,如果收到CompleteMessage时,区块还没有保存完,最多循环等待几个回合
     */
    private int maxLoop;

    /**
     * 两次区块同步之间的时间间隔
     */
    private int synSleepInterval;

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

    public ChainParameters() {
    }

    public ChainParameters(String chainName, int chainId, int assetId, int blockMaxSize, int resetTime, int chainSwtichThreshold, int cacheSize, int heightRange, int waitInterval, int maxRollback, int consistencyNodePercent, int minNodeAmount, int downloadNumber, int extendMaxSize, int validBlockInterval, int smallBlockCache, int orphanChainMaxAge, String logLevel, int singleDownloadTimeout, int batchDownloadTimeout, int maxLoop, int synSleepInterval, int waitNetworkInterval, int cleanParam, String genesisBlockPath, long cachedBlockSizeLimit) {
        this.chainName = chainName;
        this.chainId = chainId;
        this.assetId = assetId;
        this.blockMaxSize = blockMaxSize;
        this.resetTime = resetTime;
        this.chainSwtichThreshold = chainSwtichThreshold;
        this.cacheSize = cacheSize;
        this.heightRange = heightRange;
        this.waitInterval = waitInterval;
        this.maxRollback = maxRollback;
        this.consistencyNodePercent = consistencyNodePercent;
        this.minNodeAmount = minNodeAmount;
        this.downloadNumber = downloadNumber;
        this.extendMaxSize = extendMaxSize;
        this.validBlockInterval = validBlockInterval;
        this.smallBlockCache = smallBlockCache;
        this.orphanChainMaxAge = orphanChainMaxAge;
        this.logLevel = logLevel;
        this.singleDownloadTimeout = singleDownloadTimeout;
        this.batchDownloadTimeout = batchDownloadTimeout;
        this.maxLoop = maxLoop;
        this.synSleepInterval = synSleepInterval;
        this.waitNetworkInterval = waitNetworkInterval;
        this.genesisBlockPath = genesisBlockPath;
        this.cachedBlockSizeLimit = cachedBlockSizeLimit;
    }

    public String getGenesisBlockPath() {
        return genesisBlockPath;
    }

    public void setGenesisBlockPath(String genesisBlockPath) {
        this.genesisBlockPath = genesisBlockPath;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getBlockMaxSize() {
        return blockMaxSize;
    }

    public void setBlockMaxSize(int blockMaxSize) {
        this.blockMaxSize = blockMaxSize;
    }

    public int getResetTime() {
        return resetTime;
    }

    public void setResetTime(int resetTime) {
        this.resetTime = resetTime;
    }

    public int getChainSwtichThreshold() {
        return chainSwtichThreshold;
    }

    public void setChainSwtichThreshold(int chainSwtichThreshold) {
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

    public int getWaitInterval() {
        return waitInterval;
    }

    public void setWaitInterval(int waitInterval) {
        this.waitInterval = waitInterval;
    }

    public int getMaxRollback() {
        return maxRollback;
    }

    public void setMaxRollback(int maxRollback) {
        this.maxRollback = maxRollback;
    }

    public int getConsistencyNodePercent() {
        return consistencyNodePercent;
    }

    public void setConsistencyNodePercent(int consistencyNodePercent) {
        this.consistencyNodePercent = consistencyNodePercent;
    }

    public int getSingleDownloadTimeout() {
        return singleDownloadTimeout;
    }

    public void setSingleDownloadTimeout(int singleDownloadTimeout) {
        this.singleDownloadTimeout = singleDownloadTimeout;
    }

    public int getBatchDownloadTimeout() {
        return batchDownloadTimeout;
    }

    public void setBatchDownloadTimeout(int batchDownloadTimeout) {
        this.batchDownloadTimeout = batchDownloadTimeout;
    }

    public long getCachedBlockSizeLimit() {
        return cachedBlockSizeLimit;
    }

    public void setCachedBlockSizeLimit(long cachedBlockSizeLimit) {
        this.cachedBlockSizeLimit = cachedBlockSizeLimit;
    }

    public int getMinNodeAmount() {
        return minNodeAmount;
    }

    public void setMinNodeAmount(int minNodeAmount) {
        this.minNodeAmount = minNodeAmount;
    }

    public int getDownloadNumber() {
        return downloadNumber;
    }

    public void setDownloadNumber(int downloadNumber) {
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

    public int getSmallBlockCache() {
        return smallBlockCache;
    }

    public void setSmallBlockCache(int smallBlockCache) {
        this.smallBlockCache = smallBlockCache;
    }

    public int getOrphanChainMaxAge() {
        return orphanChainMaxAge;
    }

    public void setOrphanChainMaxAge(int orphanChainMaxAge) {
        this.orphanChainMaxAge = orphanChainMaxAge;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public int getMaxLoop() {
        return maxLoop;
    }

    public void setMaxLoop(int maxLoop) {
        this.maxLoop = maxLoop;
    }

    public int getSynSleepInterval() {
        return synSleepInterval;
    }

    public void setSynSleepInterval(int synSleepInterval) {
        this.synSleepInterval = synSleepInterval;
    }

    public int getWaitNetworkInterval() {
        return waitNetworkInterval;
    }

    public void setWaitNetworkInterval(int waitNetworkInterval) {
        this.waitNetworkInterval = waitNetworkInterval;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(chainName);
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeUint32(blockMaxSize);
        stream.writeUint16(resetTime);
        stream.writeUint16(chainSwtichThreshold);
        stream.writeUint16(cacheSize);
        stream.writeUint16(heightRange);
        stream.writeUint16(waitInterval);
        stream.writeUint16(maxRollback);
        stream.writeUint16(consistencyNodePercent);
        stream.writeUint16(minNodeAmount);
        stream.writeUint16(downloadNumber);
        stream.writeUint16(extendMaxSize);
        stream.writeUint16(validBlockInterval);
        stream.writeUint16(smallBlockCache);
        stream.writeUint16(orphanChainMaxAge);
        stream.writeString(logLevel);
        stream.writeUint16(singleDownloadTimeout);
        stream.writeUint16(batchDownloadTimeout);
        stream.writeUint16(maxLoop);
        stream.writeUint16(synSleepInterval);
        stream.writeUint16(waitNetworkInterval);
        stream.writeString(genesisBlockPath);
        stream.writeUint32(cachedBlockSizeLimit);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainName = byteBuffer.readString();
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.blockMaxSize = (int) byteBuffer.readUint32();
        this.resetTime = byteBuffer.readUint16();
        this.chainSwtichThreshold = byteBuffer.readUint16();
        this.cacheSize = byteBuffer.readUint16();
        this.heightRange = byteBuffer.readUint16();
        this.waitInterval = byteBuffer.readUint16();
        this.maxRollback = byteBuffer.readUint16();
        this.consistencyNodePercent = byteBuffer.readUint16();
        this.minNodeAmount = byteBuffer.readUint16();
        this.downloadNumber = byteBuffer.readUint16();
        this.extendMaxSize = byteBuffer.readUint16();
        this.validBlockInterval = byteBuffer.readUint16();
        this.smallBlockCache = byteBuffer.readUint16();
        this.orphanChainMaxAge = byteBuffer.readUint16();
        this.logLevel = byteBuffer.readString();
        this.singleDownloadTimeout = byteBuffer.readUint16();
        this.batchDownloadTimeout = byteBuffer.readUint16();
        this.maxLoop = byteBuffer.readUint16();
        this.synSleepInterval = byteBuffer.readUint16();
        this.waitNetworkInterval = byteBuffer.readUint16();
        this.genesisBlockPath = byteBuffer.readString();
        this.cachedBlockSizeLimit = byteBuffer.readUint32();
    }

    @Override
    public int size() {
        int size = 0;
        size += (24 * SerializeUtils.sizeOfUint16());
        size += SerializeUtils.sizeOfString(chainName);
        size += SerializeUtils.sizeOfString(logLevel);
        size += SerializeUtils.sizeOfString(genesisBlockPath);
        return size;
    }
}
