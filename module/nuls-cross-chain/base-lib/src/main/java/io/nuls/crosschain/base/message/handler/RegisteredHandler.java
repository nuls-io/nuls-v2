package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("RegisteredHandlerV1")
public class RegisteredHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.REGISTERED_CHAIN_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        RegisteredChainMessage realMessage = RPCUtil.getInstanceRpcStr(message, RegisteredChainMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveRegisteredChainInfo(chainId, nodeId, realMessage);
    }
}
