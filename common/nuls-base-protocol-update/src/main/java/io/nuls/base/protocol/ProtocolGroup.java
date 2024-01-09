package io.nuls.base.protocol;

import java.util.Map;

public class ProtocolGroup {

    /**
     * Current protocol version
     */
    private short version;

    /**
     * All protocol versions(Including messages、Transaction mapping)
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
