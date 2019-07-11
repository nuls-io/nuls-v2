package io.nuls.transaction.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.NulsHash;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.message.GetTxMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;

import static io.nuls.transaction.constant.TxCmd.NW_ASK_TX;
import static io.nuls.transaction.utils.LoggerUtil.LOG;

@Component("GetTxMessageHandlerV1")
public class GetTxMessageHandler implements MessageProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxService txService;

    @Override
    public String getCmd() {
        return NW_ASK_TX;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        Chain chain = null;
        try {
            //解析获取完整交易消息
            GetTxMessage message = RPCUtil.getInstanceRpcStr(msgStr, GetTxMessage.class);
            if (message == null) {
                return;
            }
            chain = chainManager.getChain(chainId);
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            NulsHash txHash = message.getTxHash();
            TransactionConfirmedPO tx = txService.getTransaction(chain, txHash);
            if (tx == null) {
                chain.getLogger().debug("recieve [askTx] message from node-{}, chainId:{}, hash:{}", nodeId, chainId, txHash.toHex());
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            NetworkCall.sendTxToNode(chain, nodeId, tx.getTx(), tx.getOriginalSendNanoTime());
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
