package io.nuls.transaction.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseBusinessMessage;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 广播交易(完整交易)
 */
public class BroadcastTxMessage extends BaseBusinessMessage {

    /**
     * 交易
     */
    private Transaction tx;

    /**
     * 交易发送时间, 用于处理孤儿
     */
    private long sendNanoTime;

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }

    public long getSendNanoTime() {
        return sendNanoTime;
    }

    public void setSendNanoTime(long sendNanoTime) {
        this.sendNanoTime = sendNanoTime;
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
        stream.writeInt64(sendNanoTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.tx = byteBuffer.readNulsData(new Transaction());
        this.sendNanoTime = byteBuffer.readInt64();
    }
}
