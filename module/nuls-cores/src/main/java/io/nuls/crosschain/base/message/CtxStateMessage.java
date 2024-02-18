package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * The processing results of initiating link collection cross chain transactions in the main chain/Main network receiving and receiving chain processing results
 * @author tag
 * @date 2019/4/4
 */
public class CtxStateMessage extends BaseMessage {
    private NulsHash requestHash;
    /**
     * 0Unconfirmed 1Main network confirmed 2Receiving chain confirmed
     */
    private byte handleResult;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(requestHash.getBytes());
        stream.writeByte(handleResult);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
        this.handleResult = byteBuffer.readByte();
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        size += 1;
        return size;
    }


    public NulsHash getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(NulsHash requestHash) {
        this.requestHash = requestHash;
    }

    public byte getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(byte handleResult) {
        this.handleResult = handleResult;
    }
}
