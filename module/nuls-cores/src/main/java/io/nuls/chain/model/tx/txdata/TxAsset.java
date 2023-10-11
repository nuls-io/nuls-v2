package io.nuls.chain.model.tx.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */

public class TxAsset extends BaseNulsData {
    private int chainId;
    private int assetId;
    private String symbol;
    private String name;
    private BigInteger depositNuls;
    private BigInteger destroyNuls;
    private BigInteger initNumber;
    private short decimalPlaces;
    private byte[] address;


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeString(symbol);
        stream.writeString(name);
        stream.writeBigInteger(depositNuls);
        stream.writeBigInteger(destroyNuls);
        stream.writeBigInteger(initNumber);
        stream.writeShort(decimalPlaces);
        stream.writeBytesWithLength(address);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.symbol = byteBuffer.readString();
        this.name = byteBuffer.readString();
        this.depositNuls = byteBuffer.readBigInteger();
        this.destroyNuls = byteBuffer.readBigInteger();
        this.initNumber = byteBuffer.readBigInteger();
        this.decimalPlaces = byteBuffer.readShort();
        this.address = byteBuffer.readByLengthByte();
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
        size += SerializeUtils.sizeOfBigInteger();
        // destroyNuls
        size += SerializeUtils.sizeOfBigInteger();
        // initNumber
        size += SerializeUtils.sizeOfBigInteger();
        // decimalPlaces
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfBytes(address);

        return size;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public BigInteger getDestroyNuls() {
        return destroyNuls;
    }

    public void setDestroyNuls(BigInteger destroyNuls) {
        this.destroyNuls = destroyNuls;
    }
}
