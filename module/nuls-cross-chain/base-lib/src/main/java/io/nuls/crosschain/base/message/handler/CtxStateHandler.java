package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.CtxStateMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("CtxStateHandlerV1")
public class CtxStateHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.CTX_STATE_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        CtxStateMessage realMessage = RPCUtil.getInstanceRpcStr(message, CtxStateMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveCtxState(chainId, nodeId, realMessage);
    }
}
