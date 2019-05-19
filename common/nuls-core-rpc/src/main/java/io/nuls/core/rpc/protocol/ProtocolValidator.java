package io.nuls.core.rpc.protocol;

import java.util.List;

public class ProtocolValidator {

    public static boolean meaasgeValidate(Class messageClass, Class handlerClass, Protocol protocol, String methodName) {
        List<MessageDefine> allowMsg = protocol.getAllowMsg();
        String messageClassName = messageClass.getName();
        String handlerClassName = handlerClass.getName();
        for (MessageDefine config : allowMsg) {
            if (config.getName().equals(messageClassName)) {
                List<MessageProcessor> processors = config.getProcessors();
                for (MessageProcessor processor : processors) {
                    if (handlerClassName.equals(processor.getHandler()) && methodName.equals(processor.getMethod())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean transactionValidate(int txType, Class handlerClass, Protocol protocol, String methodName, TxMethodType type) {
        List<TxDefine> allowTx = protocol.getAllowTx();
        String handlerClassName = handlerClass.getName();
        for (TxDefine config : allowTx) {
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
