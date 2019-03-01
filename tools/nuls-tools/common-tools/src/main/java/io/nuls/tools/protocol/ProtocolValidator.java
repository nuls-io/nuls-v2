package io.nuls.tools.protocol;

public class ProtocolValidator {

    public static boolean meaasgeValidate(int chainId, Class messageClass, Class handlerClass) {
//        ChainContext context = ContextManager.getContext(chainId);
//        short version = context.getVersion();
//        Protocol protocol = context.getProtocolsMap().get(version);
//        List<MessageConfig> allowMsg = protocol.getAllowMsg();
//        String messageClassName = messageClass.getName();
//        String handlerClassName = handlerClass.getName();
//        for (MessageConfig config : allowMsg) {
//            if (config.getRefer().equals(messageClassName)) {
//                List<ListItem> handlers = config.getHandlers();
//                for (ListItem handler : handlers) {
//                    if (handler.getName().equals(handlerClassName)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
        return true;
    }

    public static boolean transactionValidate(int chainId, Class messageClass, Class handlerClass) {
        return false;
    }

}
