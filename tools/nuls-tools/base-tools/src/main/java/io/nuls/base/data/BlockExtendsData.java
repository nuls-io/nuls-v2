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
package io.nuls.base.data;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * @author Niels
 */
public class BlockExtendsData extends BaseNulsData {

    protected long roundIndex;

    protected int consensusMemberCount;

    protected long roundStartTime;

    protected int packingIndexOfRound;

    /**
     * 主网版本是否与区块的版本一致
     */
    private boolean upgrade;

    /**
     * 主网版本
     */
    private short mainVersion;

    /**
     * 区块的版本
     */
    private short blockVersion;

    /**
     * 统计区间大小(500-10000)
     */
    private short interval;

    /**
     * 每个统计区间内的最小生效比例(60-100)
     */
    private byte effectiveRatio;

    /**
     * 协议生效要满足的连续区间数(50-1000)
     */
    private short continuousIntervalCount;

    private byte[] stateRoot;

    public BlockExtendsData() {
    }

    public BlockExtendsData(byte[] extend) {
        try {
            this.parse(extend, 0);
        } catch (NulsException e) {
            Log.error(e);
        }
    }

    public boolean isUpgrade() {
        return upgrade;
    }

    public void setUpgrade(boolean upgrade) {
        this.upgrade = upgrade;
    }

    public short getMainVersion() {
        return mainVersion;
    }

    public void setMainVersion(short mainVersion) {
        this.mainVersion = mainVersion;
    }

    public short getBlockVersion() {
        return blockVersion;
    }

    public void setBlockVersion(short blockVersion) {
        this.blockVersion = blockVersion;
    }

    public short getInterval() {
        return interval;
    }

    public void setInterval(short interval) {
        this.interval = interval;
    }

    public byte getEffectiveRatio() {
        return effectiveRatio;
    }

    public void setEffectiveRatio(byte effectiveRatio) {
        this.effectiveRatio = effectiveRatio;
    }

    public short getContinuousIntervalCount() {
        return continuousIntervalCount;
    }

    public void setContinuousIntervalCount(short continuousIntervalCount) {
        this.continuousIntervalCount = continuousIntervalCount;
    }

    /**
     * 根据轮次开始时间计算轮次结束时间
     * @param packing_interval 打包间隔时间（单位：毫秒）
     * */
    public long getRoundEndTime(long packing_interval) {
        return roundStartTime + consensusMemberCount * packing_interval;
    }

    public int getConsensusMemberCount() {
        return consensusMemberCount;
    }

    public void setConsensusMemberCount(int consensusMemberCount) {
        this.consensusMemberCount = consensusMemberCount;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public int getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(int packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }


    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint32();  // roundIndex
        size += SerializeUtils.sizeOfUint16();  // consensusMemberCount
        size += SerializeUtils.sizeOfUint48();  // roundStartTime
        size += SerializeUtils.sizeOfUint16();  // packingIndexOfRound
        if (upgrade) {
            size += 10;
        } else {
            size += 3;
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(roundIndex);
        stream.writeUint16(consensusMemberCount);
        stream.writeUint48(roundStartTime);
        stream.writeUint16(packingIndexOfRound);
        stream.writeBoolean(upgrade);
        stream.writeShort(mainVersion);
        if (upgrade) {
            stream.writeShort(blockVersion);
            stream.writeShort(interval);
            stream.writeByte(effectiveRatio);
            stream.writeShort(continuousIntervalCount);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.roundIndex = byteBuffer.readUint32();
        this.consensusMemberCount = byteBuffer.readUint16();
        this.roundStartTime = byteBuffer.readUint48();
        this.packingIndexOfRound = byteBuffer.readUint16();
        this.upgrade = byteBuffer.readBoolean();
        this.mainVersion = byteBuffer.readShort();
        if (upgrade) {
            this.blockVersion = byteBuffer.readShort();
            this.interval = byteBuffer.readShort();
            this.effectiveRatio = byteBuffer.readByte();
            this.continuousIntervalCount = byteBuffer.readShort();
        }
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }
}
