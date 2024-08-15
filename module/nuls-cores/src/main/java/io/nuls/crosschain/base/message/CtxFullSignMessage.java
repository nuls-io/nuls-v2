package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.crosschain.base.message.base.BaseMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @description TODO
 * @date 2024/6/17 14:09
 * @COPYRIGHT nabox.io
 */
public class CtxFullSignMessage extends BaseMessage {

    private NulsHash localTxHash;

    private byte[] transactionSignature;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(localTxHash.getBytes());
        stream.writeBytesWithLength(transactionSignature);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.localTxHash = byteBuffer.readHash();
        this.transactionSignature = byteBuffer.readByLengthByte();
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        size += SerializeUtils.sizeOfBytes(transactionSignature);
        return size;
    }


    public byte[] getTransactionSignature() {
        return transactionSignature;
    }

    public void setTransactionSignature(byte[] transactionSignature) {
        this.transactionSignature = transactionSignature;
    }

    public NulsHash getLocalTxHash() {
        return localTxHash;
    }

    public void setLocalTxHash(NulsHash localTxHash) {
        this.localTxHash = localTxHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CtxFullSignMessage that = (CtxFullSignMessage) o;
        return Objects.equals(localTxHash, that.localTxHash) && Arrays.equals(transactionSignature, that.transactionSignature);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(localTxHash);
        result = 31 * result + Arrays.hashCode(transactionSignature);
        return result;
    }
}
