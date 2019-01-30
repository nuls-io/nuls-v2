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


    {
        "name": "",
            "remark": "日志级别",
            "readOnly": "false",
            "value": "DEBUG"
    },
    {
        "name": "chainId",
            "remark": "链ID",
            "readOnly": "true",
            "value": "12345"
    },
    {
        "name": "intervalMaximum",
            "remark": "统计区间最大值",
            "readOnly": "true",
            "value": "10000"
    },
    {
        "name": "intervalMinimum",
            "remark": "统计区间最小值",
            "readOnly": "true",
            "value": "500"
    },
    {
        "name": "effectiveRatioMinimum",
            "remark": "每个统计区间内的最小生效比例",
            "readOnly": "true",
            "value": "70"
    },
    {
        "name": "effectiveRatioMaximum",
            "remark": "每个统计区间内的最大生效比例",
            "readOnly": "true",
            "value": "100"
    },
    {
        "name": "continuousIntervalCountMaximum",
            "remark": "协议生效要满足的连续区间数",
            "readOnly": "true",
            "value": "1000"
    },
    {
        "name": "continuousIntervalCountMinimum",
            "remark": "协议生效要满足的连续区间数",
            "readOnly": "true",
            "value": "50"
    }

    /**
     * 链ID
     */
    private int chainId;
    /**
     * 日志级别
     */
    private String logLevel;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(chainName);
        stream.writeUint16(chainId);
        stream.writeUint32(protocolMaxSize);
        stream.writeUint16(resetTime);
        stream.writeUint16(chainSwtichThreshold);
        stream.writeUint16(cacheSize);
        stream.writeUint16(heightRange);
        stream.writeUint16(maxRollback);
        stream.writeUint16(consistencyNodePercent);
        stream.writeUint16(minNodeAmount);
        stream.writeUint16(downloadNumber);
        stream.writeUint16(extendMaxSize);
        stream.writeUint16(validBlockInterval);
        stream.writeUint16(protocolCache);
        stream.writeUint16(smallBlockCache);
        stream.writeUint16(orphanChainMaxAge);
        stream.writeString(logLevel);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainName = byteBuffer.readString();
        this.chainId = byteBuffer.readUint16();
        this.protocolMaxSize = (int) byteBuffer.readUint32();
        this.resetTime = byteBuffer.readUint16();
        this.chainSwtichThreshold = byteBuffer.readUint16();
        this.cacheSize = byteBuffer.readUint16();
        this.heightRange = byteBuffer.readUint16();
        this.maxRollback = byteBuffer.readUint16();
        this.consistencyNodePercent = byteBuffer.readUint16();
        this.minNodeAmount = byteBuffer.readUint16();
        this.downloadNumber = byteBuffer.readUint16();
        this.extendMaxSize = byteBuffer.readUint16();
        this.validBlockInterval = byteBuffer.readUint16();
        this.protocolCache = byteBuffer.readUint16();
        this.smallBlockCache = byteBuffer.readUint16();
        this.orphanChainMaxAge = byteBuffer.readUint16();
        this.logLevel = byteBuffer.readString();
    }

    @Override
    public int size() {
        int size = 0;
        size += (16 * SerializeUtils.sizeOfUint16());
        size += SerializeUtils.sizeOfString(chainName);
        size += SerializeUtils.sizeOfString(logLevel);
        return size;
    }

    public void init(Map<String, ConfigItem> map) {
        this.chainName = map.get(ConfigConstant.CHAIN_NAME).getValue();
        this.chainId = Integer.parseInt(map.get(ConfigConstant.CHAIN_ID).getValue());
        this.protocolMaxSize = Integer.parseInt(map.get(ConfigConstant.BLOCK_MAX_SIZE).getValue());
        this.resetTime = Integer.parseInt(map.get(ConfigConstant.RESET_TIME).getValue());
        this.chainSwtichThreshold = Integer.parseInt(map.get(ConfigConstant.CHAIN_SWTICH_THRESHOLD).getValue());
        this.cacheSize = Integer.parseInt(map.get(ConfigConstant.CACHE_SIZE).getValue());
        this.heightRange = Integer.parseInt(map.get(ConfigConstant.HEIGHT_RANGE).getValue());
        this.maxRollback = Integer.parseInt(map.get(ConfigConstant.MAX_ROLLBACK).getValue());
        this.consistencyNodePercent = Integer.parseInt(map.get(ConfigConstant.CONSISTENCY_NODE_PERCENT).getValue());
        this.minNodeAmount = Integer.parseInt(map.get(ConfigConstant.MIN_NODE_AMOUNT).getValue());
        this.downloadNumber = Integer.parseInt(map.get(ConfigConstant.DOWNLOAD_NUMBER).getValue());
        this.extendMaxSize = Integer.parseInt(map.get(ConfigConstant.EXTEND_MAX_SIZE).getValue());
        this.validBlockInterval = Integer.parseInt(map.get(ConfigConstant.VALID_BLOCK_INTERVAL).getValue());
        this.protocolCache = Integer.parseInt(map.get(ConfigConstant.BLOCK_CACHE).getValue());
        this.smallBlockCache = Integer.parseInt(map.get(ConfigConstant.SMALL_BLOCK_CACHE).getValue());
        this.orphanChainMaxAge = Integer.parseInt(map.get(ConfigConstant.ORPHAN_CHAIN_MAX_AGE).getValue());
        this.logLevel = map.get(ConfigConstant.LOG_LEVEL).getValue();
    }
}
