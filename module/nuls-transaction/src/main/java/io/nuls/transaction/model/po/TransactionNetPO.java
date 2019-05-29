/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 收到网络广播的交易后，记录发送者，转发时排除发送者
 * @author: Charlie
 * @date: 2019/4/23
 */
public class TransactionNetPO extends BaseNulsData {

    private Transaction tx;

    private String excludeNode;

    private long sendNanoTime;

    public TransactionNetPO() {
    }

    public TransactionNetPO(Transaction tx) {
        this.tx = tx;
    }

    public TransactionNetPO(Transaction tx, String excludeNode, long sendNanoTime) {
        this.tx = tx;
        this.excludeNode = excludeNode;
        this.sendNanoTime = sendNanoTime;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(tx);
        stream.writeString(excludeNode);
        stream.writeInt64(sendNanoTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.tx = byteBuffer.readNulsData(new Transaction());
        this.excludeNode = byteBuffer.readString();
        this.sendNanoTime = byteBuffer.readInt64();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(tx);
        size += SerializeUtils.sizeOfString(excludeNode);
        size += SerializeUtils.sizeOfInt64();
        return size;
    }

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }

    public String getExcludeNode() {
        return excludeNode;
    }

    public void setExcludeNode(String excludeNode) {
        this.excludeNode = excludeNode;
    }

    public long getSendNanoTime() {
        return sendNanoTime;
    }

    public void setSendNanoTime(long sendNanoTime) {
        this.sendNanoTime = sendNanoTime;
    }
}
