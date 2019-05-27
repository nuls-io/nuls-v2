package io.nuls.crosschain.base.message.handler;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.CirculationMessage;
import io.nuls.crosschain.base.message.GetOtherCtxMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("GetOtherCtxHandlerV1")
public class GetOtherCtxHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.GET_OTHER_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetOtherCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, CirculationMessage.class);
        if (message == null) {
            return;
        }
        protocolService.getOtherCtx(chainId, nodeId, realMessage);
    }
}
