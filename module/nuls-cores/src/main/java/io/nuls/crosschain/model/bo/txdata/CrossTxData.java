package io.nuls.crosschain.model.bo.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 跨链交易的txData
 *
 * @author tag
 * 2019/5/16
 */
public class CrossTxData extends BaseNulsData {
    /**
     * 发起链链id
     */
    private int chainId;

    /**
     * 原始交易hash
     */
    private NulsHash originalTxHash;

    public CrossTxData() {

    }

    public CrossTxData(NulsHash originalTxHash, int chainId) {
        this.originalTxHash = originalTxHash;
        this.chainId = chainId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CrossTxData)) {
            return false;
        }
        CrossTxData crossTxData = ((CrossTxData) obj);
        if (this.chainId != crossTxData.getChainId()) {
            return false;
        }
        return crossTxData.getOriginalTxHash().equals(this.originalTxHash);
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.write(originalTxHash.getBytes());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.originalTxHash = byteBuffer.readHash();
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint16();
        s += NulsHash.HASH_LENGTH;
        return s;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public NulsHash getOriginalTxHash() {
        return originalTxHash;
    }

    public void setOriginalTxHash(NulsHash originalTxHash) {
        this.originalTxHash = originalTxHash;
    }
}
