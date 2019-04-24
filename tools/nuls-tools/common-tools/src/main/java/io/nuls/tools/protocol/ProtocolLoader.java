package io.nuls.tools.protocol;

import io.nuls.tools.io.IoUtils;
import io.nuls.tools.parse.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.nuls.tools.constant.BaseConstant.PROTOCOL_CONFIG_COMPARATOR;
import static io.nuls.tools.constant.BaseConstant.PROTOCOL_CONFIG_FILE;

public class ProtocolLoader {

    public static void load(int chainId, String protocolConfigJson) throws Exception {
        List<ProtocolConfigJson> protocolConfigs = JSONUtils.json2list(protocolConfigJson, ProtocolConfigJson.class);
        protocolConfigs.sort(PROTOCOL_CONFIG_COMPARATOR);
        Map<Short, Protocol> protocolsMap = new HashMap<>(protocolConfigs.size());
        for (ProtocolConfigJson configJson : protocolConfigs) {
            Protocol protocol = new Protocol();
            protocol.setVersion(configJson.getVersion());
            short extend = configJson.getExtend();
            List<MessageConfig> msgList = new ArrayList<>();
            List<TransactionConfig> txList = new ArrayList<>();
            List<MessageConfig> validMessages = configJson.getValidMessages();
            List<TransactionConfig> validTransactions = configJson.getValidTransactions();
            if (extend > 0) {
                Protocol parent = protocolsMap.get(extend);
                List<MessageConfig> parentAllowMsg = parent.getAllowMsg();
                List<TransactionConfig> parentAllowTx = parent.getAllowTx();
                List<String> msg = validMessages.stream().map(MessageConfig::getName).collect(Collectors.toList());
                msgList.addAll(parentAllowMsg);
                msgList.removeIf(e -> msg.contains(e.getName()));
                List<String> tx = validTransactions.stream().map(TransactionConfig::getName).collect(Collectors.toList());
                txList.addAll(parentAllowTx);
                txList.removeIf(e -> tx.contains(e.getName()));
            }
            msgList.addAll(validMessages);
            List<String> discardMsg = configJson.getInvalidMessages().stream().map(ListItem::getName).collect(Collectors.toList());
            msgList.removeIf(e -> discardMsg.contains(e.getName()));
            txList.addAll(validTransactions);
            List<String> discardTx = configJson.getInvalidTransactions().stream().map(ListItem::getName).collect(Collectors.toList());
            txList.removeIf(e -> discardTx.contains(e.getName()));
            protocol.setAllowMsg(msgList);
            protocol.setAllowTx(txList);
            protocol.setModuleValidator(configJson.getModuleValidator());
            protocol.setModuleCommit(configJson.getModuleCommit());
            protocol.setModuleRollback(configJson.getModuleRollback());
            protocolsMap.put(protocol.getVersion(), protocol);
        }
        ProtocolGroupManager.init(chainId, protocolsMap, (short) 1);
    }

    public static void load(int chainId) throws Exception {
        load(chainId, IoUtils.read(PROTOCOL_CONFIG_FILE));
    }

}
