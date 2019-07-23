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

package io.nuls.protocol.model;

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
     * 日志级别
     */
    private String logLevel;

    /**
     * 统计区间
     */
    private short interval;
    /**
     * 每个统计区间内的最小生效比例
     */
    private byte effectiveRatioMinimum;
    /**
     * 协议生效要满足的连续区间数
     */
    private short continuousIntervalCountMaximum;
    /**
     * 协议生效要满足的连续区间数
     */
    private short continuousIntervalCountMinimum;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
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

    public short getContinuousIntervalCountMaximum() {
        return continuousIntervalCountMaximum;
    }

    public void setContinuousIntervalCountMaximum(short continuousIntervalCountMaximum) {
        this.continuousIntervalCountMaximum = continuousIntervalCountMaximum;
    }

    public short getContinuousIntervalCountMinimum() {
        return continuousIntervalCountMinimum;
    }

    public void setContinuousIntervalCountMinimum(short continuousIntervalCountMinimum) {
        this.continuousIntervalCountMinimum = continuousIntervalCountMinimum;
    }

    public ChainParameters() {
    }

    public ChainParameters(int chainId, String logLevel, short interval, byte effectiveRatioMinimum, byte effectiveRatioMaximum, short continuousIntervalCountMaximum, short continuousIntervalCountMinimum) {
        this.chainId = chainId;
        this.logLevel = logLevel;
        this.interval = interval;
        this.effectiveRatioMinimum = effectiveRatioMinimum;
        this.continuousIntervalCountMaximum = continuousIntervalCountMaximum;
        this.continuousIntervalCountMinimum = continuousIntervalCountMinimum;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeString(logLevel);
        stream.writeShort(interval);
        stream.writeByte(effectiveRatioMinimum);
        stream.writeShort(continuousIntervalCountMaximum);
        stream.writeShort(continuousIntervalCountMinimum);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.logLevel = byteBuffer.readString();
        this.interval = byteBuffer.readShort();
        this.effectiveRatioMinimum = byteBuffer.readByte();
        this.continuousIntervalCountMaximum = byteBuffer.readShort();
        this.continuousIntervalCountMinimum = byteBuffer.readShort();
    }

    @Override
    public int size() {
        int size = 9;
        size += SerializeUtils.sizeOfString(logLevel);
        return size;
    }

    @Override
    public String toString() {
        return "ChainParameters{" +
                "chainId=" + chainId +
                ", logLevel='" + logLevel + '\'' +
                ", interval=" + interval +
                ", effectiveRatioMinimum=" + effectiveRatioMinimum +
                ", continuousIntervalCountMaximum=" + continuousIntervalCountMaximum +
                ", continuousIntervalCountMinimum=" + continuousIntervalCountMinimum +
                '}';
    }
}
