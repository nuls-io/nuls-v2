package io.nuls.core.rpc.protocol;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ProtocolGroupManager {

    @Autowired
    public static ModuleConfig moduleConfig;

    public static VersionChangeInvoker getVersionChangeInvoker() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return moduleConfig.getVersionChangeInvoker();
    }

    public static List<Integer> chainIds = new CopyOnWriteArrayList<>();

    private static Map<Integer, ProtocolGroup> protocolGroupMap = new ConcurrentHashMap<>();

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
        Protocol protocol = protocolGroup.getProtocolsMap().get(protocolVersion);
        if (protocol != null) {
            protocolGroup.setVersion(protocolVersion);
        }
    }
}
