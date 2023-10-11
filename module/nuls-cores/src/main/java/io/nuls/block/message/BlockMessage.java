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
import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 完整的区块消息
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:08
 */
public class BlockMessage extends BaseBusinessMessage {
    /**
     * 用来区分批量获取区块请求和单个区块请求,也可以用来过滤非法消息
     */
    private NulsHash requestHash;
    /**
     * 区块数据
     */
    private Block block;

    /**
     * 是否同步中下载的区块
     */
    private boolean syn;

    public NulsHash getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(NulsHash requestHash) {
        this.requestHash = requestHash;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public BlockMessage(NulsHash requestHash, Block block, boolean syn) {
        this.requestHash = requestHash;
        this.block = block;
        this.syn = syn;
    }

    public boolean isSyn() {
        return syn;
    }

    public BlockMessage() {
    }

    public void setSyn(boolean syn) {
        this.syn = syn;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer buffer) throws IOException {
        buffer.write(requestHash.getBytes());
        buffer.writeNulsData(block);
        buffer.writeBoolean(syn);
    }

    @Override
    public void parse(NulsByteBuffer nulsByteBuffer) throws NulsException {
        this.requestHash = nulsByteBuffer.readHash();
        this.block = nulsByteBuffer.readNulsData(new Block());
        this.syn = nulsByteBuffer.readBoolean();
    }

    @Override
    public int size() {
        return NulsHash.HASH_LENGTH + SerializeUtils.sizeOfNulsData(block) + SerializeUtils.sizeOfBoolean();
    }

}
