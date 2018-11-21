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

package io.nuls.block.message.body;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Block;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Data;

import java.io.IOException;

/**
 * 完整的区块消息
 * @author captain
 * @date 18-11-20 上午11:08
 * @version 1.0
 */
@Data
public class BlockMessageBody extends MessageBody {

    private int chainID;
    private Block block;

    public BlockMessageBody() {
    }

    public BlockMessageBody(int chainID, Block block) {
        this.chainID = chainID;
        this.block = block;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer buffer) throws IOException {
        buffer.writeUint32(chainID);
        buffer.writeNulsData(block);
    }

    @Override
    public void parse(NulsByteBuffer nulsByteBuffer) throws NulsException {
        this.chainID = nulsByteBuffer.readInt32();
        this.block = nulsByteBuffer.readNulsData(new Block());
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfNulsData(block);
        return size;
    }

}
