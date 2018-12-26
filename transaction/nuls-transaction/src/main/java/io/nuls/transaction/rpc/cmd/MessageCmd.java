package io.nuls.transaction.rpc.cmd;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.cache.TransactionDuplicateRemoval;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.message.GetTxMessage;
import io.nuls.transaction.message.TransactionMessage;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.service.TransactionService;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.transaction.constant.TxConstant.KEY_CHAIN_ID;
import static io.nuls.transaction.constant.TxConstant.KEY_MESSAGE_BODY;
import static io.nuls.transaction.constant.TxConstant.KEY_NODE_ID;

/**
 * 处理网络协议数据
 *
 * @author: qinyifeng
 * @date: 2018/12/26
 */
@Component
public class MessageCmd extends BaseCmd {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ConfirmedTransactionService confirmedTransactionService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TransactionManager transactionManager;

    /**
     * 接收广播的新交易hash
     * receive new transaction hash
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_NEW_HASH, version = 1.0, description = "receive new transaction hash")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response newHash(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result = false;
        try {
            //
            Integer chainId = Integer.parseInt(map.get(KEY_CHAIN_ID).toString());
            String nodeId = map.get(KEY_NODE_ID).toString();
            BroadcastTxMessage message = new BroadcastTxMessage();
            byte[] decode = HexUtil.decode(map.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            NulsDigestData hash = message.getRequestHash();
            boolean consains = TransactionDuplicateRemoval.mightContain(hash);
            if (consains) {
                return success();
            }
            TransactionDuplicateRemoval.insert(hash);
            GetTxMessage getTxMessage = new GetTxMessage();
            getTxMessage.setRequestHash(hash);
            result = NetworkCall.sendToNode(chainId, getTxMessage, nodeId);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }

    /**
     * 接收其他节点的新交易
     * receive new transactions from other nodes
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_RECEIVE_TX, version = 1.0, description = "receive new transactions from other nodes")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    public Response receiveTx(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result = false;
        try {
            Integer chainId = Integer.parseInt(map.get(KEY_CHAIN_ID).toString());
            TransactionMessage message = new TransactionMessage();
            byte[] decode = HexUtil.decode(map.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            Transaction transaction = message.getTx();
            //将交易放入待验证本地交易队列中
            result = transactionService.newTx(chainManager.getChain(chainId), transaction);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }
}
