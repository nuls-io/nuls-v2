package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.message.CtxFullSignMessage;
import io.nuls.crosschain.base.service.ProtocolService;

/**
 * @description TODO
 * @date 2024/6/17 14:47
 * @COPYRIGHT nabox.io
 */
@Component("CtxFullSignHandlerV1")
public class CtxFullSignHandler implements MessageProcessor {

    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.CROSS_CTX_FULL_SIGN_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        CtxFullSignMessage ctxFullSignMessage = RPCUtil.getInstanceRpcStr(message, CtxFullSignMessage.class);
        protocolService.receiveCtxFullSign(chainId, nodeId, ctxFullSignMessage);
    }
}
