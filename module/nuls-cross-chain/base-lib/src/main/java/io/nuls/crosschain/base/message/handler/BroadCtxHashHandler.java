package io.nuls.crosschain.base.message.handler;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxHashMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("BroadCtxHashHandlerV1")
public class BroadCtxHashHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.BROAD_CTX_HASH_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        BroadCtxHashMessage realMessage = RPCUtil.getInstanceRpcStr(message, BroadCtxHashMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveCtxHash(chainId, nodeId, realMessage);
    }
}
