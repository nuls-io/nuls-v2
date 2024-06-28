package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.service.ProtocolService;
import io.nuls.crosschain.base.utils.HashSetTimeDuplicateProcessor;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.utils.manager.ChainManager;

/**
 * BroadCtxSignMessageProcessing class
 * BroadCtxSignMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("BroadCtxSignHandlerV1")
public class BroadCtxSignHandler implements MessageProcessor {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ProtocolService protocolService;

    private HashSetTimeDuplicateProcessor processor = new HashSetTimeDuplicateProcessor(1000, 60000L);

    @Override
    public String getCmd() {
        return CommandConstant.BROAD_CTX_SIGN_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        BroadCtxSignMessage realMessage = RPCUtil.getInstanceRpcStr(message, BroadCtxSignMessage.class);
        if (realMessage == null || realMessage.getLocalHash() == null) {
            return;
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        String hash = realMessage.getLocalHash().toHex();
        if (processor.insertAndCheck(nodeId + hash)) {
            protocolService.receiveCtxSign(chainId, nodeId, realMessage);
            chain.getLogger().info("A process ： " + nodeId + "," + hash);
        } else {
            chain.getLogger().info("A discard ： " + nodeId + "," + hash);
        }
    }
//
//    public static void main(String[] args) throws InterruptedException {
//        String key = "sdfesgjlsdflksdf";
//        HashSetTimeDuplicateProcessor processor = new HashSetTimeDuplicateProcessor(1000, 60000L);
//        boolean b = processor.insertAndCheck(key);
//        System.out.println(b);
//        b = processor.insertAndCheck(key);
//        System.out.println(b);
//        Thread.sleep(60001L);
//        b = processor.insertAndCheck(key);
//        System.out.println(b);
//        b = processor.insertAndCheck(key);
//        System.out.println(b);
//    }
}
