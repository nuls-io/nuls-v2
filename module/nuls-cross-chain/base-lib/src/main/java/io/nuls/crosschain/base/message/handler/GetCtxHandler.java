package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.GetCtxMessage;
import io.nuls.crosschain.base.service.ProtocolService;

/**
 * GetCtxMessage处理类
 * GetCtxMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("GetCtxHandlerV1")
public class GetCtxHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.GET_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetCtxMessage.class);
        if (message == null) {
            return;
        }
        protocolService.getCtx(chainId, nodeId, realMessage);
    }
}
