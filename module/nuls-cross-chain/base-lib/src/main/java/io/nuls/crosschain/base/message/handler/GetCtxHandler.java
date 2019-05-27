package io.nuls.crosschain.base.message.handler;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.GetCtxMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("GetCtxHandlerV1")
public class GetCtxHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.GET_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetCtxMessage.class);
        if (message == null) {
            return;
        }
        protocolService.getCtx(chainId, nodeId, realMessage);
    }
}
