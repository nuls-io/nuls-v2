package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.NewCtxMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("NewCtxHandlerV1")
public class NewCtxHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.NEW_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        NewCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, NewCtxMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveCtx(chainId, nodeId, realMessage);

    }
}
