package io.nuls.base.protocol;

import java.util.Map;

public class ProtocolGroup {

    /**
     * 当前协议版本
     */
    private short version;

    /**
     * 所有协议版本(包括消息、交易映射)
     */
    private Map<Short, Protocol> protocolsMap;

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public Map<Short, Protocol> getProtocolsMap() {
        return protocolsMap;
    }

    public void setProtocolsMap(Map<Short, Protocol> protocolsMap) {
        this.protocolsMap = protocolsMap;
    }

    public Protocol getProtocol() {
        return protocolsMap.get(version);
    }
}
