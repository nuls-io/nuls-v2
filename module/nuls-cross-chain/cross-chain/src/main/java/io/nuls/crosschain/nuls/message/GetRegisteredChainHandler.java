package io.nuls.crosschain.nuls.message;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.crosschain.base.message.GetRegisteredChainMessage;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.servive.MainNetService;

@Component("GetRegisteredChainHandlerV1")
public class GetRegisteredChainHandler implements MessageProcessor {
    @Autowired
    private MainNetService mainNetService;

    @Override
    public String getCmd() {
        return NulsCrossChainConstant.GET_REGISTERED_CHAIN_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetRegisteredChainMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetRegisteredChainMessage.class);
        mainNetService.getCrossChainList(chainId, nodeId, realMessage);
    }
}
