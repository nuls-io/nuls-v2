package io.nuls.crosschain.base.model.bo.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsHash;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * @Author: zhoulijun
 * @Time: 2020/7/30 13:38
 * @Description: 功能描述
 */
public class CrossTransferData extends BaseNulsData {

    Integer sourceType;

    byte[] hubHash;

    byte[] sourceHash;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        if (sourceType == null) {
            stream.writeUint32(10);
        } else {
            stream.writeUint32(sourceType);
        }
        stream.writeBytesWithLength(hubHash);
        stream.writeBytesWithLength(sourceHash);
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        try {
            if (!buffer.isFinished()) {
                this.sourceType = buffer.readInt32();
            }
            if (!buffer.isFinished()) {
                this.hubHash = buffer.readByLengthByte();
            }
            if (!buffer.isFinished()) {
                this.sourceHash = buffer.readByLengthByte();
            }
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint32();
        s += SerializeUtils.sizeOfBytes(sourceHash);
        s += SerializeUtils.sizeOfBytes(hubHash);
        return s;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public byte[] getSourceHash() {
        return sourceHash;
    }

    public void setSourceHash(byte[] sourceHash) {
        this.sourceHash = sourceHash;
    }

    public byte[] getHubHash() {
        return hubHash;
    }

    public void setHubHash(byte[] hubHash) {
        this.hubHash = hubHash;
    }

    public static void main(String[] args) throws NulsException, IOException {
        CrossTransferData crossTransferData = new CrossTransferData();
        byte[] byts = ByteUtils.intToBytes(TxType.CONTRACT_TOKEN_CROSS_TRANSFER);
        crossTransferData.parse(byts, 0);
        Log.info("hex:{}", HexUtil.encode(crossTransferData.serialize()));
        Log.info("{}", crossTransferData.sourceType);
        crossTransferData = new CrossTransferData();
        crossTransferData.setSourceType(26);
        crossTransferData.setSourceHash(HexUtil.decode("792dc5108df2f3ab3575cff3cd1f1cfd7137ecbd05a813b6c255260e38c4d36c"));
        crossTransferData.setHubHash(HexUtil.decode("3bb11b164b14f09362a3b7ad7020257fadf91a6ac0b4af81cd284e6ce178a117"));
        byte[] hex = crossTransferData.serialize();
        crossTransferData = new CrossTransferData();
        crossTransferData.parse(hex,0);
        Log.info("{}", crossTransferData.sourceType);
        if(crossTransferData.hubHash != null){
            Log.info("hubHash:{}", HexUtil.encode(crossTransferData.hubHash));
        }
        if(crossTransferData.sourceHash != null){
            Log.info("sourceHash:{}", HexUtil.encode(crossTransferData.sourceHash));
        }
    }

}
