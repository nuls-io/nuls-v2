package io.nuls.chain.model.dto;

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

    private int chainId;
    private long assetId;
    private long currentNumber;
    private String chainName;
    private String assetSymbol;
    private String assetName;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
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

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint48(assetId);
        stream.writeInt64(currentNumber);
        stream.writeString(chainName);
        stream.writeString(assetSymbol);
        stream.writeString(assetName);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint48();
        this.currentNumber = byteBuffer.readInt64();
        this.chainName = byteBuffer.readString();
        this.assetSymbol = byteBuffer.readString();
        this.assetName = byteBuffer.readString();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfString(chainName);
        size += SerializeUtils.sizeOfString(assetSymbol);
        size += SerializeUtils.sizeOfString(assetName);
        return size;
    }
}
