package io.nuls.chain.model.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.*;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class ChainAsset extends BaseNulsData {

    private int chainId;

    private int assetId;

    private BigInteger initNumber = BigInteger.ZERO;

    private BigInteger inNumber = BigInteger.ZERO;

    private BigInteger outNumber = BigInteger.ZERO;


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeBigInteger(initNumber);
        stream.writeBigInteger(inNumber);
        stream.writeBigInteger(outNumber);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.initNumber = byteBuffer.readBigInteger();
        this.inNumber = byteBuffer.readBigInteger();
        this.outNumber = byteBuffer.readBigInteger();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfBigInteger();

        return size;
    }
}
