package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.crosschain.base.message.base.BaseMessage;

import java.io.IOException;
import java.util.Arrays;

/**
 * 广播跨链交易Hash给链接到的链内其他节点
 * @author tag
 * @date 2019/4/4
 */
public class BroadCtxSignMessage extends BaseMessage {
    private NulsHash originalHash;
    private NulsHash requestHash;
    private byte[] signature;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(originalHash);
        stream.writeNulsData(requestHash);
        stream.writeBytesWithLength(signature);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.originalHash = byteBuffer.readHash();
        this.requestHash = byteBuffer.readHash();
        this.signature = byteBuffer.readByLengthByte();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(originalHash);
        size += SerializeUtils.sizeOfNulsData(requestHash);
        size += SerializeUtils.sizeOfBytes(signature);
        return size;
    }

    public NulsHash getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(NulsHash requestHash) {
        this.requestHash = requestHash;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public NulsHash getOriginalHash() {
        return originalHash;
    }

    public void setOriginalHash(NulsHash originalHash) {
        this.originalHash = originalHash;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BroadCtxSignMessage)) {
            return false;
        }
        return Arrays.equals(this.getSignature(), ((BroadCtxSignMessage) obj).getSignature());
    }
}
