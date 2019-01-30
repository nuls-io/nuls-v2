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
import io.nuls.protocol.constant.ConfigConstant;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.tools.parse.config.ConfigItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolConfig extends BaseNulsData {

    /**
     * 链ID
     */
    private int chainId;
    /**
     * 日志级别
     */
    private String logLevel;

    /**
     * 统计区间最大值
     */
    private short intervalMaximum;
    /**
     * 统计区间最小值
     */
    private short intervalMinimum;
    /**
     * 每个统计区间内的最小生效比例
     */
    private byte effectiveRatioMinimum;
    /**
     * 每个统计区间内的最大生效比例
     */
    private byte effectiveRatioMaximum;
    /**
     * 协议生效要满足的连续区间数
     */
    private short continuousIntervalCountMaximum;
    /**
     * 协议生效要满足的连续区间数
     */
    private short continuousIntervalCountMinimum;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeString(logLevel);
        stream.writeShort(intervalMaximum);
        stream.writeShort(intervalMinimum);
        stream.writeByte(effectiveRatioMinimum);
        stream.writeByte(effectiveRatioMaximum);
        stream.writeShort(continuousIntervalCountMaximum);
        stream.writeShort(continuousIntervalCountMinimum);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.logLevel = byteBuffer.readString();
        this.intervalMaximum = byteBuffer.readShort();
        this.intervalMinimum = byteBuffer.readShort();
        this.effectiveRatioMinimum = byteBuffer.readByte();
        this.effectiveRatioMaximum = byteBuffer.readByte();
        this.continuousIntervalCountMaximum = byteBuffer.readShort();
        this.continuousIntervalCountMinimum = byteBuffer.readShort();
    }

    @Override
    public int size() {
        int size = 12;
        size += SerializeUtils.sizeOfString(logLevel);
        return size;
    }

    public void init(Map<String, ConfigItem> map) {
        this.chainId = Integer.parseInt(map.get(ConfigConstant.CHAIN_ID).getValue());
        this.logLevel = map.get(ConfigConstant.LOG_LEVEL).getValue();
        this.intervalMaximum = Short.parseShort(map.get(ConfigConstant.INTERVAL_MAXIMUM).getValue());
        this.intervalMinimum = Short.parseShort(map.get(ConfigConstant.INTERVAL_MINIMUM).getValue());
        this.effectiveRatioMinimum = Byte.parseByte(map.get(ConfigConstant.EFFECTIVE_RATIO_MINIMUM).getValue());
        this.effectiveRatioMaximum = Byte.parseByte(map.get(ConfigConstant.EFFECTIVE_RATIO_MAXIMUM).getValue());
        this.continuousIntervalCountMaximum = Short.parseShort(map.get(ConfigConstant.CONTINUOUS_INTERVAL_COUNT_MAXIMUM).getValue());
        this.continuousIntervalCountMinimum = Short.parseShort(map.get(ConfigConstant.CONTINUOUS_INTERVAL_COUNT_MINIMUM).getValue());
    }
}
