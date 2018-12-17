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

    private Integer mainVersion;

    private Integer currentVersion;

    private Integer percent;

    private Long delay;

    private byte[] stateRoot;

    /**
     * 根据轮次开始时间计算轮次结束时间
     * @param packing_interval 打包间隔时间（单位：毫秒）
     * */
    public long getRoundEndTime(long packing_interval) {
        return roundStartTime + consensusMemberCount * packing_interval;
    }

    public BlockExtendsData() {
    }

    public BlockExtendsData(byte[] extend) {
        try {
            this.parse(extend, 0);
        } catch (NulsException e) {
            Log.error(e);
        }
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
        if (currentVersion != null) {
            size += SerializeUtils.sizeOfUint32();  // mainVersion
            size += SerializeUtils.sizeOfUint32();  // currentVersion
            size += SerializeUtils.sizeOfUint16();  // percent;
            size += SerializeUtils.sizeOfUint32();  // delay;
            size += SerializeUtils.sizeOfBytes(stateRoot);
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(roundIndex);
        stream.writeUint16(consensusMemberCount);
        stream.writeUint48(roundStartTime);
        stream.writeUint16(packingIndexOfRound);
        if (currentVersion != null) {
            stream.writeUint32(mainVersion);
            stream.writeUint32(currentVersion);
            stream.writeUint16(percent);
            stream.writeUint32(delay);
            stream.writeBytesWithLength(stateRoot);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.roundIndex = byteBuffer.readUint32();
        this.consensusMemberCount = byteBuffer.readUint16();
        this.roundStartTime = byteBuffer.readUint48();
        this.packingIndexOfRound = byteBuffer.readUint16();
        if (!byteBuffer.isFinished()) {
            this.mainVersion = byteBuffer.readInt32();
            this.currentVersion = byteBuffer.readInt32();
            this.percent = byteBuffer.readUint16();
            this.delay = byteBuffer.readUint32();
            this.stateRoot = byteBuffer.readByLengthByte();
        }
    }

    public Integer getMainVersion() {
        return mainVersion;
    }

    public void setMainVersion(Integer mainVersion) {
        this.mainVersion = mainVersion;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public String getProtocolKey() {
        if (currentVersion != null && currentVersion > 1) {
            return this.currentVersion + "-" + this.percent + "-" + this.delay;
        }
        return null;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }
}
