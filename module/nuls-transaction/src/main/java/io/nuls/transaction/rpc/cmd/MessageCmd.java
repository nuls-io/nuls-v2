package io.nuls.transaction.rpc.cmd;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.protocol.MessageHandler;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.transaction.cache.TxDuplicateRemoval;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.message.ForwardTxMessage;
import io.nuls.transaction.message.GetTxMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.transaction.constant.TxConstant.*;
import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 网络消息处理
 * @author: Charlie
 * @date: 2019/04/16
 */
@Service
public class MessageCmd extends BaseCmd {
    @Autowired
    private TxService txService;
    @Autowired
    private ConfirmedTxService confirmedTxService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxConfig txConfig;

    /**
     * 接收链内广播的新交易hash
     * receive new transaction hash
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_NEW_HASH, version = 1.0, description = "receive new transaction hash")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    @MessageHandler(message = ForwardTxMessage.class)
    public Response newHash(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        Chain chain = null;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            chain = chainManager.getChain(chainId);
            String nodeId = params.get(KEY_NODE_ID).toString();
            //解析广播交易hash消息
            ForwardTxMessage message = new ForwardTxMessage();
            byte[] decode = RPCUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            NulsDigestData hash = message.getHash();
            chain.getLoggerMap().get(TxConstant.LOG_TX_MESSAGE).debug(
                    "recieve [newHash] message from node-{}, chainId:{}, hash:{}", nodeId, chainId, hash.getDigestHex());
            //交易缓存中是否已存在该交易hash
            boolean consains = TxDuplicateRemoval.mightContain(hash);
            if (consains) {
                return success();
            }
            //如果交易hash不存在，则添加到缓存中
            TxDuplicateRemoval.insert(hash);
            //去该节点查询完整交易
            GetTxMessage getTxMessage = new GetTxMessage();
            getTxMessage.setCommand(TxCmd.NW_ASK_TX);
            getTxMessage.setRequestHash(hash);
            result = NetworkCall.sendToNode(chainId, getTxMessage, nodeId);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }

    /**
     * 获取完交易数据
     * get complete transaction entity
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_ASK_TX, version = 1.0, description = "get complete transaction entity")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    @MessageHandler(message = GetTxMessage.class)
    public Response askTx(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        Chain chain = null;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            //解析获取完整交易消息
            GetTxMessage message = new GetTxMessage();
            byte[] decode = RPCUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            chain = chainManager.getChain(chainId);
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            NulsDigestData txHash = message.getRequestHash();
            chain.getLoggerMap().get(TxConstant.LOG_TX_MESSAGE).debug(
                    "recieve [askTx] message from node-{}, chainId:{}, hash:{}", nodeId, chainId, txHash.getDigestHex());
            TransactionConfirmedPO tx = txService.getTransaction(chain, txHash);
            if (tx == null) {
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            result = NetworkCall.sendTxToNode(chainId, nodeId, tx.getTx());
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }

    /**
     * 接收链内其他节点的新的完整交易
     * receive new transactions from other nodes
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_RECEIVE_TX, version = 1.0, description = "receive new transactions from other nodes")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    @MessageHandler(message = BroadcastTxMessage.class)
    public Response receiveTx(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        Chain chain = null;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            chain = chainManager.getChain(chainId);
            //解析新的交易消息
            BroadcastTxMessage message = new BroadcastTxMessage();
            byte[] decode = RPCUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            Transaction transaction = message.getTx();
            chain.getLoggerMap().get(TxConstant.LOG_TX_MESSAGE).debug(
                    "recieve [receiveTx] message from node-{}, chainId:{}, hash:{}", nodeId, chainId, transaction.getHash().getDigestHex());
            //交易缓存中是否已存在该交易hash
            boolean consains = TxDuplicateRemoval.mightContain(transaction.getHash());
            if (!consains) {
                //添加到交易缓存中
                TxDuplicateRemoval.insert(transaction.getHash());
            }
            //将交易放入待验证本地交易队列中
            txService.newBroadcastTx(chainManager.getChain(chainId), new TransactionNetPO(transaction, nodeId));
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", true);
        return success(map);
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLoggerMap().get(TxConstant.LOG_TX_MESSAGE).error(e);
        }
    }
}
