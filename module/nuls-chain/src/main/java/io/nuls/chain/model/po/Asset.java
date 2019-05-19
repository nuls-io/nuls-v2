package io.nuls.chain.model.po;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.chain.model.tx.txdata.TxAsset;
import io.nuls.chain.model.tx.txdata.TxChain;
import io.nuls.chain.util.TimeUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */
public class Asset extends BaseNulsData {
    /**
     * 资产是在哪条链上注册的
     * Which chain is the asset registered on
     */
    private int chainId = 0;

    private int assetId = 0;
    private String symbol;
    private String assetName;
    private BigInteger depositNuls =  BigInteger.ZERO;
    private BigInteger initNumber = BigInteger.ZERO;
    private short decimalPlaces = 8;
    private boolean available = true;
    private long createTime = 0;
    private long lastUpdateTime = 0;
    private byte[] address;
    private String txHash;

    /**
     * 资产流通的链集合
     */
    List<Integer> chainIds = new ArrayList<>();


    public void addChainId(int chainId) {
        chainIds.add(chainId);
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeString(symbol);
        stream.writeString(assetName);
        stream.writeBigInteger(depositNuls);
        stream.writeBigInteger(initNumber);
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
        this.assetName = byteBuffer.readString();
        this.depositNuls = byteBuffer.readBigInteger();
        this.initNumber = byteBuffer.readBigInteger();
        this.decimalPlaces = byteBuffer.readShort();
        this.available = byteBuffer.readBoolean();
        this.createTime = byteBuffer.readUint48();
        this.lastUpdateTime = byteBuffer.readUint48();
        this.address = byteBuffer.readByLengthByte();
        this.txHash = byteBuffer.readString();
        int chainIdSize = (int) byteBuffer.readVarInt();
        if (0 < chainIdSize) {
            for (int i = 0; i < chainIdSize; i++) {
                int chainId = byteBuffer.readUint16();
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
        size += SerializeUtils.sizeOfString(assetName);
        // depositNuls
        size += SerializeUtils.sizeOfBigInteger();
        // initNumber
        size += SerializeUtils.sizeOfBigInteger();
        // decimalPlaces
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfBoolean();
        // createTime
        size += SerializeUtils.sizeOfUint48();
        // lastUpdateTime
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfBytes(address);
        size += SerializeUtils.sizeOfString(txHash);

        size += SerializeUtils.sizeOfVarInt(chainIds == null ? 0 : chainIds.size());
        if (null != chainIds) {
            size += SerializeUtils.sizeOfUint16() * chainIds.size();
        }
        return size;
    }

    public byte[] parseToTransaction() throws IOException {
        TxChain txChain = new TxChain();
        txChain.setAddress(this.getAddress());
        txChain.setAssetId(this.getAssetId());
        txChain.setChainId(this.getChainId());
        txChain.setDecimalPlaces(this.getDecimalPlaces());
        txChain.setDepositNuls(this.getDepositNuls());
        txChain.setInitNumber(this.getInitNumber());
        txChain.setName(this.getAssetName());
        txChain.setSymbol(this.getSymbol());
        return txChain.serialize();
    }

    public Asset(int assetId) {
        this.assetId = assetId;
    }

    public Asset(TxChain tx) {
        this.address = tx.getAddress();
        this.assetId = tx.getAssetId();
        this.chainId = tx.getChainId();
        this.decimalPlaces = tx.getDecimalPlaces();
        this.depositNuls = tx.getDepositNuls();
        this.initNumber = tx.getInitNumber();
        this.symbol = tx.getSymbol();
        this.assetName = tx.getName();
    }

    public Asset(TxAsset tx) {
        this.address = tx.getAddress();
        this.assetId = tx.getAssetId();
        this.chainId = tx.getChainId();
        this.decimalPlaces = tx.getDecimalPlaces();
        this.depositNuls = tx.getDepositNuls();
        this.initNumber = tx.getInitNumber();
        this.symbol = tx.getSymbol();
        this.assetName = tx.getName();
    }

    public Asset() {
        super();
    }

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

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public BigInteger getDepositNuls() {
        return depositNuls;
    }

    public void setDepositNuls(BigInteger depositNuls) {
        this.depositNuls = depositNuls;
    }

    public BigInteger getInitNumber() {
        return initNumber;
    }

    public void setInitNumber(BigInteger initNumber) {
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

    public void map2pojo(Map<String,Object> map){
        this.setChainId(Integer.valueOf(map.get("chainId").toString()));
        this.setAssetId(Integer.valueOf(map.get("assetId").toString()));
        this.setSymbol(String.valueOf(map.get("symbol")));
        this.setAssetName(String.valueOf(map.get("assetName")));
        this.setDecimalPlaces(Short.valueOf(map.get("decimalPlaces").toString()));
        long decimal  =(long) Math.pow(10,Integer.valueOf(this.getDecimalPlaces()));
        BigInteger initNumber =new BigInteger(String.valueOf(map.get("initNumber"))).multiply(
                BigInteger.valueOf(decimal));
        this.setInitNumber(initNumber);
        this.setCreateTime(TimeUtil.getCurrentTime());
        this.setAddress(AddressTool.getAddress(map.get("address").toString()));
    }
}
