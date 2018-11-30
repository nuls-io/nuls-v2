package io.nuls.chain.model.tx.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.basic.TransactionLogicData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.Set;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */
public class TxAsset extends TransactionLogicData {
    private int chainId;
    private int assetId;
    private String symbol;
    private String name;
    private int depositNuls;
    private String initNumber;
    private short decimalPlaces;
    private byte[] address;

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

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint32(assetId);
        stream.writeString(symbol);
        stream.writeString(name);
        stream.writeUint32(depositNuls);
        stream.writeString(initNumber);
        stream.writeShort(decimalPlaces);
        stream.writeBytesWithLength(address);
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
        this.address=byteBuffer.readByLengthByte();
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
        size+=SerializeUtils.sizeOfBytes(address);

        return size;
    }

    @Override
    public Set<byte[]> getAddresses() {
        return null;
    }
}
