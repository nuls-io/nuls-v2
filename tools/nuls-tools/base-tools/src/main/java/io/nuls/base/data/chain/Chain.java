package io.nuls.base.data.chain;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
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
    private int magicNumber;
    private boolean supportInflowAsset;
    private int minAvailableNodeNum;
    private int singleNodeMinConnectionNum;
    private int txConfirmedBlockNum;
    private List<Seed> seedList;
    private List<Asset> assetList;
    private long createTime;
    private long lastUpdateTime;

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

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public boolean isSupportInflowAsset() {
        return supportInflowAsset;
    }

    public void setSupportInflowAsset(boolean supportInflowAsset) {
        this.supportInflowAsset = supportInflowAsset;
    }

    public int getMinAvailableNodeNum() {
        return minAvailableNodeNum;
    }

    public void setMinAvailableNodeNum(int minAvailableNodeNum) {
        this.minAvailableNodeNum = minAvailableNodeNum;
    }

    public int getSingleNodeMinConnectionNum() {
        return singleNodeMinConnectionNum;
    }

    public void setSingleNodeMinConnectionNum(int singleNodeMinConnectionNum) {
        this.singleNodeMinConnectionNum = singleNodeMinConnectionNum;
    }

    public int getTxConfirmedBlockNum() {
        return txConfirmedBlockNum;
    }

    public void setTxConfirmedBlockNum(int txConfirmedBlockNum) {
        this.txConfirmedBlockNum = txConfirmedBlockNum;
    }

    public List<Seed> getSeedList() {
        return seedList;
    }

    public void setSeedList(List<Seed> seedList) {
        this.seedList = seedList;
    }

    public List<Asset> getAssetList() {
        return assetList;
    }

    public void setAssetList(List<Asset> assetList) {
        this.assetList = assetList;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(chainId);
        stream.writeString(name);
        stream.writeString(addressType);
        stream.writeUint32(magicNumber);
        stream.writeBoolean(supportInflowAsset);
        stream.writeUint32(minAvailableNodeNum);
        stream.writeUint32(singleNodeMinConnectionNum);
        stream.writeUint32(txConfirmedBlockNum);
        stream.writeUint32(seedList.size());
        for (Seed seed : seedList) {
            stream.write(seed.serialize());
        }
        stream.writeUint48(createTime);
        stream.writeUint48(lastUpdateTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readShort();
        this.name = byteBuffer.readString();
        this.addressType = byteBuffer.readString();
        this.magicNumber = byteBuffer.readInt32();
        this.supportInflowAsset = byteBuffer.readBoolean();
        this.minAvailableNodeNum = byteBuffer.readInt32();
        this.singleNodeMinConnectionNum = byteBuffer.readInt32();
        this.txConfirmedBlockNum = byteBuffer.readInt32();
        int seedLength = byteBuffer.readInt32();
        this.seedList = new ArrayList<>();
        for (int i = 0; i < seedLength; i++) {
            this.seedList.add(byteBuffer.readNulsData(new Seed()));
        }
        this.createTime = byteBuffer.readUint48();
        this.lastUpdateTime = byteBuffer.readUint48();
    }

    @Override
    public int size() {
        int size = 0;
        // chainId;
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfString(name);
        size += SerializeUtils.sizeOfString(addressType);
        // magicNumber;
        size += SerializeUtils.sizeOfInt32();
        // supportInflowAsset;
        size += SerializeUtils.sizeOfBoolean(supportInflowAsset);
        // minAvailableNodeNum;
        size += SerializeUtils.sizeOfInt32();
        // singleNodeMinConnectionNum;
        size += SerializeUtils.sizeOfInt32();
        // txConfirmedBlockNum;
        size += SerializeUtils.sizeOfInt32();
        // seed length
        size += SerializeUtils.sizeOfInt32();
        for (Seed seed : seedList) {
            size += seed.size();
        }
        // createTime;
        size += SerializeUtils.sizeOfUint48();
        // lastUpdateTime;
        size += SerializeUtils.sizeOfUint48();
        return size;
    }
}
