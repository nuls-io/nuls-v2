package io.nuls.crosschain.base.message.handler;

import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;

/**
 * @description TODO
 * @date 2024/6/17 14:47
 * @COPYRIGHT nabox.io
 */
@Component("CtxFullSignHandlerV1")
public class CtxFullSignHandler implements MessageProcessor {

    @Override
    public String getCmd() {
        return CommandConstant.CROSS_CTX_FULL_SIGN_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {

    }
}
