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

    public ChainParameters(int chainId, int assetId, long blockMaxSize, long resetTime, byte chainSwtichThreshold, int cacheSize, int heightRange, int maxRollback, byte consistencyNodePercent, byte minNodeAmount, byte downloadNumber, int extendMaxSize, int validBlockInterval, byte smallBlockCache, byte orphanChainMaxAge, String logLevel, int singleDownloadTimeout, int waitNetworkInterval, String genesisBlockPath, long cachedBlockSizeLimit) {
        this.chainId = chainId;
        this.assetId = assetId;
        this.blockMaxSize = blockMaxSize;
        this.resetTime = resetTime;
        this.chainSwtichThreshold = chainSwtichThreshold;
        this.cacheSize = cacheSize;
        this.heightRange = heightRange;
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
        this.waitNetworkInterval = waitNetworkInterval;
        this.genesisBlockPath = genesisBlockPath;
        this.cachedBlockSizeLimit = cachedBlockSizeLimit;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public ChainParameters() {
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

    public void setChainSwtichThreshold(byte chainSwtichThreshold) {
        this.chainSwtichThreshold = chainSwtichThreshold;
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

    public void setDownloadNumber(byte downloadNumber) {
        this.downloadNumber = downloadNumber;
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

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void setOrphanChainMaxAge(byte orphanChainMaxAge) {
        this.orphanChainMaxAge = orphanChainMaxAge;
    }

    public int getSingleDownloadTimeout() {
        return singleDownloadTimeout;
    }

    public int getWaitNetworkInterval() {
        return waitNetworkInterval;
    }

    public void setWaitNetworkInterval(int waitNetworkInterval) {
        this.waitNetworkInterval = waitNetworkInterval;
    }

    public void setSingleDownloadTimeout(int singleDownloadTimeout) {
        this.singleDownloadTimeout = singleDownloadTimeout;
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

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
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
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
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
    }

    @Override
    public int size() {
        int size = 36;
        size += SerializeUtils.sizeOfString(logLevel);
        size += SerializeUtils.sizeOfString(genesisBlockPath);
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChainParameters that = (ChainParameters) o;

        if (chainId != that.chainId) {
            return false;
        }
        if (assetId != that.assetId) {
            return false;
        }
        if (blockMaxSize != that.blockMaxSize) {
            return false;
        }
        if (resetTime != that.resetTime) {
            return false;
        }
        if (chainSwtichThreshold != that.chainSwtichThreshold) {
            return false;
        }
        if (cacheSize != that.cacheSize) {
            return false;
        }
        if (heightRange != that.heightRange) {
            return false;
        }
        if (maxRollback != that.maxRollback) {
            return false;
        }
        if (consistencyNodePercent != that.consistencyNodePercent) {
            return false;
        }
        if (minNodeAmount != that.minNodeAmount) {
            return false;
        }
        if (downloadNumber != that.downloadNumber) {
            return false;
        }
        if (extendMaxSize != that.extendMaxSize) {
            return false;
        }
        if (validBlockInterval != that.validBlockInterval) {
            return false;
        }
        if (smallBlockCache != that.smallBlockCache) {
            return false;
        }
        if (orphanChainMaxAge != that.orphanChainMaxAge) {
            return false;
        }
        if (singleDownloadTimeout != that.singleDownloadTimeout) {
            return false;
        }
        if (waitNetworkInterval != that.waitNetworkInterval) {
            return false;
        }
        if (cachedBlockSizeLimit != that.cachedBlockSizeLimit) {
            return false;
        }
        if (logLevel != null ? !logLevel.equals(that.logLevel) : that.logLevel != null) {
            return false;
        }
        return genesisBlockPath != null ? genesisBlockPath.equals(that.genesisBlockPath) : that.genesisBlockPath == null;
    }

    @Override
    public int hashCode() {
        int result = chainId;
        result = 31 * result + assetId;
        result = 31 * result + (int) (blockMaxSize ^ (blockMaxSize >>> 32));
        result = 31 * result + (int) (resetTime ^ (resetTime >>> 32));
        result = 31 * result + (int) chainSwtichThreshold;
        result = 31 * result + cacheSize;
        result = 31 * result + heightRange;
        result = 31 * result + maxRollback;
        result = 31 * result + (int) consistencyNodePercent;
        result = 31 * result + (int) minNodeAmount;
        result = 31 * result + (int) downloadNumber;
        result = 31 * result + extendMaxSize;
        result = 31 * result + validBlockInterval;
        result = 31 * result + (int) smallBlockCache;
        result = 31 * result + (int) orphanChainMaxAge;
        result = 31 * result + (logLevel != null ? logLevel.hashCode() : 0);
        result = 31 * result + singleDownloadTimeout;
        result = 31 * result + waitNetworkInterval;
        result = 31 * result + (genesisBlockPath != null ? genesisBlockPath.hashCode() : 0);
        result = 31 * result + (int) (cachedBlockSizeLimit ^ (cachedBlockSizeLimit >>> 32));
        return result;
    }
}
