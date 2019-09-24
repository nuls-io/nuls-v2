package io.nuls.base.protocol;

import io.nuls.base.protocol.cmd.MessageDispatcher;
import io.nuls.base.protocol.cmd.TransactionDispatcher;
import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ProtocolGroupManager {

    private static boolean loadProtocol;

    @Autowired
    public static ModuleConfig moduleConfig;

    @Autowired
    private static TransactionDispatcher transactionDispatcher;

    @Autowired
    private static MessageDispatcher messageDispatcher;

    public static boolean isLoadProtocol() {
        return loadProtocol;
    }

    public static void setLoadProtocol(boolean loadProtocol) {
        ProtocolGroupManager.loadProtocol = loadProtocol;
    }

    public static VersionChangeInvoker getVersionChangeInvoker() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return moduleConfig.getVersionChangeInvoker();
    }

    public static List<Integer> chainIds = new CopyOnWriteArrayList<>();

    private static Map<Integer, ProtocolGroup> protocolGroupMap = new ConcurrentHashMap<>();

    private static Map<Integer, Short> versionMap = new ConcurrentHashMap<>();

    public static void init(int chainId, Map<Short, Protocol> protocolMap, short version) {
        if (ProtocolGroupManager.isLoadProtocol()) {
            ProtocolGroup protocolGroup = new ProtocolGroup();
            protocolGroup.setProtocolsMap(protocolMap);
            protocolGroup.setVersion(version);
            protocolGroupMap.put(chainId, protocolGroup);
        }
        chainIds.add(chainId);
        if (ProtocolGroupManager.getCurrentVersion(chainId) != null) {
            Short currentVersion = ProtocolGroupManager.getCurrentVersion(chainId);
            version = version < currentVersion ? currentVersion : version;
        }
        updateProtocol(chainId, version);
    }

    /**
     * 获取当前生效的协议版本号
     * @param chainId
     * @return
     */
    public static Short getCurrentVersion(int chainId) {
        return versionMap.get(chainId);
    }

    /**
     * 获取当前生效的协议版本(包含消息、交易详细信息)
     * @param chainId
     * @return
     */
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

    public static void updateProtocol(int chainId, short protocolVersion) {
        versionMap.put(chainId, protocolVersion);
        if (ProtocolGroupManager.isLoadProtocol()) {
            if (transactionDispatcher == null) {
                transactionDispatcher = SpringLiteContext.getBean(TransactionDispatcher.class);
            }
            if (messageDispatcher == null) {
                messageDispatcher = SpringLiteContext.getBean(MessageDispatcher.class);
            }
            ProtocolGroup protocolGroup = protocolGroupMap.get(chainId);
            Protocol protocol = protocolGroup.getProtocolsMap().get(protocolVersion);
            if (protocol != null) {
                protocolGroup.setVersion(protocolVersion);
                List<TransactionProcessor> transactionProcessors = new ArrayList<>();
                protocol.getAllowTx().forEach(e -> {
                    if (StringUtils.isNotBlank(e.getHandler())) {
                        transactionProcessors.add(SpringLiteContext.getBean(TransactionProcessor.class, e.getHandler()));
                    }
                });
                transactionDispatcher.setProcessors(transactionProcessors);
                List<MessageProcessor> messageProcessors = new ArrayList<>();
                protocol.getAllowMsg().forEach(e -> {
                    for (String s : e.getHandlers().split(",")) {
                        messageProcessors.add(SpringLiteContext.getBean(MessageProcessor.class, s));
                    }
                });
                messageDispatcher.setProcessors(messageProcessors);
            }
        }
    }
}
