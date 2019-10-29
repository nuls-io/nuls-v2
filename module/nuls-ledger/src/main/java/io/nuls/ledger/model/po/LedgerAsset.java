/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.ledger.model.po;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.model.tx.txdata.TxLedgerAsset;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */
public class LedgerAsset extends BaseNulsData {
    /**
     * 资产是在哪条链上注册的
     * Which chain is the asset registered on
     */
    private int chainId = 0;
    private int assetId = 0;
    private String symbol;
    private String assetName;
    private short assetType = 1;
    private BigInteger destroyNuls = BigInteger.ZERO;
    private BigInteger initNumber = BigInteger.ZERO;
    private short decimalPlace = 8;
    private byte[] assetOwnerAddress;
    private byte[] creatorAddress;
    private String txHash;
    private long createTime = 0;

    public LedgerAsset(TxLedgerAsset tx,
                       int chainId, BigInteger destroyNuls, String txHash, long createTime, byte[] creatorAddress, short assetType) {
        this.chainId = chainId;
        this.assetType = assetType;
        this.symbol = tx.getSymbol();
        this.assetName = tx.getName();
        this.destroyNuls = destroyNuls;
        this.initNumber = tx.getInitNumber();
        this.decimalPlace = tx.getDecimalPlace();
        this.assetOwnerAddress = tx.getAddress();
        this.creatorAddress = creatorAddress;
        this.txHash = txHash;
        this.createTime = createTime;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint8(assetType);
        stream.writeUint16(assetId);
        stream.writeString(symbol);
        stream.writeString(assetName);
        stream.writeBigInteger(destroyNuls);
        stream.writeBigInteger(initNumber);
        stream.writeUint8(decimalPlace);
        stream.writeBytesWithLength(assetOwnerAddress);
        stream.writeBytesWithLength(creatorAddress);
        stream.writeString(txHash);
        stream.writeUint32(createTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetType = byteBuffer.readUint8();
        this.assetId = byteBuffer.readUint16();
        this.symbol = byteBuffer.readString();
        this.assetName = byteBuffer.readString();
        this.destroyNuls = byteBuffer.readBigInteger();
        this.initNumber = byteBuffer.readBigInteger();
        this.decimalPlace = byteBuffer.readUint8();
        this.assetOwnerAddress = byteBuffer.readByLengthByte();
        this.creatorAddress = byteBuffer.readByLengthByte();
        this.txHash = byteBuffer.readString();
        this.createTime = byteBuffer.readUint32();
    }

    @Override
    public int size() {
        int size = 0;
        // chainId
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint8();
        // assetId
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(symbol);
        size += SerializeUtils.sizeOfString(assetName);
        // destroyNuls
        size += SerializeUtils.sizeOfBigInteger();
        // initNumber
        size += SerializeUtils.sizeOfBigInteger();
        // decimalPlaces
        size += SerializeUtils.sizeOfUint8();
        size += SerializeUtils.sizeOfBytes(assetOwnerAddress);
        size += SerializeUtils.sizeOfBytes(creatorAddress);
        size += SerializeUtils.sizeOfString(txHash);
        // createTime
        size += SerializeUtils.sizeOfUint32();

        return size;
    }

    public byte[] parseToTransaction() throws IOException {
        TxLedgerAsset txAsset = new TxLedgerAsset();
        txAsset.setName(this.getAssetName());
        txAsset.setInitNumber(this.getInitNumber());
        txAsset.setDecimalPlace(this.getDecimalPlace());
        txAsset.setSymbol(this.getSymbol());
        txAsset.setAddress(this.getAssetOwnerAddress());
        return txAsset.serialize();
    }

    public LedgerAsset() {
        super();
    }


    public void map2pojo(Map<String, Object> map, short assetType) {
        this.setChainId(Integer.valueOf(map.get("chainId").toString()));
        this.setAssetName(String.valueOf(map.get("assetName")));
        BigInteger initNumber = new BigInteger(String.valueOf(map.get("initNumber")));
        this.setInitNumber(initNumber);
        this.setDecimalPlace(Short.valueOf(map.get("decimalPlace").toString()));
        this.setSymbol(String.valueOf(map.get("assetSymbol")));
        this.setAssetOwnerAddress(AddressTool.getAddress(map.get("address").toString()));
        this.setCreateTime(NulsDateUtils.getCurrentTimeSeconds());
        this.assetType = assetType;

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

    public short getAssetType() {
        return assetType;
    }

    public void setAssetType(short assetType) {
        this.assetType = assetType;
    }

    public BigInteger getDestroyNuls() {
        return destroyNuls;
    }

    public void setDestroyNuls(BigInteger destroyNuls) {
        this.destroyNuls = destroyNuls;
    }

    public BigInteger getInitNumber() {
        return initNumber;
    }

    public void setInitNumber(BigInteger initNumber) {
        this.initNumber = initNumber;
    }

    public short getDecimalPlace() {
        return decimalPlace;
    }

    public void setDecimalPlace(short decimalPlace) {
        this.decimalPlace = decimalPlace;
    }

    public byte[] getAssetOwnerAddress() {
        return assetOwnerAddress;
    }

    public void setAssetOwnerAddress(byte[] assetOwnerAddress) {
        this.assetOwnerAddress = assetOwnerAddress;
    }

    public byte[] getCreatorAddress() {
        return creatorAddress;
    }

    public void setCreatorAddress(byte[] creatorAddress) {
        this.creatorAddress = creatorAddress;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
