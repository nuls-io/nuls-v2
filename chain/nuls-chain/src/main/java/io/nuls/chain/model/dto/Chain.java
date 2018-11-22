package io.nuls.chain.model.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.chain.model.tx.txdata.ChainTx;
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
    private int chainId;
    private String name;
    private String addressType;
    private long magicNumber;
    private boolean supportInflowAsset;
    private int minAvailableNodeNum;
    private int singleNodeMinConnectionNum;
    private int txConfirmedBlockNum;
    private List<Seed> seedList;
    private List<ChainAsset> chainAssetList;
    /**删除链时，置位为true*/
    private boolean isDelete = false;
    private long createTime;
    private long lastUpdateTime;
    private byte[] address;
    private String txHash;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
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

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
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

    public List<ChainAsset> getChainAssetList() {
        return chainAssetList;
    }

    public void setChainAssetList(List<ChainAsset> chainAssetList) {
        this.chainAssetList = chainAssetList;
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

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
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
        stream.writeBoolean(isDelete);
        stream.writeUint48(createTime);
        stream.writeUint48(lastUpdateTime);
        stream.writeBytesWithLength(address);
        stream.writeString(txHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
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
        this.isDelete = byteBuffer.readBoolean();
        this.createTime = byteBuffer.readUint48();
        this.lastUpdateTime = byteBuffer.readUint48();
        this.address = byteBuffer.readByLengthByte();
        this.txHash = byteBuffer.readString();
    }

    @Override
    public int size() {
        int size = 0;
        // chainId;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(name);
        size += SerializeUtils.sizeOfString(addressType);
        // magicNumber;
        size += SerializeUtils.sizeOfInt32();
        // supportInflowAsset;
        size += SerializeUtils.sizeOfBoolean();
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
        // isDelete
        size += SerializeUtils.sizeOfBoolean();
        // createTime;
        size += SerializeUtils.sizeOfUint48();
        // lastUpdateTime;
        size += SerializeUtils.sizeOfUint48();
        size+= SerializeUtils.sizeOfBytes(address);
        //txHash
        size += SerializeUtils.sizeOfString(txHash);
        return size;
    }
    public Chain(){
        super();
    }
    public Chain(ChainTx chainRegTx){
        this.address = chainRegTx.getAddress();
        this.addressType = chainRegTx.getAddressType();
        this.chainId = chainRegTx.getChainId();
        this.magicNumber = chainRegTx.getMagicNumber();
        this.minAvailableNodeNum = chainRegTx.getMinAvailableNodeNum();
        this.name =  chainRegTx.getName();
        this.singleNodeMinConnectionNum = chainRegTx.getSingleNodeMinConnectionNum();
        this.supportInflowAsset = chainRegTx.isSupportInflowAsset();
    }
    public ChainTx parseToTransaction(){
        ChainTx chainRegTx = new ChainTx();
        chainRegTx.setAddress(this.address);
        chainRegTx.setAddressType(this.addressType);
        chainRegTx.setChainId(this.chainId);
        chainRegTx.setMagicNumber(this.magicNumber);
        chainRegTx.setMinAvailableNodeNum(this.minAvailableNodeNum);
        chainRegTx.setName(this.name);
        chainRegTx.setSingleNodeMinConnectionNum(this.singleNodeMinConnectionNum);
        chainRegTx.setSupportInflowAsset(this.supportInflowAsset);
        return chainRegTx;
    }
}
