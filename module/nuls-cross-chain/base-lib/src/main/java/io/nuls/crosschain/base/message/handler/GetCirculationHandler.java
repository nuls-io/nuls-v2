package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.GetCirculationMessage;
import io.nuls.crosschain.base.service.ProtocolService;

@Component("GetCirculationHandlerV1")
public class GetCirculationHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.GET_CIRCULLAT_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetCirculationMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetCirculationMessage.class);
        if (message == null) {
            return;
        }
        protocolService.getCirculation(chainId, nodeId, realMessage);
    }
}
