package io.nuls.tools.protocol;

import java.util.List;

public class ProtocolValidator {

    public static boolean meaasgeValidate(Class messageClass, Class handlerClass, Protocol protocol, String methodName) {
        List<MessageConfig> allowMsg = protocol.getAllowMsg();
        String messageClassName = messageClass.getName();
        String handlerClassName = handlerClass.getName();
        for (MessageConfig config : allowMsg) {
            if (config.getName().equals(messageClassName)) {
                List<ListItem> handlers = config.getHandlers();
                for (ListItem handler : handlers) {
                    String name = handler.getName();
                    String[] strings = name.split("#");
                    if (handlerClassName.equals(strings[0]) && methodName.equals(strings[1])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean transactionValidate(int chainId, Class messageClass, Class handlerClass) {
        return false;
    }

}
