/*
 *
 *  MIT License
 *
 *  Copyright (c) 2017-2019 nuls.io
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 */
package io.nuls.base.data;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * @author Niels
 */
public class BlockExtendsData extends BaseNulsData {

    /**
     * Round
     */
    private long roundIndex;

    /**
     * Number of consensus nodes
     */
    private int consensusMemberCount;

    /**
     * Start time of round
     */
    private long roundStartTime;

    /**
     * Order in rounds
     */
    private int packingIndexOfRound;

    /**
     * The current effective version of the main network
     */
    private short mainVersion;

    /**
     * The version of the block can be understood as the version of the local wallet
     */
    private short blockVersion;

    /**
     * The minimum effective ratio within each statistical interval(60-100)
     */
    private byte effectiveRatio;

    /**
     * The number of consecutive intervals that the agreement must meet in order to take effect(50-1000)
     */
    private short continuousIntervalCount;

    /**
     * Initial state root of smart contract
     */
    private byte[] stateRoot;

    private byte[] seed;

    private byte[] nextSeedHash;

    public BlockExtendsData() {
    }

    public BlockExtendsData(byte[] extend) {
        try {
            this.parse(extend, 0);
        } catch (NulsException e) {
            Log.error(e);
        }
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
     * Calculate the end time of the round based on the start time of the round
     * @param packingInterval Packaging interval time（unit：second）
     * */
    public long getRoundEndTime(long packingInterval) {
        return roundStartTime + consensusMemberCount * packingInterval;
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
        size += SerializeUtils.sizeOfUint32();  // roundStartTime
        size += SerializeUtils.sizeOfUint16();  // packingIndexOfRound
        size += 7;
        size += SerializeUtils.sizeOfBytes(stateRoot);
        if (nextSeedHash != null) {
            size += 40;
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(roundIndex);
        stream.writeUint16(consensusMemberCount);
        stream.writeUint32(roundStartTime);
        stream.writeUint16(packingIndexOfRound);
        stream.writeShort(mainVersion);
        stream.writeShort(blockVersion);
        stream.writeByte(effectiveRatio);
        stream.writeShort(continuousIntervalCount);
        stream.writeBytesWithLength(stateRoot);
        if (nextSeedHash != null) {
            stream.write(seed);
            stream.write(nextSeedHash);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.roundIndex = byteBuffer.readUint32();
        this.consensusMemberCount = byteBuffer.readUint16();
        this.roundStartTime = byteBuffer.readUint32();
        this.packingIndexOfRound = byteBuffer.readUint16();
        this.mainVersion = byteBuffer.readShort();
        this.blockVersion = byteBuffer.readShort();
        this.effectiveRatio = byteBuffer.readByte();
        this.continuousIntervalCount = byteBuffer.readShort();
        this.stateRoot = byteBuffer.readByLengthByte();
        if (!byteBuffer.isFinished() && byteBuffer.getPayload().length >= (byteBuffer.getCursor() + 40)) {
            this.seed = byteBuffer.readBytes(32);
            this.nextSeedHash = byteBuffer.readBytes(8);
        }
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    public byte[] getNextSeedHash() {
        return nextSeedHash;
    }

    public void setNextSeedHash(byte[] nextSeedHash) {
        this.nextSeedHash = nextSeedHash;
    }

    @Override
    public String toString() {
        return "{mainVersion=" + mainVersion +
                ", blockVersion=" + blockVersion +
                ", effectiveRatio=" + effectiveRatio +
                ", continuousIntervalCount=" + continuousIntervalCount +
                '}';
    }
}
