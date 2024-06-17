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
public class CxtFullSignMessage extends BaseMessage {

    private NulsHash txHash;

    private byte[] transactionSignature;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(txHash.getBytes());
        stream.writeBytesWithLength(transactionSignature);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.txHash = byteBuffer.readHash();
        this.transactionSignature = byteBuffer.readByLengthByte();
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        size += SerializeUtils.sizeOfBytes(transactionSignature);
        return size;
    }

    public NulsHash getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsHash txHash) {
        this.txHash = txHash;
    }

    public byte[] getTransactionSignature() {
        return transactionSignature;
    }

    public void setTransactionSignature(byte[] transactionSignature) {
        this.transactionSignature = transactionSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CxtFullSignMessage that = (CxtFullSignMessage) o;
        return Objects.equals(txHash, that.txHash) && Arrays.equals(transactionSignature, that.transactionSignature);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(txHash);
        result = 31 * result + Arrays.hashCode(transactionSignature);
        return result;
    }
}
