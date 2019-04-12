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

    public static boolean transactionValidate(int txType, Class handlerClass, Protocol protocol, String methodName, TxMethodType type) {
        List<TransactionConfig> allowTx = protocol.getAllowTx();
        String handlerClassName = handlerClass.getName();
        for (TransactionConfig config : allowTx) {
            if (config.getType() == txType && config.getHandler().equals(handlerClassName)) {
                switch (type) {
                    case VALID:
                        if (methodName.equals(config.getValidate())) {
                            return true;
                        }
                        break;
                    case COMMIT:
                        if (methodName.equals(config.getCommit())) {
                            return true;
                        }
                        break;
                    case ROLLBACK:
                        if (methodName.equals(config.getRollback())) {
                            return true;
                        }
                        break;
                    default:
                        return false;
                }
            }
        }
        return false;
    }

}
