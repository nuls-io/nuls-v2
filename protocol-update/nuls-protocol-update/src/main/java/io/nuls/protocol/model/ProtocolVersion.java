package io.nuls.protocol.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.Comparator;

/**
 * 协议版本
 *
 * @author captain
 * @version 1.0
 * @date 19-1-30 下午3:23
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ProtocolVersion extends BaseNulsData {

    public static final Comparator<ProtocolVersion> COMPARATOR = Comparator.comparingInt(ProtocolVersion::getVersion);

    /**
     * 协议版本号
     */
    private short version;

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
        return 5;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(version);
        stream.writeByte(effectiveRatio);
        stream.writeShort(continuousIntervalCount);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.version = byteBuffer.readShort();
        this.effectiveRatio = byteBuffer.readByte();
        this.continuousIntervalCount = byteBuffer.readShort();
    }

}
