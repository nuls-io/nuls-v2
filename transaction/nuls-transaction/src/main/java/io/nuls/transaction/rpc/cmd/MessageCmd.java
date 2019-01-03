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
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.message.CrossTxMessage;
import io.nuls.transaction.message.GetTxMessage;
import io.nuls.transaction.message.TransactionMessage;
import io.nuls.transaction.message.VerifyCrossResultMessage;
import io.nuls.transaction.message.VerifyCrossWithFCMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxVerifyResult;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.ConfirmedTransactionService;
import io.nuls.transaction.service.CrossChainTxService;
import io.nuls.transaction.service.TransactionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.constant.TxCmd.NW_NEW_MN_TX;
import static io.nuls.transaction.constant.TxCmd.NW_VERIFYR_ESULT;
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
    private CrossChainTxService crossChainTxService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ConfirmedTransactionService confirmedTransactionService;
    @Autowired
    private CrossChainTxStorageService crossChainTxStorageService;
    @Autowired
    private ChainManager chainManager;

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
            //解析广播交易hash消息
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
            //解析获取完整交易消息
            GetTxMessage message = new GetTxMessage();
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            Chain chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            NulsDigestData txHash = message.getRequestHash();
            Transaction tx = confirmedTransactionService.getConfirmedTransaction(chain, txHash);
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
            //解析新的交易消息
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
            //解析广播跨链交易hash消息
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
            //解析获取完整跨链交易消息
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
            Transaction tx = confirmedTransactionService.getConfirmedTransaction(chain, txHash);
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
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response newMnTx(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            //解析新的跨链交易消息
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
            //保存未验证跨链交易
            crossChainTxService.newCrossTx(chainManager.getChain(chainId), nodeId, transaction);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", true);
        return success(map);
    }

    /**
     * 友链节点验证跨链交易
     * friendly chain nodes verify cross-chain transactions
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_VERIFY_FC, version = 1.0, description = "friendly chain nodes verify cross-chain transactions")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response verifyFc(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            //解析跨链交易验证消息
            VerifyCrossWithFCMessage message = new VerifyCrossWithFCMessage();
            byte[] decode = HexUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            //解析原始交易hash
            byte[] origTxHashByte = message.getOriginalTxHash();
            NulsDigestData originalTxHash = NulsDigestData.fromDigestHex(HexUtil.encode(origTxHashByte));
            //查询已确认跨链交易
            Transaction tx = confirmedTransactionService.getConfirmedTransaction(chainManager.getChain(chainId), originalTxHash);
            if (tx == null) {
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            //发送跨链交易验证结果到指定节点
            VerifyCrossResultMessage verifyResultMessage = new VerifyCrossResultMessage();
            verifyResultMessage.setCommand(NW_VERIFYR_ESULT);
            verifyResultMessage.setRequestHash(message.getRequestHash());
            verifyResultMessage.setHeight(tx.getBlockHeight());
            result = NetworkCall.sendToNode(chainId, verifyResultMessage, nodeId);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }

    /**
     * 接收友链发送的跨链验证结果
     * receive cross-chain verify results sent by friend chain
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_VERIFYR_ESULT, version = 1.0, description = "receive cross-chain verify results sent by friend chain")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response verifyResult(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        try {
            Integer chainId = Integer.parseInt(params.get(KEY_CHAIN_ID).toString());
            String nodeId = params.get(KEY_NODE_ID).toString();
            //解析跨链交易验证结果消息
            VerifyCrossResultMessage message = new VerifyCrossResultMessage();
            byte[] decode = HexUtil.decode(params.get(KEY_MESSAGE_BODY).toString());
            message.parse(new NulsByteBuffer(decode));
            if (message == null) {
                return failed(TxErrorCode.PARAMETER_ERROR);
            }
            //查询处理中的跨链交易
            CrossChainTx ctx = crossChainTxStorageService.getTx(chainId, message.getRequestHash());
            if (ctx == null) {
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            //获取跨链交易验证结果
            List<CrossTxVerifyResult> verifyResultList = ctx.getCtxVerifyResultList();
            if (verifyResultList == null) {
                verifyResultList = new ArrayList<>();
            }
            //添加新的跨链验证结果
            CrossTxVerifyResult verifyResult = new CrossTxVerifyResult();
            verifyResult.setChainId(chainId);
            verifyResult.setNodeId(nodeId);
            verifyResult.setHeight(message.getHeight());
            verifyResultList.add(verifyResult);
            ctx.setCtxVerifyResultList(verifyResultList);
            //保存跨链交易验证结果
            result = crossChainTxStorageService.putTx(chainId, ctx);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put("value", result);
        return success(map);
    }

}
