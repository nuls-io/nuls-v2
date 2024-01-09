package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.crosschain.base.message.base.BaseMessage;

import java.io.IOException;

/**
 * @Author: zhoulijun
 * @Time: 2020/9/11 11:16
 * @Description: Message requiring all nodes to reprocess specified cross chain transactions
 *
 */
public class CrossTxRehandleMessage extends BaseMessage {

    /**
     * Local protocol transactions for cross chain transactionshash
     * */
    private NulsHash ctxHash;

    /**
     * The height at which the broadcast was initiated
     */
    private long blockHeight;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(ctxHash.getBytes());
        stream.writeInt64(blockHeight);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.ctxHash = byteBuffer.readHash();
        this.blockHeight = byteBuffer.readInt64();
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        size += SerializeUtils.sizeOfInt64();
        return size;
    }

    public NulsHash getCtxHash() {
        return ctxHash;
    }

    public void setCtxHash(NulsHash ctxHash) {
        this.ctxHash = ctxHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }
}
