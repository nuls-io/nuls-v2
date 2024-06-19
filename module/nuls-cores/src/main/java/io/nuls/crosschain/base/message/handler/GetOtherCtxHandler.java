package io.nuls.crosschain.base.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.GetOtherCtxMessage;
import io.nuls.crosschain.base.service.ProtocolService;
import io.nuls.crosschain.base.utils.HashSetTimeDuplicateProcessor;

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
    private ProtocolService protocolService;

    private HashSetTimeDuplicateProcessor processor = new HashSetTimeDuplicateProcessor(1000, 300000L);

    @Override
    public String getCmd() {
        return CommandConstant.GET_OTHER_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetOtherCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetOtherCtxMessage.class);
        if (message == null) {
            return;
        }
        if (processor.insertAndCheck(nodeId + realMessage.getRequestHash().toHex())) {
            protocolService.getOtherCtx(chainId, nodeId, realMessage);
        }

    }
}
