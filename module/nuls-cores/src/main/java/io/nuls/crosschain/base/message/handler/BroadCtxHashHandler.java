package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxHashMessage;
import io.nuls.crosschain.base.service.ProtocolService;
import io.nuls.crosschain.base.utils.HashSetTimeDuplicateProcessor;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.utils.manager.ChainManager;

/**
 * BroadCtxHashMessageProcessing class
 * BroadCtxHashMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("BroadCtxHashHandlerV1")
public class BroadCtxHashHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    private HashSetTimeDuplicateProcessor processor = new HashSetTimeDuplicateProcessor(1000, 300000L);

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
        if (processor.insertAndCheck(nodeId + realMessage)) {
//            Log.info("C process ： " + nodeId + "," + message);
            protocolService.receiveCtxHash(chainId, nodeId, realMessage);
        } else {
//            Log.info("C discard ： " + nodeId + "," + message);
        }
    }
}
