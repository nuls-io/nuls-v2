package io.nuls.base.data.chain;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
public class ChainAsset extends BaseNulsData {

    private short chainId;
    private long assetId;
    private long currentNumber;

    public short getChainId() {
        return chainId;
    }

    public void setChainId(short chainId) {
        this.chainId = chainId;
    }

    public long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }

    public long getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(long currentNumber) {
        this.currentNumber = currentNumber;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(chainId);
        stream.writeUint48(assetId);
        stream.writeInt64(currentNumber);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readShort();
        this.assetId = byteBuffer.readUint48();
        this.currentNumber = byteBuffer.readInt64();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfInt64();
        return size;
    }
}
