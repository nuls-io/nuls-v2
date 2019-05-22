package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.parse.HashUtil;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 主网向发起链节点验证主网协议跨链交易正确性
 * @author tag
 * @date 2019/4/4
 */
public class VerifyCtxMessage extends BaseMessage {
    /**
     * 被请求链跨链交易Hash
     * */
    private byte[] originalCtxHash;
    /**
     * 请求链协议跨链交易Hash
     * */
    private byte[] requestHash;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(originalCtxHash);
        stream.write(requestHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.originalCtxHash = byteBuffer.readHash();
        this.requestHash = byteBuffer.readHash();
    }

    @Override
    public int size() {
        int size = 0;
        size += HashUtil.HASH_LENGTH;
        size += HashUtil.HASH_LENGTH;
        return size;
    }

    public byte[] getOriginalCtxHash() {
        return originalCtxHash;
    }

    public void setOriginalCtxHash(byte[] originalCtxHash) {
        this.originalCtxHash = originalCtxHash;
    }

    public byte[] getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(byte[] requestHash) {
        this.requestHash = requestHash;
    }
}
