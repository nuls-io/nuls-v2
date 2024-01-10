package io.nuls.protocol.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.util.Comparator;

public class ProtocolVersionPo extends BaseNulsData {

    public static final Comparator<ProtocolVersionPo> COMPARATOR = Comparator.comparingInt(ProtocolVersionPo::getVersion);

    /**
     * Protocol version number
     */
    private short version;

    /**
     * The minimum effective ratio within each statistical interval(60-100)
     */
    private byte effectiveRatio;

    /**
     * The number of consecutive intervals that the agreement must meet in order to take effect(50-1000)
     */
    private short continuousIntervalCount;

    /**
     * Starting height of agreement effectiveness
     */
    private long beginHeight;

    /**
     * The height at which the agreement takes effect and ends
     */
    private long endHeight;

    public long getBeginHeight() {
        return beginHeight;
    }

    public void setBeginHeight(long beginHeight) {
        this.beginHeight = beginHeight;
    }

    public long getEndHeight() {
        return endHeight;
    }

    public void setEndHeight(long endHeight) {
        this.endHeight = endHeight;
    }

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

        ProtocolVersionPo that = (ProtocolVersionPo) o;

        if (version != that.version) {
            return false;
        }
        if (effectiveRatio != that.effectiveRatio) {
            return false;
        }
        if (continuousIntervalCount != that.continuousIntervalCount) {
            return false;
        }
        if (beginHeight != that.beginHeight) {
            return false;
        }
        return endHeight == that.endHeight;

    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + (int) effectiveRatio;
        result = 31 * result + (int) continuousIntervalCount;
        result = 31 * result + (int) (beginHeight ^ (beginHeight >>> 32));
        result = 31 * result + (int) (endHeight ^ (endHeight >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "version=" + version +
                ", effectiveRatio=" + effectiveRatio +
                ", continuousIntervalCount=" + continuousIntervalCount +
                ", beginHeight=" + beginHeight +
                ", endHeight=" + endHeight +
                '}';
    }

    @Override
    public int size() {
        return 13;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(version);
        stream.writeByte(effectiveRatio);
        stream.writeShort(continuousIntervalCount);
        stream.writeUint32(beginHeight);
        stream.writeUint32(endHeight);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.version = byteBuffer.readShort();
        this.effectiveRatio = byteBuffer.readByte();
        this.continuousIntervalCount = byteBuffer.readShort();
        this.beginHeight = byteBuffer.readUint32();
        this.endHeight = byteBuffer.readUint32();
    }

}
