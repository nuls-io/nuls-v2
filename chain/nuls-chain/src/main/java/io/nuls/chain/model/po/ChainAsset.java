package io.nuls.chain.model.po;

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
    /**
     *资产所在链
     */
    private int addressChainId;
    /**
     *资产产生链
     */
    private int assetChainId;
    /**
     *资产id
     */
    private int assetId;
    /**
     * 初始值,也可以动态由查询来进行变更
     */
    private BigInteger initNumber = BigInteger.ZERO;
    /**
     * 入账，增加资产值
     */
    private BigInteger inNumber = BigInteger.ZERO;
    /**
     * 出账，减少资产值
     */
    private BigInteger outNumber = BigInteger.ZERO;


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(addressChainId);
        stream.writeUint16(assetChainId);
        stream.writeUint16(assetId);
        stream.writeBigInteger(initNumber);
        stream.writeBigInteger(inNumber);
        stream.writeBigInteger(outNumber);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.addressChainId = byteBuffer.readUint16();
        this.assetChainId = byteBuffer.readUint16();
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
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfBigInteger();

        return size;
    }
}
