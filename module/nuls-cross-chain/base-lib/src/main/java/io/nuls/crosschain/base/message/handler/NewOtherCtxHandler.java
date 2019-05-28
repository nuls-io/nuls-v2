package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.NewOtherCtxMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("NewOtherCtxHandlerV1")
public class NewOtherCtxHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.NEW_OTHER_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        NewOtherCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, NewOtherCtxMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveOtherCtx(chainId, nodeId, realMessage);
    }
}
