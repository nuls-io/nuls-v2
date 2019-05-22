package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 发起链接收跨链交易在主链的处理结果/主网接收接收链处理结果
 * @author tag
 * @date 2019/4/4
 */
public class CtxStateMessage extends BaseMessage {
    private NulsHash requestHash;

    private boolean handleResult;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(requestHash);
        stream.writeBoolean(handleResult);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
        this.handleResult = byteBuffer.readBoolean();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(requestHash);
        size += SerializeUtils.sizeOfBoolean();
        return size;
    }


    public NulsHash getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(NulsHash requestHash) {
        this.requestHash = requestHash;
    }

    public boolean isHandleResult() {
        return handleResult;
    }

    public void setHandleResult(boolean handleResult) {
        this.handleResult = handleResult;
    }
}
