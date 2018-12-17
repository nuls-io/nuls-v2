/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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
import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.message.base.BaseMessage;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.*;

import java.io.IOException;

/**
 * 完整的区块消息
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:08
 */
@NoArgsConstructor
@AllArgsConstructor
public class BlockMessage extends BaseMessage {
    /**
     * 用来区分批量获取区块请求和单个区块请求，也可以用来过滤非法消息
     */
    @Getter
    private NulsDigestData requestHash;
    /**
     * 区块数据
     */
    @Getter @Setter
    private Block block;

    @Override
    public void serializeToStream(NulsOutputStreamBuffer buffer) throws IOException {
        buffer.writeNulsData(requestHash);
        buffer.writeNulsData(block);
    }

    @Override
    public void parse(NulsByteBuffer nulsByteBuffer) throws NulsException {
        this.requestHash = nulsByteBuffer.readHash();
        this.block = nulsByteBuffer.readNulsData(new Block());
    }

    @Override
    public int size() {
        return SerializeUtils.sizeOfNulsData(requestHash) + SerializeUtils.sizeOfNulsData(block);
    }

}
