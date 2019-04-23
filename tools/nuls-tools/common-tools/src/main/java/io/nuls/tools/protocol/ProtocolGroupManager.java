package io.nuls.tools.protocol;

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

    public static ProtocolGroup getProtocol(int chainId) {
        return protocolGroupMap.get(chainId);
    }

    public static void updateProtocol(int chainId, short protocolVersion) {
        ProtocolGroup protocolGroup = protocolGroupMap.get(chainId);
        protocolGroup.setVersion(protocolVersion);
    }
}
