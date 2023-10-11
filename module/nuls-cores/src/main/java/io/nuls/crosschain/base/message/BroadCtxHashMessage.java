package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.message.base.BaseMessage;

import java.io.IOException;

/**
 * 广播跨链交易Hash给链接到的主网节点
 * @author tag
 * @date 2019/4/4
 */
public class BroadCtxHashMessage extends BaseMessage{
    private NulsHash convertHash;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(convertHash.getBytes());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.convertHash = byteBuffer.readHash();

    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        return size;
    }

    public NulsHash getConvertHash() {
        return convertHash;
    }

    public void setConvertHash(NulsHash convertHash) {
        this.convertHash = convertHash;
    }
}
