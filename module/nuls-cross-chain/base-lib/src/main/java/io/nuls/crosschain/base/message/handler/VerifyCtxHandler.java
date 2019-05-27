package io.nuls.crosschain.base.message.handler;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.VerifyCtxMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("VerifyCtxHandlerV1")
public class VerifyCtxHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.VERIFY_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        VerifyCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, VerifyCtxMessage.class);
        if (message == null) {
            return;
        }
        protocolService.verifyCtx(chainId, nodeId, realMessage);
    }
}
