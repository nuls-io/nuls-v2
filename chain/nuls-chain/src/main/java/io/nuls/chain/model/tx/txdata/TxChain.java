package io.nuls.chain.model.tx.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.basic.TransactionLogicData;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
@ToString
@NoArgsConstructor
public class TxChain extends TransactionLogicData {
    @Getter
    @Setter
    private int chainId;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String addressType;
    @Getter
    @Setter
    private long magicNumber;
    @Getter
    @Setter
    private boolean supportInflowAsset;
    @Getter
    @Setter
    private int minAvailableNodeNum;
    @Getter
    @Setter
    private int singleNodeMinConnectionNum;
    @Getter
    @Setter
    private byte[] address;

    /**
     * 下面这些是创建链的时候，必须携带的资产信息
     */
    @Getter
    @Setter
    private int assetId;
    @Getter
    @Setter
    private String symbol;
    @Getter
    @Setter
    private String assetName;
    @Getter
    @Setter
    private int depositNuls;
    @Getter
    @Setter
    private BigInteger initNumber;
    @Getter
    @Setter
    private short decimalPlaces;


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeString(name);
        stream.writeString(addressType);
        stream.writeUint32(magicNumber);
        stream.writeBoolean(supportInflowAsset);
        stream.writeUint32(minAvailableNodeNum);
        stream.writeUint32(singleNodeMinConnectionNum);
        stream.writeBytesWithLength(address);
        stream.writeUint16(assetId);
        stream.writeString(symbol);
        stream.writeString(assetName);
        stream.writeUint16(depositNuls);
        stream.write(BigIntegerUtils.toBytes(initNumber));
        stream.writeShort(decimalPlaces);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.name = byteBuffer.readString();
        this.addressType = byteBuffer.readString();
        this.magicNumber = byteBuffer.readUint32();
        this.supportInflowAsset = byteBuffer.readBoolean();
        this.minAvailableNodeNum = byteBuffer.readInt32();
        this.singleNodeMinConnectionNum = byteBuffer.readInt32();
        this.address = byteBuffer.readByLengthByte();
        this.assetId = byteBuffer.readUint16();
        this.symbol = byteBuffer.readString();
        this.assetName = byteBuffer.readString();
        this.depositNuls = byteBuffer.readUint16();
        this.initNumber = BigIntegerUtils.fromBytes(byteBuffer.readBytes(BigIntegerUtils.BIG_INTEGER_LENGTH));
        this.decimalPlaces = byteBuffer.readShort();
    }

    @Override
    public int size() {
        int size = 0;
        // chainId;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(name);
        size += SerializeUtils.sizeOfString(addressType);
        // magicNumber;
        size += SerializeUtils.sizeOfUint32();
        // supportInflowAsset;
        size += SerializeUtils.sizeOfBoolean();
        // minAvailableNodeNum;
        size += SerializeUtils.sizeOfInt32();
        // singleNodeMinConnectionNum;
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfBytes(address);
        //assetId
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(symbol);
        size += SerializeUtils.sizeOfString(assetName);
        //depositNuls
        size += SerializeUtils.sizeOfUint16();
        //initNumber
        size += BigIntegerUtils.BIG_INTEGER_LENGTH;
        //decimalPlaces
        size += SerializeUtils.sizeOfInt16();
        return size;
    }


    @Override
    public Set<byte[]> getAddresses() {
        return null;
    }
}
