package io.nuls.transaction.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.HashUtil;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.transaction.message.base.BaseMessage;

import java.io.IOException;

/**
 * 获取完整交易数据
 *
 * @author: qinyifeng
 * @date: 2018/12/26
 */
public class GetTxMessage extends BaseMessage {
    /**
     * 交易hash
     */
    private byte[] requestHash;

    public byte[] getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(byte[] requestHash) {
        this.requestHash = requestHash;
    }

    @Override
    public int size() {
        int size = 0;
        size += HashUtil.HASH_LENGTH;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(requestHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
    }
}
