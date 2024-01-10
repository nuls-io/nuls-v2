package io.nuls.transaction.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.utils.TxDuplicateRemoval;

import static io.nuls.transaction.constant.TxCmd.NW_RECEIVE_TX;
import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * Receive and process messages for complete transactions broadcasted by other nodes in the network
 */
@Component("BroadcastTxMessageHandlerV1")
public class BroadcastTxMessageHandler implements MessageProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxService txService;

    @Override
    public String getCmd() {
        return NW_RECEIVE_TX;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        Chain chain = null;
        try {
            chain = chainManager.getChain(chainId);
            //Based on block synchronization status,Decide whether to start processing transactions
            if(!chain.getProcessTxStatus().get()){
                return;
            }
            //Analyze new transaction messages
            BroadcastTxMessage message = RPCUtil.getInstanceRpcStr(msgStr, BroadcastTxMessage.class);
            if (message == null) {
                return;
            }
            Transaction transaction = message.getTx();
            String hash = transaction.getHash().toHex();
            //Does the transaction already exist in the transaction cachehash
            boolean rs = TxDuplicateRemoval.insertAndCheck(hash);
            //Record other network nodes that send complete transactions to this node and forward themhashTime exclusion
            TxDuplicateRemoval.putExcludeNode(hash, nodeId);
            if (!rs) {
                //The complete transaction has been received
                return;
            }
            //Put the transaction into the local transaction queue to be verified
            txService.newBroadcastTx(chainManager.getChain(chainId), new TransactionNetPO(transaction, nodeId));
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
