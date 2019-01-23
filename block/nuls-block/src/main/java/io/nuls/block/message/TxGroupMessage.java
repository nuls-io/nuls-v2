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
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.block.message.base.BaseMessage;
import io.nuls.tools.basic.VarInt;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取交易组消息
 * 收到其他节点转发的区块时,如果区块中包含的交易在本地没有,则发送此消息批量获取交易
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:37
 */
@Data
public class TxGroupMessage extends BaseMessage {

    private NulsDigestData blockHash;
    private List<Transaction> transactions;

    public TxGroupMessage() {
    }

    public TxGroupMessage(NulsDigestData blockHash, List<Transaction> transactions) {
        this.blockHash = blockHash;
        this.transactions = transactions;
    }

    @Override
    public int size() {
        int size = 0;
        size += blockHash.size();
        size += VarInt.sizeOf(transactions.size());
        size += this.getTxListLength();
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(blockHash);
        stream.writeVarInt(transactions.size());
        for (Transaction data : transactions) {
            stream.writeNulsData(data);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        blockHash = byteBuffer.readHash();
        long txCount = byteBuffer.readVarInt();
        this.transactions = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            try {
                this.transactions.add(byteBuffer.readTransaction());
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
    }

    private int getTxListLength() {
        int size = 0;
        for (Transaction tx : transactions) {
            size += SerializeUtils.sizeOfNulsData(tx);
        }
        return size;
    }

}
