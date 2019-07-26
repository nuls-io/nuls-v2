package io.nuls.transaction.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.NulsHash;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.message.ForwardTxMessage;
import io.nuls.transaction.message.GetTxMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.utils.TxDuplicateRemoval;

import static io.nuls.transaction.constant.TxCmd.NW_ASK_TX;
import static io.nuls.transaction.constant.TxCmd.NW_NEW_HASH;
import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 接收处理网络中其他节点转发的交易hash的消息
 */
@Component("ForwardTxMessageHandlerV1")
public class ForwardTxMessageHandler implements MessageProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxService txService;

    @Override
    public String getCmd() {
        return NW_NEW_HASH;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        Chain chain = null;
        try {
            chain = chainManager.getChain(chainId);
            //根据区块同步状态,决定是否开始处理交易hash
            if(!chain.getProcessTxStatus().get()){
                return;
            }
            //解析广播交易hash消息
            ForwardTxMessage message = RPCUtil.getInstanceRpcStr(msgStr, ForwardTxMessage.class);
            if (message == null) {
                return;
            }
            NulsHash hash = message.getTxHash();
//            chain.getLoggerMap().get(TxConstant.LOG_TX_MESSAGE).debug(
//                    "recieve [newHash] message from node-{}, chainId:{}, hash:{}", nodeId, chainId, hash.toHex());
            //只判断是否存在
            String hashHex = hash.toHex();
            if (TxDuplicateRemoval.exist(hashHex)) {
                TxDuplicateRemoval.putExcludeNode(hashHex, nodeId);
                return;
            }
            //去该节点查询完整交易
            GetTxMessage getTxMessage = new GetTxMessage();
            getTxMessage.setTxHash(hash);
            NetworkCall.sendToNode(chain, getTxMessage, nodeId, NW_ASK_TX);
        } catch (Exception e) {
            errorLogProcess(chain, e);
        }
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }
}
