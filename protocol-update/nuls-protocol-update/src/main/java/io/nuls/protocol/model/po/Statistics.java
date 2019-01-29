package io.nuls.protocol.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Data;

import java.io.IOException;

/**
 * 版本统计信息,实质上也是由生效高度串联的一条链
 *
 * @author captain
 * @version 1.0
 * @date 18-12-10 下午3:50
 */
@Data
public class Statistics extends BaseNulsData {

    /**
     * 上一个统计信息生效高度
     */
    private long lastHeight;

    /**
     * 统计信息生效高度
     */
    private long height;
    /**
     * 统计信息连续确认数
     */
    private short count;
    /**
     * 生效协议版本
     */
    private ProtocolVersion protocolVersion;

    @Override
    public int size() {
        int size = 18;
        size += SerializeUtils.sizeOfNulsData(protocolVersion);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(lastHeight);
        stream.writeInt64(height);
        stream.writeShort(count);
        stream.writeNulsData(protocolVersion);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.lastHeight = byteBuffer.readInt64();
        this.height = byteBuffer.readInt64();
        this.count = byteBuffer.readShort();
        this.protocolVersion = byteBuffer.readNulsData(new ProtocolVersion());
    }

}
