package io.nuls.base.data.chain;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */
public class Asset extends BaseNulsData {
    private short chainId;
    private short assetId;
    private String symbol;
    private String name;
    private int depositNuls;
    private long initNumber;
    private long currentNumber;
    private short decimalPlaces;
    private boolean available;
    private long createTime;
    private long lastUpdateTime;

    public short getChainId() {
        return chainId;
    }

    public void setChainId(short chainId) {
        this.chainId = chainId;
    }

    public short getAssetId() {
        return assetId;
    }

    public void setAssetId(short assetId) {
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

    public long getInitNumber() {
        return initNumber;
    }

    public void setInitNumber(long initNumber) {
        this.initNumber = initNumber;
    }

    public long getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(long currentNumber) {
        this.currentNumber = currentNumber;
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

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(chainId);
        stream.writeShort(assetId);
        stream.writeString(symbol);
        stream.writeString(name);
        stream.writeUint32(depositNuls);
        stream.writeInt64(initNumber);
        stream.writeInt64(currentNumber);
        stream.writeShort(decimalPlaces);
        stream.writeBoolean(available);
        stream.writeUint48(createTime);
        stream.writeUint48(lastUpdateTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readShort();
        this.assetId = byteBuffer.readShort();
        this.symbol = byteBuffer.readString();
        this.name = byteBuffer.readString();
        this.depositNuls = byteBuffer.readInt32();
        this.initNumber = byteBuffer.readInt64();
        this.currentNumber = byteBuffer.readInt64();
        this.decimalPlaces = byteBuffer.readShort();
        this.available = byteBuffer.readBoolean();
        this.createTime = byteBuffer.readUint48();
        this.lastUpdateTime = byteBuffer.readUint48();
    }

    @Override
    public int size() {
        int size = 0;
        // chainId
        size += SerializeUtils.sizeOfInt16();
        // assetId
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfString(symbol);
        size += SerializeUtils.sizeOfString(name);
        // depositNuls
        size += SerializeUtils.sizeOfInt32();
        // initNumber
        size += SerializeUtils.sizeOfInt64();
        // currentNumber
        size += SerializeUtils.sizeOfInt64();
        // decimalPlaces
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfBoolean(available);
        // createTime
        size += SerializeUtils.sizeOfUint48();
        // lastUpdateTime
        size += SerializeUtils.sizeOfUint48();

        return size;
    }
}
