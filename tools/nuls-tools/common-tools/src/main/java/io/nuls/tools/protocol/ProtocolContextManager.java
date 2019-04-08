package io.nuls.tools.protocol;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProtocolContextManager {
    public static List<Integer> chainIds = new CopyOnWriteArrayList<>();

    private static Map<Integer, ProtocolContext> contextMap = new ConcurrentHashMap<>();

    private ProtocolContextManager() {
    }

    public static void init(int chainId, Map<Short, Protocol> protocolMap, short version) {
        ProtocolContext protocolContext = new ProtocolContext();
        chainIds.add(chainId);
        contextMap.put(chainId, protocolContext);
        protocolContext.setProtocolsMap(protocolMap);
        protocolContext.setVersion(version);
    }

    public static ProtocolContext getContext(int chainId) {
        return contextMap.get(chainId);
    }
}
