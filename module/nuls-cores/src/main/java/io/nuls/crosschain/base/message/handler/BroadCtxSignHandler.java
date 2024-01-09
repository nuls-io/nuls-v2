package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.service.ProtocolService;

/**
 * BroadCtxSignMessage处理类
 * BroadCtxSignMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

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
        protocolService.receiveCtxSign(chainId, nodeId, realMessage);
    }
}
