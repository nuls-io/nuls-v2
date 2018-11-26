package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.basic.TransactionLogicData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.Set;

/**
 * 跨链交易的txData
 * @author: Charlie
 * @date: 2018/11/22
 */
public class CrossTxData extends TransactionLogicData {

    /**
     * 发起链链id
     */
    private int chainId;

    /**
     * 原始交易hash
     */
    private byte[] originalTxHash;


    @Override
    public Set<byte[]> getAddresses() {
        return null;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(chainId);
        stream.write(originalTxHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.originalTxHash = byteBuffer.readByLengthByte();
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint16();
        s += SerializeUtils.sizeOfBytes(originalTxHash);
        return s;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public byte[] getOriginalTxHash() {
        return originalTxHash;
    }

    public void setOriginalTxHash(byte[] originalTxHash) {
        this.originalTxHash = originalTxHash;
    }
}
