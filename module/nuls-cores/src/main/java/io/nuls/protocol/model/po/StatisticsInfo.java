package io.nuls.protocol.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.basic.ProtocolVersion;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Version Statistics,Essentially, it is also a chain that is highly interconnected by its effectiveness
 *
 * @author captain
 * @version 1.0
 * @date 18-12-10 afternoon3:50
 */
public class StatisticsInfo extends BaseNulsData {

    /**
     * The effective height of this statistical information
     */
    private long height;
    /**
     * Number of consecutive confirmations of statistical information
     */
    private short oldCount;
    private long count;
    /**
     * Effective Agreement Version
     */
    private ProtocolVersion protocolVersion;

    /**
     * The proportion of all protocols within the statistical interval
     */
    private Map<ProtocolVersion, Integer> protocolVersionMap;

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        if (count < 0) {
            count = 65536 + count;
        }
        this.count = count;
    }
    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public Map<ProtocolVersion, Integer> getProtocolVersionMap() {
        return protocolVersionMap;
    }

    public void setProtocolVersionMap(Map<ProtocolVersion, Integer> protocolVersionMap) {
        this.protocolVersionMap = protocolVersionMap;
    }

    @Override
    public int size() {
        int size = 12;
        size += SerializeUtils.sizeOfNulsData(protocolVersion);
        for (int i = 0; i < protocolVersionMap.size(); i++) {
            size += 7;
        }
        size += SerializeUtils.sizeOfInt64();
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(height);
        stream.writeShort(oldCount);
        stream.writeNulsData(protocolVersion);
        stream.writeShort((short) protocolVersionMap.size());
        Set<Map.Entry<ProtocolVersion, Integer>> entries = protocolVersionMap.entrySet();
        for (Map.Entry<ProtocolVersion, Integer> entry : entries) {
            stream.writeNulsData(entry.getKey());
            stream.writeUint16(entry.getValue());
        }
        stream.writeInt64(count);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.height = byteBuffer.readInt64();
        this.oldCount = byteBuffer.readShort();
        this.protocolVersion = byteBuffer.readNulsData(new ProtocolVersion());
        short size = byteBuffer.readShort();
        this.protocolVersionMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            protocolVersionMap.put(byteBuffer.readNulsData(new ProtocolVersion()), byteBuffer.readUint16());
        }
        if (byteBuffer.isFinished()) {
            this.count = oldCount;
        } else {
            this.count = byteBuffer.readInt64();
        }
    }

    @Override
    public String toString() {
        return "{" +
                "height=" + height +
                ", count=" + count +
                ", protocolVersion=" + protocolVersion +
                ", protocolVersionMap=" + protocolVersionMap +
                '}';
    }
}
