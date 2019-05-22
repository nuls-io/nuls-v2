package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.parse.HashUtil;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 跨链交易验证结果
 * @author tag
 * @date 2019/4/4
 */
public class VerifyCtxResultMessage extends BaseMessage {
    /**
     * 请求链协议跨链交易Hash
     * */
    private byte[] requestHash;
    private boolean verifyResult;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(requestHash);
        stream.writeBoolean(verifyResult);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
        this.verifyResult = byteBuffer.readBoolean();
    }

    @Override
    public int size() {
        int size = 0;
        size += HashUtil.HASH_LENGTH;
        size += SerializeUtils.sizeOfBoolean();
        return size;
    }

    public byte[] getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(byte[] requestHash) {
        this.requestHash = requestHash;
    }

    public boolean isVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(boolean verifyResult) {
        this.verifyResult = verifyResult;
    }
}
