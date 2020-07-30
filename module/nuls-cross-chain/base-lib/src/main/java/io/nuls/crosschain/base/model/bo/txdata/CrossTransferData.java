package io.nuls.crosschain.base.model.bo.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsHash;
import io.nuls.core.constant.TxType;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.parse.SerializeUtils;
import org.bouncycastle.util.Integers;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.nio.Buffer;

/**
 * @Author: zhoulijun
 * @Time: 2020/7/30 13:38
 * @Description: 功能描述
 */
public class CrossTransferData extends BaseNulsData {

    Integer sourceType;

    NulsHash sourceHash;

    NulsHash hubHash;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(sourceType);
        stream.write(sourceHash.getBytes());
        stream.write(hubHash.getBytes());
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        try {
            if (!buffer.isFinished()){
                this.sourceType = buffer.readInt32();
            }
            if (!buffer.isFinished()){
                this.sourceHash = buffer.readHash();
            }
            if (!buffer.isFinished()){
                this.hubHash = buffer.readHash();
            }
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint32();
        s += SerializeUtils.sizeOfBytes(sourceHash.getBytes());
        s += SerializeUtils.sizeOfBytes(hubHash.getBytes());
        return s;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public NulsHash getSourceHash() {
        return sourceHash;
    }

    public void setSourceHash(NulsHash sourceHash) {
        this.sourceHash = sourceHash;
    }

    public NulsHash getHubHash() {
        return hubHash;
    }

    public void setHubHash(NulsHash hubHash) {
        this.hubHash = hubHash;
    }

    public static void main(String[] args) throws NulsException {
        CrossTransferData crossTransferData = new CrossTransferData();
        byte[] byts = ByteUtils.intToBytes(TxType.CONTRACT_TOKEN_CROSS_TRANSFER);
        crossTransferData.parse(byts,0);
        Log.info("{}",crossTransferData.sourceType);
        crossTransferData = new CrossTransferData();
        crossTransferData.setSourceType(26);
        crossTransferData.setHubHash(NulsHash.fromHex("3bb11b164b14f09362a3b7ad7020257fadf91a6ac0b4af81cd284e6ce178a117"));
        Log.info("{}",crossTransferData.sourceType);
        Log.info("{}",crossTransferData.hubHash.toHex());
        Log.info("{}",crossTransferData.sourceHash.toHex());
     }

}
