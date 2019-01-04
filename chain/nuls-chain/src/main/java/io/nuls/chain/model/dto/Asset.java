package io.nuls.chain.model.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.chain.model.tx.txdata.TxAsset;
import io.nuls.chain.model.tx.txdata.TxChain;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class Asset extends BaseNulsData {
    /**
     * 资产是在哪条链上注册的
     * Which chain is the asset registered on
     */
    private int chainId;

    private int assetId;
    private String symbol;
    private String assetName;
    private int depositNuls;
    private BigInteger initNumber;
    private short decimalPlaces;
    private boolean available;
    private long createTime;
    private long lastUpdateTime;
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
        stream.writeUint48(assetId);
        stream.writeString(symbol);
        stream.writeString(assetName);
        stream.writeUint32(depositNuls);
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
        this.depositNuls = byteBuffer.readInt32();
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
        size += SerializeUtils.sizeOfInt32();
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
}
