package io.nuls.protocol.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import lombok.Data;

import java.io.IOException;

@Data
public class ProtocolVersion extends BaseNulsData {

    /**
     * 协议版本号
     */
    private short version;

    /**
     * 统计区间大小(500-10000)
     */
    private short interval;

    /**
     * 每个统计区间内的最小生效比例(60-100)
     */
    private byte effectiveRatio;

    /**
     * 协议生效要满足的连续区间数(50-1000)
     */
    private short continuousIntervalCount;

    @Override
    public int size() {
        return 7;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(version);
        stream.writeShort(interval);
        stream.writeByte(effectiveRatio);
        stream.writeShort(continuousIntervalCount);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.version = byteBuffer.readShort();
        this.interval = byteBuffer.readShort();
        this.effectiveRatio = byteBuffer.readByte();
        this.continuousIntervalCount = byteBuffer.readShort();
    }

}
