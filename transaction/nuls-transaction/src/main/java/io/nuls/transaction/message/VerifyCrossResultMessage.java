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
 * 节点收到跨链验证请求后，将验证结果发送回去
 *
 * @author: qinyifeng
 * @date: 2018/12/18
 */
public class VerifyCrossResultMessage extends BaseMessage {

    /**
     * 交易hash
     */
    @Getter
    @Setter
    private NulsDigestData requestHash;

    /**
     * 确认高度
     */
    @Getter
    @Setter
    private long height;

    /**
     * 交易签名
     */
//    @Getter
//    @Setter
//    private byte[] transactionSignature;

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(requestHash);
        size += SerializeUtils.sizeOfInt64();
//        size += SerializeUtils.sizeOfBytes(transactionSignature);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(requestHash);
        stream.writeInt64(height);
//        stream.writeBytesWithLength(transactionSignature);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
        this.height = byteBuffer.readInt64();
//        this.transactionSignature = byteBuffer.readByLengthByte();
    }
}
