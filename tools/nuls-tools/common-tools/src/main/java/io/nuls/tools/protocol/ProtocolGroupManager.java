package io.nuls.tools.protocol;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProtocolGroupManager {
    public static List<Integer> chainIds = new CopyOnWriteArrayList<>();

    private static Map<Integer, ProtocolGroup> protocolGroupMap = new ConcurrentHashMap<>();

    private ProtocolGroupManager() {
    }

    public static void init(int chainId, Map<Short, Protocol> protocolMap, short version) {
        ProtocolGroup protocolGroup = new ProtocolGroup();
        chainIds.add(chainId);
        protocolGroupMap.put(chainId, protocolGroup);
        protocolGroup.setProtocolsMap(protocolMap);
        protocolGroup.setVersion(version);
    }

    public static Protocol getCurrentProtocol(int chainId) {
        return protocolGroupMap.get(chainId).getProtocol();
    }

    public static Protocol getOneProtocol() {
        ProtocolGroup o = (ProtocolGroup) protocolGroupMap.values().toArray()[0];
        return o.getProtocol();
    }

    public static Collection<Protocol> getProtocols(int chainId) {
        return protocolGroupMap.get(chainId).getProtocolsMap().values();
    }

    public static short getVersion(int chainId) {
        return protocolGroupMap.get(chainId).getVersion();
    }

    public static void updateProtocol(int chainId, short protocolVersion) {
        ProtocolGroup protocolGroup = protocolGroupMap.get(chainId);
        protocolGroup.setVersion(protocolVersion);
    }
}
