package io.nuls.chain.model.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */

@ToString
@NoArgsConstructor
public class ChainAsset extends BaseNulsData {

    @Getter
    @Setter
    private int chainId;
    @Getter
    @Setter
    private int assetId;
    @Getter
    @Setter
    private BigInteger initNumber = BigInteger.ZERO;
    @Getter
    @Setter
    private BigInteger inNumber = BigInteger.ZERO;
    @Getter
    @Setter
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
        size += BigIntegerUtils.BIG_INTEGER_LENGTH;
        size += BigIntegerUtils.BIG_INTEGER_LENGTH;
        size += BigIntegerUtils.BIG_INTEGER_LENGTH;

        return size;
    }
}
