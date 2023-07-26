package io.nuls.crosschain.base.model.bo;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * 资产明细
 * Asset details
 *
 * @author tag
 * 2019/4/15
 * */
public class Circulation extends BaseNulsData {
    private int assetId;
    private BigInteger availableAmount ;
    private BigInteger freeze;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(assetId);
        stream.writeBigInteger(availableAmount);
        stream.writeBigInteger(freeze);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.assetId = byteBuffer.readUint16();
        this.availableAmount = byteBuffer.readBigInteger();
        this.freeze = byteBuffer.readBigInteger();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfBigInteger()*2;
        return size;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assertId) {
        this.assetId = assertId;
    }

    public BigInteger getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigInteger availableAmount) {
        this.availableAmount = availableAmount;
    }

    public BigInteger getFreeze() {
        return freeze;
    }

    public void setFreeze(BigInteger freeze) {
        this.freeze = freeze;
    }
}
