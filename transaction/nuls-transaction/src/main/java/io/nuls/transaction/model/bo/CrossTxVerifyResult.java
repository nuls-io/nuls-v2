package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * 跨链交易验证结果
 *
 * @author: qinyifeng
 * @date: 2018/12/28
 */
public class CrossTxVerifyResult extends BaseNulsData {

    /**
     * 链ID
     */
    @Getter
    @Setter
    private int chainId;
    /**
     * 节点ID
     */
    @Getter
    @Setter
    private String nodeId;

    /**
     * 确认高度
     */
    @Getter
    @Setter
    private long height;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(chainId);
        stream.writeString(nodeId);
        stream.writeVarInt(height);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.nodeId = byteBuffer.readString();
        this.height = byteBuffer.readVarInt();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(nodeId);
        size += SerializeUtils.sizeOfVarInt(height);
        return size;
    }
}
