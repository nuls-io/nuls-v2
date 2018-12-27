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
import io.nuls.transaction.message.CrossTxMessage;
import io.nuls.transaction.message.GetTxMessage;
import io.nuls.transaction.message.TransactionMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.service.TransactionService;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.transaction.constant.TxCmd.*;
import static io.nuls.transaction.constant.TxConstant.*;

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
        boolean result;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            BroadcastTxMessage message = new BroadcastTxMessage();
            byte[] decode = HexUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            NulsDigestData hash = message.getRequestHash();
            //交易缓存中是否已存在该交易hash
            boolean consains = TransactionDuplicateRemoval.mightContain(hash);
            if (consains) {
                return success();
            }
            //如果交易hash不存在，则添加到缓存中
            TransactionDuplicateRemoval.insert(hash);
            //去该节点查询完整交易
            GetTxMessage getTxMessage = new GetTxMessage();
            getTxMessage.setCommand(TxCmd.NW_ASK_TX);
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
     * 获取完整交易数据
     * get complete transaction data
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_ASK_TX, version = 1.0, description = "get complete transaction data")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response askTx(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            GetTxMessage message = new GetTxMessage();
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            NulsDigestData txHash = message.getRequestHash();
            Transaction tx = confirmedTransactionService.getTransaction(chain, txHash);
            if (tx == null) {
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            result = NetworkCall.sendTxToNode(chainId, nodeId, tx);
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
        boolean result;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            TransactionMessage message = new TransactionMessage();
            byte[] decode = HexUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            Transaction transaction = message.getTx();
            //交易缓存中是否已存在该交易hash
            boolean consains = TransactionDuplicateRemoval.mightContain(transaction.getHash());
            if (!consains) {
                //添加到交易缓存中
                TransactionDuplicateRemoval.insert(transaction.getHash());
            }
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

    /**
     * 接收广播的新跨链交易hash
     * receive new cross transaction hash
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_NEW_CROSS_HASH, version = 1.0, description = "receive new cross transaction hash")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response newCrossHash(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            BroadcastTxMessage message = new BroadcastTxMessage();
            byte[] decode = HexUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            NulsDigestData hash = message.getRequestHash();
            //交易缓存中是否已存在该交易hash
            boolean consains = TransactionDuplicateRemoval.mightContain(hash);
            if (consains) {
                return success();
            }
            //如果交易hash不存在，则添加到缓存中
            TransactionDuplicateRemoval.insert(hash);
            //去该节点查询完整跨链交易
            GetTxMessage getTxMessage = new GetTxMessage();
            getTxMessage.setCommand(TxCmd.NW_ASK_CROSS_TX);
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
     * 获取完整跨链交易数据
     * get complete cross transaction data
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_ASK_CROSS_TX, version = 1.0, description = "get complete cross transaction data")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response askCrossTxaskTx(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            GetTxMessage message = new GetTxMessage();
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            //查询已确认跨链交易
            NulsDigestData txHash = message.getRequestHash();
            Transaction tx = confirmedTransactionService.getTransaction(chain, txHash);
            if (tx == null) {
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            //发送跨链交易到指定节点
            CrossTxMessage crossTxMessage = new CrossTxMessage();
            crossTxMessage.setCommand(NW_NEW_MN_TX);
            crossTxMessage.setTx(tx);
            result = NetworkCall.sendToNode(chainId, crossTxMessage, nodeId);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }

    /**
     * 接收其他节点的新跨链交易
     * receive new cross transactions from other nodes
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_NEW_MN_TX, version = 1.0, description = "receive new cross transactions from other nodes")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    public Response newMnTx(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result = false;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            CrossTxMessage message = new CrossTxMessage();
            byte[] decode = HexUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            Transaction transaction = message.getTx();
            //交易缓存中是否已存在该交易hash
            boolean consains = TransactionDuplicateRemoval.mightContain(transaction.getHash());
            if (!consains) {
                //添加到交易缓存中
                TransactionDuplicateRemoval.insert(transaction.getHash());
            }
            //TODO 验证跨链交易
            //result = transactionService.newTx(chainManager.getChain(chainId), transaction);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }
}
