package io.nuls.transaction.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.transaction.message.base.BaseMessage;

import java.io.IOException;

/**
 * 处理完来自其他节点的交易时，再转发出去
 * 转发来自其他节点的交易
 * 转发交易hash
 * @author: Charlie
 * @date: 2019/04/17
 */
public class ForwardTxMessage extends BaseMessage {
    /**
     * 交易hash
     */
    private NulsHash hash;

    @Override
    public NulsHash getHash() {
        return hash;
    }

    @Override
    public void setHash(NulsHash hash) {
        this.hash = hash;
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(hash.getBytes());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.hash = byteBuffer.readHash();
    }
}
