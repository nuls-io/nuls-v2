package io.nuls.crosschain.base.message.handler;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.VerifyCtxResultMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("VerifyCtxResultHandlerV1")
public class VerifyCtxResultHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.CTX_VERIFY_RESULT_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        VerifyCtxResultMessage realMessage = RPCUtil.getInstanceRpcStr(message, VerifyCtxResultMessage.class);
        if (message == null) {
            return;
        }
        protocolService.recvVerifyRs(chainId, nodeId, realMessage);
    }
}
