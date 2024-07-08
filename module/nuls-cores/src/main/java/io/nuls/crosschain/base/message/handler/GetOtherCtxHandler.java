package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.GetOtherCtxMessage;
import io.nuls.crosschain.base.service.ProtocolService;
import io.nuls.crosschain.base.utils.HashSetTimeDuplicateProcessor;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.utils.manager.ChainManager;

/**
 * GetOtherCtxMessageProcessing class
 * GetOtherCtxMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("GetOtherCtxHandlerV1")
public class GetOtherCtxHandler implements MessageProcessor {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ProtocolService protocolService;

    private HashSetTimeDuplicateProcessor processor = new HashSetTimeDuplicateProcessor(1000, 300000L);

    @Override
    public String getCmd() {
        return CommandConstant.GET_OTHER_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetOtherCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetOtherCtxMessage.class);
        Chain chain = chainManager.getChainMap().get(chainId);
        if (realMessage == null || realMessage.getRequestHash() == null) {
            chain.getLogger().info("0 discard ： " + nodeId + "," + message);
            return;
        }
        String hash = realMessage.getRequestHash().toHex();
//        if (processor.insertAndCheck(nodeId + hash)) {
            chain.getLogger().info("B process ： " + nodeId + "," + hash);
            protocolService.getOtherCtx(chainId, nodeId, realMessage);
//        } else {
//            chain.getLogger().info("B discard ： " + nodeId + "," + hash);
//        }

    }
}
