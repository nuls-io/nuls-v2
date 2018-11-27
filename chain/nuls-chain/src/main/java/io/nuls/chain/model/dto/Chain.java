package io.nuls.chain.model.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.chain.model.tx.txdata.ChainDestroyTx;
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

    /*删除链时，置位为true*/
    private boolean isDelete = false;
    private long createTime;
    private long lastUpdateTime;
    private byte[] regAddress;
    private String regTxHash;
    private int regAssetId;
    /*删除链伴随的资产信息*/
    private byte[] delAddress;
    private String delTxHash;
    private int delAssetId;

    /*链上创建的资产*/
    List<int> assetIds = new ArrayList();
    /*链上的流通资产*/
    List<String> assetsKey = new ArrayList<>();
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



    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public byte[] getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(byte[] regAddress) {
        this.regAddress = regAddress;
    }

    public String getRegTxHash() {
        return regTxHash;
    }

    public void setRegTxHash(String regTxHash) {
        this.regTxHash = regTxHash;
    }

    public int getRegAssetId() {
        return regAssetId;
    }

    public void setRegAssetId(int regAssetId) {
        this.regAssetId = regAssetId;
    }

    public List<int> getAssetIds() {
        return assetIds;
    }

    public void setAssetIds(List<int> assetIds) {
        this.assetIds = assetIds;
    }

    public byte[] getDelAddress() {
        return delAddress;
    }

    public void setDelAddress(byte[] delAddress) {
        this.delAddress = delAddress;
    }

    public String getDelTxHash() {
        return delTxHash;
    }

    public void setDelTxHash(String delTxHash) {
        this.delTxHash = delTxHash;
    }

    public int getDelAssetId() {
        return delAssetId;
    }

    public void setDelAssetId(int delAssetId) {
        this.delAssetId = delAssetId;
    }

    public  void addCreateAssetId(int assetId){
        assetIds.add(assetId);
    }
    public  void removeCreateAssetId(int paramAssetId){
        int index = -1;
        int i = 0;
        for(int assetId:assetIds){
            if(assetId == paramAssetId){
                index = i;
            }
            i++;
        }
        if(index > 0){
            assetIds.remove(index);
        }
    }
    public  void addCirculateAssetId(String assetKey){
        assetsKey.add(assetKey);
    }
    public void removeCirculateAssetId(String paramAssetsKey){
        int index = -1;
        int i = 0;
        for(String assetKey:assetsKey){
            if(assetKey.equals(paramAssetsKey)){
                index = i;
            }
            i++;
        }
        if(index > 0){
            assetsKey.remove(index);
        }
    }

    public List<String> getAssetsKey() {
        return assetsKey;
    }

    public void setAssetsKey(List<String> assetsKey) {
        this.assetsKey = assetsKey;
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
        stream.writeBoolean(isDelete);
        stream.writeUint48(createTime);
        stream.writeUint48(lastUpdateTime);
        stream.writeBytesWithLength(regAddress);
        stream.writeString(regTxHash);
        stream.writeUint16(regAssetId);
        stream.writeBytesWithLength(delAddress);
        stream.writeString(delTxHash);
        stream.writeUint16(delAssetId);
        int assetSize = (assetIds == null ? 0 : assetIds.size());
        stream.writeVarInt(assetSize);
        if (null != assetIds) {
            for (int assetId : assetIds) {
                stream.writeUint16(assetId);
            }
        }
        int assetsKeySize = (assetsKey == null ? 0 : assetsKey.size());
        stream.writeVarInt(assetsKeySize);
        if (null != assetsKey) {
            for (String assetKey : assetsKey) {
                stream.writeString(assetKey);
            }
        }
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
        this.isDelete = byteBuffer.readBoolean();
        this.createTime = byteBuffer.readUint48();
        this.lastUpdateTime = byteBuffer.readUint48();
        this.regAddress = byteBuffer.readByLengthByte();
        this.regTxHash = byteBuffer.readString();
        this.regAssetId = byteBuffer.readUint16();
        this.delAddress = byteBuffer.readByLengthByte();
        this.delTxHash = byteBuffer.readString();
        this.delAssetId = byteBuffer.readUint16();
        int assetSize = (int)byteBuffer.readVarInt();
        if (0 < assetSize) {
            for (int i = 0; i < assetSize; i++) {
                int  assetId=byteBuffer.readUint16();
                assetIds.add(assetId);
            }
        }

        int assetsKeySize = (int)byteBuffer.readVarInt();
        if (0 < assetsKeySize) {
            for (int i = 0; i < assetsKeySize; i++) {
                String  assetKey=byteBuffer.readString();
                assetsKey.add(assetKey);
            }
        }
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
        // isDelete
        size += SerializeUtils.sizeOfBoolean();
        // createTime;
        size += SerializeUtils.sizeOfUint48();
        // lastUpdateTime;
        size += SerializeUtils.sizeOfUint48();
        size+= SerializeUtils.sizeOfBytes(regAddress);
        //txHash
        size += SerializeUtils.sizeOfString(regTxHash);
        //regAssetId
        size += SerializeUtils.sizeOfInt16();
        size+= SerializeUtils.sizeOfBytes(delAddress);
        //txHash
        size += SerializeUtils.sizeOfString(delTxHash);
        //delAssetId
        size += SerializeUtils.sizeOfInt16();

        size+= SerializeUtils.sizeOfVarInt(assetIds == null ? 0 : assetIds.size());
        if (null != assetIds) {
            for (int assetId : assetIds) {
                size += SerializeUtils.sizeOfUint16();
            }
        }

        size+= SerializeUtils.sizeOfVarInt(assetsKey == null ? 0 : assetsKey.size());
        if (null != assetsKey) {
            for (String assetKey : assetsKey) {
                size += SerializeUtils.sizeOfString(assetKey);
            }
        }
        return size;
    }
    public Chain(){
        super();
    }
    public Chain(ChainTx chainTx,boolean isDelete){
        if(isDelete){
            this.regAddress = chainTx.getAddress();
            this.regAssetId = chainTx.getAssetId();
        }else{
            this.delAddress = chainTx.getAddress();
            this.delAssetId = chainTx.getAssetId();
        }
        this.addressType = chainTx.getAddressType();
        this.chainId = chainTx.getChainId();
        this.magicNumber = chainTx.getMagicNumber();
        this.minAvailableNodeNum = chainTx.getMinAvailableNodeNum();
        this.name =  chainTx.getName();
        this.singleNodeMinConnectionNum = chainTx.getSingleNodeMinConnectionNum();
        this.supportInflowAsset = chainTx.isSupportInflowAsset();

    }
    public byte [] parseToTransaction(Asset asset,boolean isDelete) throws IOException {
        ChainTx chainTx = new ChainTx();

        chainTx.setAddressType(this.addressType);
        chainTx.setChainId(this.chainId);
        chainTx.setMagicNumber(this.magicNumber);
        chainTx.setMinAvailableNodeNum(this.minAvailableNodeNum);
        chainTx.setName(this.name);
        chainTx.setSingleNodeMinConnectionNum(this.singleNodeMinConnectionNum);
        chainTx.setSupportInflowAsset(this.supportInflowAsset);
        chainTx.setAddress(asset.getAddress());

        chainTx.setAssetId(asset.getAssetId());
        chainTx.setSymbol(asset.getSymbol());
        chainTx.setAssetName(asset.getName());
        chainTx.setDepositNuls(asset.getDepositNuls());
        chainTx.setInitNumber(asset.getInitNumber());
        chainTx.setDecimalPlaces(asset.getDecimalPlaces());
        return chainTx.serialize();
    }

}
