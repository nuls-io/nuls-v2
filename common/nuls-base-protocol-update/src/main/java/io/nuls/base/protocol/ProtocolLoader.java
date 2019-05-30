package io.nuls.base.protocol;

import io.nuls.core.io.IoUtils;
import io.nuls.core.parse.JSONUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.base.protocol.ProtocolConstant.PROTOCOL_CONFIG_COMPARATOR;
import static io.nuls.base.protocol.ProtocolConstant.PROTOCOL_CONFIG_FILE;

/**
 * 把协议配置文件解析成对象
 *
 * @author captain
 * @version 1.0
 * @date 2019/4/26 11:50
 */
public class ProtocolLoader {

    public static void load(int chainId, String protocolConfigJson) throws Exception {
        List<ProtocolConfigJson> protocolConfigs = JSONUtils.json2list(protocolConfigJson, ProtocolConfigJson.class);
        protocolConfigs.sort(PROTOCOL_CONFIG_COMPARATOR);
        Map<Short, Protocol> protocolsMap = new HashMap<>(protocolConfigs.size());
        for (ProtocolConfigJson configJson : protocolConfigs) {
            Protocol protocol = new Protocol();
            protocol.setVersion(configJson.getVersion());
            short extend = configJson.getExtend();
            Set<MessageDefine> msgList = new HashSet<>();
            Set<TxDefine> txList = new HashSet<>();
            List<MessageDefine> validMessages = configJson.getValidMsgs();
            List<TxDefine> validTransactions = configJson.getValidTxs();
            Set<String> discardMsgs = new HashSet<>();
            String[] strings1 = configJson.getInvalidMsgs().split(",");
            Collections.addAll(discardMsgs, strings1);
            discardMsgs.remove("");
            Set<String> discardTxs = new HashSet<>();
            String[] strings = configJson.getInvalidTxs().split(",");
            Collections.addAll(discardTxs, strings);
            discardTxs.remove("");
            if (extend > 0) {
                Protocol parent = protocolsMap.get(extend);
                List<String> msg = validMessages.stream().map(MessageDefine::getName).collect(Collectors.toList());
                discardMsgs.addAll(parent.getInvalidMsgs());
                discardMsgs.removeIf(msg::contains);
                //添加上一个版本的生效信息
                msgList.addAll(parent.getAllowMsg());
                //实现更新功能
                msgList.removeIf(e -> msg.contains(e.getName()));
                List<Short> tx = validTransactions.stream().map(TxDefine::getType).collect(Collectors.toList());
                discardTxs.addAll(parent.getInvalidTxs());
                discardTxs.removeIf(e -> tx.contains(Short.valueOf(e)));
                txList.addAll(parent.getAllowTx());
                txList.removeIf(e -> tx.contains(e.getType()));
            }
            msgList.addAll(validMessages);
            msgList.removeIf(e -> discardMsgs.contains(e.getName()));
            txList.addAll(validTransactions);
            txList.removeIf(e -> discardTxs.contains(e.getType() + ""));
            protocol.setAllowMsg(msgList);
            protocol.setAllowTx(txList);
            protocol.setInvalidTxs(discardTxs);
            protocol.setInvalidMsgs(discardMsgs);
            protocolsMap.put(protocol.getVersion(), protocol);
        }
        ProtocolGroupManager.init(chainId, protocolsMap, (short) 1);
    }

    public static void load(int chainId) throws Exception {
        load(chainId, IoUtils.read(PROTOCOL_CONFIG_FILE));
    }

}
