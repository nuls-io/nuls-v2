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

import java.util.concurrent.atomic.AtomicInteger;

import static io.nuls.transaction.constant.TxCmd.NW_RECEIVE_TX;
import static io.nuls.transaction.utils.LoggerUtil.LOG;

@Component("BroadcastTxMessageHandlerV1")
public class BroadcastTxMessageHandler implements MessageProcessor {

    /**
     * 接收网络新交易
     */
    public static AtomicInteger countRc = new AtomicInteger(0);

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
            //解析新的交易消息
            BroadcastTxMessage message = RPCUtil.getInstanceRpcStr(msgStr, BroadcastTxMessage.class);
            if (message == null) {
                return;
            }
            Transaction transaction = message.getTx();
            //交易缓存中是否已存在该交易hash
            boolean rs = TxDuplicateRemoval.insertAndCheck(transaction.getHash().toHex());
            if (!rs) {
                //该完整交易已经收到过
                return;
            }
            countRc.incrementAndGet();
            //将交易放入待验证本地交易队列中
            txService.newBroadcastTx(chainManager.getChain(chainId), new TransactionNetPO(transaction, nodeId, message.getSendNanoTime()));
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
