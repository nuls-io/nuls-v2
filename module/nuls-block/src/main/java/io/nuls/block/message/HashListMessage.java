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
import io.nuls.base.data.NulsHash;
import io.nuls.core.basic.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量请求交易消息
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:37
 */
public class HashListMessage extends BaseBusinessMessage {

    private NulsHash blockHash;

    private List<NulsHash> txHashList = new ArrayList<>();

    public NulsHash getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(NulsHash blockHash) {
        this.blockHash = blockHash;
    }

    public List<NulsHash> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<NulsHash> txHashList) {
        this.txHashList = txHashList;
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        size += VarInt.sizeOf(txHashList.size());
        size += this.getTxHashBytesLength();
        return size;
    }

    private int getTxHashBytesLength() {
        int size = 0;
        size += txHashList.size() * NulsHash.HASH_LENGTH;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(blockHash.getBytes());
        stream.writeVarInt(txHashList.size());
        for (NulsHash data : txHashList) {
            stream.write(data.getBytes());
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.blockHash = byteBuffer.readHash();
        long txCount = byteBuffer.readVarInt();
        this.txHashList = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            this.txHashList.add(byteBuffer.readHash());
        }
    }

}
