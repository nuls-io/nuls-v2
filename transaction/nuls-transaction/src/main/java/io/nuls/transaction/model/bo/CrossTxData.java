/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * 跨链交易的txData
 * @author: Charlie
 * @date: 2018/11/22
 */
public class CrossTxData extends BaseNulsData {

    /**
     * 发起链链id
     */
    private int chainId;

    /**
     * 原始交易hash
     */
    private byte[] originalTxHash;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(chainId);
        stream.writeBytesWithLength(originalTxHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.originalTxHash = byteBuffer.readByLengthByte();
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint16();
        s += SerializeUtils.sizeOfBytes(originalTxHash);
        return s;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public byte[] getOriginalTxHash() {
        return originalTxHash;
    }

    public void setOriginalTxHash(byte[] originalTxHash) {
        this.originalTxHash = originalTxHash;
    }
}
