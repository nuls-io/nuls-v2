package io.nuls.crosschain.base.message.handler;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("BroadCtxSignHandlerV1")
public class BroadCtxSignHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.BROAD_CTX_SIGN_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        BroadCtxSignMessage realMessage = RPCUtil.getInstanceRpcStr(message, BroadCtxSignMessage.class);
        if (message == null) {
            return;
        }
        protocolService.recvCtxSign(chainId, nodeId, realMessage);
    }
}
