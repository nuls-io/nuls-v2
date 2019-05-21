package io.nuls.crosschain.nuls.model.bo.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 跨链交易的txData
 * @author tag
 * 2019/5/16
 * */
public class CrossTxData extends BaseNulsData {
    /**
     * 发起链链id
     */
    private int chainId;

    /**
     * 原始交易hash
     */
    private NulsDigestData originalTxHash;

    public  CrossTxData (){

    }

    public CrossTxData(NulsDigestData originalTxHash,int chainId){
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
        if(this.chainId != crossTxData.getChainId()){
            return false;
        }
        return crossTxData.getOriginalTxHash().equals(this.originalTxHash);
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeNulsData(originalTxHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.originalTxHash = byteBuffer.readNulsData(originalTxHash);
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint16();
        s += SerializeUtils.sizeOfNulsData(originalTxHash);
        return s;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public NulsDigestData getOriginalTxHash() {
        return originalTxHash;
    }

    public void setOriginalTxHash(NulsDigestData originalTxHash) {
        this.originalTxHash = originalTxHash;
    }
}
