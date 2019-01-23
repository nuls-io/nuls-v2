/*
 * MIT License
 *
 * Copyright (c) 2018-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * 跨链交易验证结果
 *
 * @author: qinyifeng
 * @date: 2019/01/07
 */
public class CrossTxSignResult extends BaseNulsData {

    /**
     * 节点ID
     */
    @Getter
    @Setter
    private String nodeId;

    /**
     * 验证的节点对交易的签名
     */
    @Getter
    @Setter
    private P2PHKSignature signature;

    /**
     * 节点地址
     */
    @Getter
    @Setter
    private String packingAddress;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(nodeId);
        stream.writeNulsData(signature);
        stream.writeString(packingAddress);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.nodeId = byteBuffer.readString();
        this.signature = byteBuffer.readNulsData(new P2PHKSignature());
        this.packingAddress = byteBuffer.readString();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(nodeId);
        size += SerializeUtils.sizeOfNulsData(signature);
        size += SerializeUtils.sizeOfString(packingAddress);
        return size;
    }
}
