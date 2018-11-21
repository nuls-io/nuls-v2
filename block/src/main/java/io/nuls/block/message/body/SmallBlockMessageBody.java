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
import io.nuls.block.model.SmallBlock;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Data;

import java.io.IOException;

/**
 * 广播区块消息
 * 从共识模块接收到新打包区块后，先进行验证，验证通过后保存到数据库并广播到网络上
 * @author captain
 * @date 18-11-9 下午2:37
 * @version 1.0
 */
@Data
public class SmallBlockMessageBody extends MessageBody {

    private int chainID;
    private SmallBlock smallBlock;

    public SmallBlockMessageBody() {
    }

    public SmallBlockMessageBody(int chainID, SmallBlock smallBlock) {
        this.chainID = chainID;
        this.smallBlock = smallBlock;
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfNulsData(smallBlock);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(chainID);
        stream.writeNulsData(smallBlock);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainID = byteBuffer.readInt32();
        this.smallBlock = byteBuffer.readNulsData(new SmallBlock());
    }

}
