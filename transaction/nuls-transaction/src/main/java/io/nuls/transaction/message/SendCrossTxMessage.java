package io.nuls.transaction.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.message.base.BaseMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * 发送完整的跨链交易的消息
 *
 * @author: qinyifeng
 * @date: 2018/12/18
 */
public class SendCrossTxMessage extends BaseMessage {
    /**
     * 链ID
     */
    @Getter
    @Setter
    private int chainId;

    /**
     * 交易类型
     */
    @Getter
    @Setter
    private int type;

    /**
     * 交易时间
     */
    @Getter
    @Setter
    private long time;

    /**
     * 交易数据
     */
    @Getter
    @Setter
    private byte[] txData;

    /**
     * 交易输入和输出
     */
    @Getter
    @Setter
    private byte[] coinData;

    /**
     * 备注
     */
    @Getter
    @Setter
    private byte[] remark;

    /**
     * 交易签名
     */
    @Getter
    @Setter
    private byte[] transactionSignature;

    @Override
    public int size() {
        int size = 0;
        //chainId
        size += SerializeUtils.sizeOfUint16();
        //type
        size += SerializeUtils.sizeOfUint16();
        //time
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfBytes(txData);
        size += SerializeUtils.sizeOfBytes(coinData);
        size += SerializeUtils.sizeOfBytes(remark);
        size += SerializeUtils.sizeOfBytes(transactionSignature);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint16(type);
        stream.writeUint48(time);
        stream.writeBytesWithLength(txData);
        stream.writeBytesWithLength(coinData);
        stream.writeBytesWithLength(remark);
        stream.writeBytesWithLength(transactionSignature);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.type = byteBuffer.readUint16();
        this.time = byteBuffer.readUint48();
        this.txData = byteBuffer.readByLengthByte();
        this.coinData = byteBuffer.readByLengthByte();
        this.remark = byteBuffer.readByLengthByte();
        this.transactionSignature = byteBuffer.readByLengthByte();
    }
}
