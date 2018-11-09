package io.nuls.base.data.chain;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
public class Chain extends BaseNulsData {
    private short chainId;
    private String name;
    private String addressType;
    private List<Asset> assetList;
    private int magicNumber;
    private List<Seed> seedList;
    private boolean assetInflow;

    public short getChainId() {
        return chainId;
    }

    public void setChainId(short chainId) {
        this.chainId = chainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public List<Asset> getAssetList() {
        return assetList;
    }

    public void setAssetList(List<Asset> assetList) {
        this.assetList = assetList;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public List<Seed> getSeedList() {
        return seedList;
    }

    public void setSeedList(List<Seed> seedList) {
        this.seedList = seedList;
    }

    public boolean isAssetInflow() {
        return assetInflow;
    }

    public void setAssetInflow(boolean assetInflow) {
        this.assetInflow = assetInflow;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(chainId);
        stream.writeString(name);
        stream.writeString(addressType);
        for (Asset asset : assetList) {
            stream.write(asset.serialize());
        }
        stream.write(magicNumber);
        for (Seed seed : seedList) {
            stream.write(seed.serialize());
        }
        stream.writeBoolean(assetInflow);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId=byteBuffer.readShort();
        //this.name=byteBuffer.
    }

    @Override
    public int size() {
        int size = 0;
        // chainId
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfString(name);
        size += SerializeUtils.sizeOfString(addressType);
        for (Asset asset : assetList) {
            size += asset.size();
        }
        // magicNumber
        size += SerializeUtils.sizeOfInt32();
        for (Seed seed : seedList) {
            size += seed.size();
        }
        size += SerializeUtils.sizeOfBoolean(assetInflow);
        return size;
    }
}
