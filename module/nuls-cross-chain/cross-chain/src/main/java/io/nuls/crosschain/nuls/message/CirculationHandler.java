package io.nuls.crosschain.nuls.message;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.CirculationMessage;
import io.nuls.crosschain.nuls.servive.MainNetService;

@Component("CirculationHandlerV1")
public class CirculationHandler implements MessageProcessor {
    @Autowired
    private MainNetService mainNetService;

    @Override
    public String getCmd() {
        return CommandConstant.CIRCULATION_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        CirculationMessage realMessage = RPCUtil.getInstanceRpcStr(message, CirculationMessage.class);
        if (message == null) {
            return;
        }
        mainNetService.receiveCirculation(chainId, nodeId, realMessage);
    }
}
