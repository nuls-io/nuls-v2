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
 * 版本统计信息,实质上也是由生效高度串联的一条链
 *
 * @author captain
 * @version 1.0
 * @date 18-12-10 下午3:50
 */
public class StatisticsInfo extends BaseNulsData {

    /**
     * 本次统计信息生效高度
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

    /**
     * 统计区间内所有协议的占比
     */
    private Map<ProtocolVersion,Integer> protocolVersionMap;

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public short getCount() {
        return count;
    }

    public void setCount(short count) {
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
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(height);
        stream.writeShort(count);
        stream.writeNulsData(protocolVersion);
        stream.writeShort((short) protocolVersionMap.size());
        Set<Map.Entry<ProtocolVersion, Integer>> entries = protocolVersionMap.entrySet();
        for (Map.Entry<ProtocolVersion, Integer> entry : entries) {
            stream.writeNulsData(entry.getKey());
            stream.writeUint16(entry.getValue());
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.height = byteBuffer.readInt64();
        this.count = byteBuffer.readShort();
        this.protocolVersion = byteBuffer.readNulsData(new ProtocolVersion());
        short size = byteBuffer.readShort();
        this.protocolVersionMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            protocolVersionMap.put(byteBuffer.readNulsData(new ProtocolVersion()), byteBuffer.readUint16());
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
