package io.nuls.tools.protocol;

import java.util.List;

public class ProtocolValidator {

    public static String meaasgeValidateV1(Class messageClass, Class handlerClass, Protocol protocol) {
        List<MessageConfig> allowMsg = protocol.getAllowMsg();
        String messageClassName = messageClass.getName();
        String handlerClassName = handlerClass.getName();
        for (MessageConfig config : allowMsg) {
            if (config.getName().equals(messageClassName)) {
                List<ListItem> handlers = config.getHandlers();
                for (ListItem handler : handlers) {
                    String name = handler.getName();
                    String[] strings = name.split("#");
                    String ss = strings[0];
                    if (ss.equals(handlerClassName)) {
                        return strings[1];
                    }
                }
            }
        }
        return "";
    }

    public static boolean transactionValidate(int chainId, Class messageClass, Class handlerClass) {
        return false;
    }

}
