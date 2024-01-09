package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
/**
 * 跨链间跨链交易消息
 * @author tag
 * @date 2019/4/4
 */
public class NewOtherCtxMessage extends BaseMessage {
    /**
     * 被请求链协议的跨链交易
     * */
    private Transaction ctx;
    /**
     * 被请求链协议跨链交易Hash
     * */
    private NulsHash requestHash;
    @Override
    public int size() {
        int size = 0;
        //tx
        size += SerializeUtils.sizeOfNulsData(ctx);
        size += NulsHash.HASH_LENGTH;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(ctx);
        stream.write(requestHash.getBytes());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.ctx = byteBuffer.readNulsData(new Transaction());
        this.requestHash = byteBuffer.readHash();
    }

    public Transaction getCtx() {
        return ctx;
    }

    public void setCtx(Transaction ctx) {
        this.ctx = ctx;
    }

    public NulsHash getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(NulsHash requestHash) {
        this.requestHash = requestHash;
    }
}
