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
     * 链ID
     */
    @Getter
    @Setter
    private int chainId;

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
    private NulsDigestData originalHash;

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfNulsData(requestHash);
        size += SerializeUtils.sizeOfNulsData(originalHash);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeNulsData(requestHash);
        stream.writeNulsData(originalHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.requestHash = byteBuffer.readHash();
        this.originalHash = byteBuffer.readHash();
    }
}
