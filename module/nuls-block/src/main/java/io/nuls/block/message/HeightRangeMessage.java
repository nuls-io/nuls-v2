/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseBusinessMessage;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

import static io.nuls.block.utils.LoggerUtil.COMMON_LOG;

/**
 * 异步请求处理完成响应消息
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:37
 */
public class HeightRangeMessage extends BaseBusinessMessage {

    private long startHeight;
    private long endHeight;

    public HeightRangeMessage() {
    }

    public HeightRangeMessage(long startHeight, long endHeight) {
        this.startHeight = startHeight;
        this.endHeight = endHeight;
    }

    public long getStartHeight() {
        return startHeight;
    }

    public void setStartHeight(long startHeight) {
        this.startHeight = startHeight;
    }

    public long getEndHeight() {
        return endHeight;
    }

    public void setEndHeight(long endHeight) {
        this.endHeight = endHeight;
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(startHeight);
        stream.writeInt64(endHeight);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        try {
            this.startHeight = byteBuffer.readInt64();
            this.endHeight = byteBuffer.readInt64();
        } catch (Exception e) {
            COMMON_LOG.error("", e);
        }
    }

    @Override
    public String toString() {
        return "HeightRangeMessage{" +
                "startHeight=" + startHeight +
                ", endHeight=" + endHeight +
                '}';
    }
}
