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
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 保存已确认交易的数据
 *
 * @author: Charlie
 * @date: 2019/3/12
 */
public class TransactionConfirmedPO extends BaseNulsData {

    private Transaction tx;

    private long blockHeight = -1L;

    private byte status = TxStatusEnum.UNCONFIRM.getStatus();

    /**
     * 交易创建后，第一次被广播到网络中的时间, 发动给其他节点时使用
     */
    private transient long originalSendNanoTime;

    public TransactionConfirmedPO() {
    }

    public TransactionConfirmedPO(Transaction tx, long blockHeight, byte status) {
        this.tx = tx;
        this.blockHeight = blockHeight;
        this.status = status;
    }

    public TransactionConfirmedPO(Transaction tx, long blockHeight, byte status, long originalSendNanoTime) {
        this.tx = tx;
        this.blockHeight = blockHeight;
        this.status = status;
        this.originalSendNanoTime = originalSendNanoTime;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(tx);
        stream.writeUint32(blockHeight);
        stream.write(status);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.tx = byteBuffer.readTransaction();
        this.blockHeight = byteBuffer.readUint32();
        this.status = byteBuffer.readByte();

    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(tx);
        size += SerializeUtils.sizeOfUint32();
        size += 1;
        return size;
    }

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getOriginalSendNanoTime() {
        return originalSendNanoTime;
    }

    public void setOriginalSendNanoTime(long originalSendNanoTime) {
        this.originalSendNanoTime = originalSendNanoTime;
    }
}
