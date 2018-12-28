package io.nuls.block.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.signture.BlockSignature;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 区块头存储对象
 *
 * @author captain
 * @date 18-12-10 下午3:50
 * @version 1.0
 */
@Data
public class BlockHeaderPo extends BaseNulsData {

    private NulsDigestData hash;
    private boolean complete;
    private NulsDigestData preHash;
    private NulsDigestData merkleHash;
    private long time;
    private long height;
    private long txCount;
    private BlockSignature blockSignature;
    private byte[] extend;
    private transient int size;
    private List<NulsDigestData> txHashList;

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBoolean();
        size += SerializeUtils.sizeOfNulsData(hash);
        size += SerializeUtils.sizeOfNulsData(preHash);
        size += SerializeUtils.sizeOfNulsData(merkleHash);
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfBytes(extend);
        for (NulsDigestData hash : txHashList) {
            size += SerializeUtils.sizeOfNulsData(hash);
        }
        size += SerializeUtils.sizeOfNulsData(blockSignature);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBoolean(complete);
        stream.writeNulsData(hash);
        stream.writeNulsData(preHash);
        stream.writeNulsData(merkleHash);
        stream.writeUint48(time);
        stream.writeUint32(height);
        stream.writeUint32(txCount);
        stream.writeBytesWithLength(extend);
        stream.writeNulsData(blockSignature);
        for (NulsDigestData hash : txHashList) {
            stream.writeNulsData(hash);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.complete = byteBuffer.readBoolean();
        this.hash = byteBuffer.readHash();
        this.preHash = byteBuffer.readHash();
        this.merkleHash = byteBuffer.readHash();
        this.time = byteBuffer.readUint48();
        this.height = byteBuffer.readUint32();
        this.txCount = byteBuffer.readUint32();
        this.extend = byteBuffer.readByLengthByte();
        this.txHashList = new ArrayList<>();
        this.blockSignature = byteBuffer.readNulsData(new BlockSignature());
        for (int i = 0; i < txCount; i++) {
            this.txHashList.add(byteBuffer.readHash());
        }
    }

}
