package io.nuls.base.basic;

import com.google.common.base.Objects;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.util.Comparator;

/**
 * 协议版本
 *
 * @author captain
 * @version 1.0
 * @date 19-1-30 下午3:23
 */
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

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public byte getEffectiveRatio() {
        return effectiveRatio;
    }

    public void setEffectiveRatio(byte effectiveRatio) {
        this.effectiveRatio = effectiveRatio;
    }

    public short getContinuousIntervalCount() {
        return continuousIntervalCount;
    }

    public void setContinuousIntervalCount(short continuousIntervalCount) {
        this.continuousIntervalCount = continuousIntervalCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtocolVersion that = (ProtocolVersion) o;
        return version == that.version &&
                effectiveRatio == that.effectiveRatio &&
                continuousIntervalCount == that.continuousIntervalCount;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(version, effectiveRatio, continuousIntervalCount);
    }

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

    @Override
    public String toString() {
        return "ProtocolVersion{" +
                "version=" + version +
                ", effectiveRatio=" + effectiveRatio +
                ", continuousIntervalCount=" + continuousIntervalCount +
                '}';
    }
}
