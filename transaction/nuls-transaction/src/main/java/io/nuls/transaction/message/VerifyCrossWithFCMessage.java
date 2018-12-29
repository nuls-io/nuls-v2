package io.nuls.transaction.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.message.base.BaseMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * 发送协议转换前的交易hash、协议转换后的交易hash，向友链节点验证跨链交易
 *
 * @author: qinyifeng
 * @date: 2018/12/17
 */
public class VerifyCrossWithFCMessage extends BaseMessage {

    /**
     * 转换NULS主网协议后交易hash
     */
    @Getter
    @Setter
    private NulsDigestData requestHash;

    /**
     * 友链原始交易hash
     */
    @Getter
    @Setter
    private byte[] originalTxHash;

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(requestHash);
        size += SerializeUtils.sizeOfBytes(originalTxHash);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(requestHash);
        stream.writeBytesWithLength(originalTxHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
        this.originalTxHash = byteBuffer.readByLengthByte();
    }
}
