package io.nuls.chain.model.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.chain.model.tx.txdata.AssetTx;
import io.nuls.chain.model.tx.txdata.ChainTx;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */
public class Asset extends BaseNulsData {
    private int chainId;
    private int assetId;
    private String symbol;
    private String name;
    private int depositNuls;
    private String initNumber;
    private short decimalPlaces;
    private boolean available;
    private long createTime;
    private long lastUpdateTime;
    private byte[] address;
    private String txHash;
    /*资产流通的链集合*/
    List<Integer> chainIds = new ArrayList();

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepositNuls() {
        return depositNuls;
    }

    public void setDepositNuls(int depositNuls) {
        this.depositNuls = depositNuls;
    }

    public String getInitNumber() {
        return initNumber;
    }

    public void setInitNumber(String initNumber) {
        this.initNumber = initNumber;
    }

    public short getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(short decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public List<Integer> getChainIds() {
        return chainIds;
    }

    public void setChainIds(List<Integer> chainIds) {
        this.chainIds = chainIds;
    }
    public void addChainId(int chainId){
        chainIds.add(chainId);
    }
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint48(assetId);
        stream.writeString(symbol);
        stream.writeString(name);
        stream.writeUint32(depositNuls);
        stream.writeString(initNumber);
        stream.writeShort(decimalPlaces);
        stream.writeBoolean(available);
        stream.writeUint48(createTime);
        stream.writeUint48(lastUpdateTime);
        stream.writeBytesWithLength(address);
        stream.writeString(txHash);
        int chainIdsSize = (chainIds == null ? 0 : chainIds.size());
        stream.writeVarInt(chainIdsSize);
        if (null != chainIds) {
            for (int chainId : chainIds) {
                stream.writeUint16(chainId);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.symbol = byteBuffer.readString();
        this.name = byteBuffer.readString();
        this.depositNuls = byteBuffer.readInt32();
        this.initNumber = byteBuffer.readString();
        this.decimalPlaces = byteBuffer.readShort();
        this.available = byteBuffer.readBoolean();
        this.createTime = byteBuffer.readUint48();
        this.lastUpdateTime = byteBuffer.readUint48();
        this.address=byteBuffer.readByLengthByte();
        this.txHash = byteBuffer.readString();
        int chainIdSize = (int)byteBuffer.readVarInt();
        if (0 < chainIdSize) {
            for (int i = 0; i < chainIdSize; i++) {
                int  chainId=byteBuffer.readUint16();
                chainIds.add(chainId);
            }
        }

    }

    @Override
    public int size() {
        int size = 0;
        // chainId
        size += SerializeUtils.sizeOfUint16();
        // assetId
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(symbol);
        size += SerializeUtils.sizeOfString(name);
        // depositNuls
        size += SerializeUtils.sizeOfInt32();
        // initNumber
        size += SerializeUtils.sizeOfString(initNumber);
        // decimalPlaces
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfBoolean();
        // createTime
        size += SerializeUtils.sizeOfUint48();
        // lastUpdateTime
        size += SerializeUtils.sizeOfUint48();
        size+=SerializeUtils.sizeOfBytes(address);
        size += SerializeUtils.sizeOfString(txHash);

        size+= SerializeUtils.sizeOfVarInt(chainIds == null ? 0 : chainIds.size());
        if (null != chainIds) {
            for (int chainId : chainIds) {
                size += SerializeUtils.sizeOfUint16();
            }
        }
        return size;
    }
    public byte [] parseToTransaction() throws IOException {
        AssetTx assetTx = new AssetTx();
        assetTx.setAddress(this.getAddress());
        assetTx.setAssetId(this.getAssetId());
        assetTx.setChainId(this.getChainId());
        assetTx.setDecimalPlaces(this.getDecimalPlaces());
        assetTx.setDepositNuls(this.getDepositNuls());
        assetTx.setInitNumber(this.getInitNumber());
        assetTx.setName(this.getName());
        assetTx.setSymbol(this.getSymbol());
        return assetTx.serialize();
    }
    public Asset(int assetId){
        this.assetId = assetId;
    }
    public Asset(AssetTx tx){
        this.address = tx.getAddress();
        this.assetId = tx.getAssetId();
        this.chainId = tx.getChainId();
        this.decimalPlaces = tx.getDecimalPlaces();
        this.depositNuls = tx.getDepositNuls();
        this.initNumber = tx.getInitNumber();
        this.symbol = tx.getSymbol();
        this.name = tx.getName();
    }
    public Asset(ChainTx tx){
        this.address = tx.getAddress();
        this.assetId = tx.getAssetId();
        this.chainId = tx.getChainId();
        this.decimalPlaces = tx.getDecimalPlaces();
        this.depositNuls = tx.getDepositNuls();
        this.initNumber = tx.getInitNumber();
        this.symbol = tx.getSymbol();
        this.name = tx.getName();
    }
    public Asset(){
        super();
    }
}
