package io.nuls.transaction.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseBusinessMessage;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 发送完整交易的消息
 */
public class BroadcastTxMessage extends BaseBusinessMessage {

    /**
     * 交易
     */
    private Transaction tx;

    /**
     * 交易创建后，第一次被广播到网络中的时间, 用于处理孤儿
     */
    private long originalSendNanoTime;

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }

    public long getOriginalSendNanoTime() {
        return originalSendNanoTime;
    }

    public void setOriginalSendNanoTime(long originalSendNanoTime) {
        this.originalSendNanoTime = originalSendNanoTime;
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(tx);
        size += SerializeUtils.sizeOfInt64();
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(tx);
        stream.writeInt64(originalSendNanoTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.tx = byteBuffer.readNulsData(new Transaction());
        this.originalSendNanoTime = byteBuffer.readInt64();
    }
}
