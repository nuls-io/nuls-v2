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
import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.cache.TransactionDuplicateRemoval;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.message.BroadcastCrossNodeRsMessage;
import io.nuls.transaction.message.BroadcastTxMessage;
import io.nuls.transaction.message.CrossTxMessage;
import io.nuls.transaction.message.GetTxMessage;
import io.nuls.transaction.message.TransactionMessage;
import io.nuls.transaction.message.VerifyCrossResultMessage;
import io.nuls.transaction.message.VerifyCrossWithFCMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxVerifyResult;
import io.nuls.transaction.rpc.call.AccountCall;
import io.nuls.transaction.rpc.call.ConsensusCall;
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
     * 接收链内广播的新交易hash
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
     * 获取完整链内交易数据,包含还未开始跨链处理的跨链交易
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
     * 接收链内其他节点的新的完整交易
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
     * (主网,友链都要处理)接收广播的新跨链交易hash
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
     * 获取完整开始跨链处理的跨链交易数据
     * get complete cross transaction data
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_ASK_CROSS_TX, version = 1.0, description = "get complete cross transaction data")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response askCrossTx(Map params) {
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
     * 接收友链节点发送的新的完整跨链交易
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
     * (友链处理)主网节点向友链节点验证跨链交易
     * verification of cross-chain transactions from home network node to friend chain node
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_VERIFY_FC, version = 1.0, description = "verification of cross-chain transactions from home network node to friend chain node")
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
            //TODO 验证该交易所在的区块已经被确认n个区块高度

            //TODO 将atx交易进行协议转换生成新的Anode2_atx_trans，再验证接收到的atx_trans_hash与Anode2_atx_trans_hash一致

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
     * (主网和友链都要处理)节点接收其他链节点发送的跨链验证结果
     * home network node receive cross-chain verify results sent by friend chain node
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_VERIFYR_ESULT, version = 1.0, description = "home network node receive cross-chain verify results sent by friend chain node")
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
            //判断当前节点是共识节点还是普通节点
            if (ConsensusCall.isConsensusNode()) {
                //共识节点
                double percent = ctx.getCtxVerifyResultList().size() / ctx.getVerifyNodeList().size() * 100;
                //超过全部链接节点51%的节点验证通过,则节点判定交易的验证通过
                if (percent >= 51) {
                    //TODO 获取共识节点的节点地址
                    String agentAddress = "";
                    //TODO 使用该地址到账户模块对跨链交易atx_trans_hash签名
                    byte[] signature = AccountCall.signDigest(agentAddress, null, message.getRequestHash().getDigestHex());
                    BroadcastCrossNodeRsMessage rsMessage = new BroadcastCrossNodeRsMessage();
                    rsMessage.setCommand(TxCmd.NW_CROSS_NODE_RS);
                    rsMessage.setRequestHash(message.getRequestHash());
                    rsMessage.setTransactionSignature(signature);
                    rsMessage.setAgentAddress(agentAddress);
                    //广播交易hash
                    NetworkCall.broadcast(chainId, rsMessage);
                    ctx.setState(TxConstant.CTX_VERIFY_RESULT_2);
                }
            } else {
                //普通节点
                if (verifyResultList.size() >= 3) {
                    //广播交易hash
                    NetworkCall.broadcastTxHash(chainId, message.getRequestHash());
                    ctx.setState(TxConstant.CTX_VERIFY_RESULT_2);
                }
            }

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

    /**
     * 友链节点向主网节点验证跨链交易
     * verification of cross-chain transactions from friend chain node to home network node
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_VERIFY_MN, version = 1.0, description = "verification of cross-chain transactions from friend chain node to home network node")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response verifyMn(Map params) {
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
            //TODO 验证该交易所在的区块已经被确认n个区块高度

            //TODO 将atx交易进行协议转换生成新的Anode2_atx_trans，再验证接收到的atx_trans_hash与Anode2_atx_trans_hash一致

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
     * 接收链内其他节点广播的跨链验证结果, 并保存.
     * 1.如果接收者是主网 当一个交易的签名者超过共识节点总数的80%，则通过
     * 2.如果接受者是友链 如果交易的签名者是友链最近x块的出块者
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = TxCmd.NW_CROSS_NODE_RS, version = 1.0, description = "friend chain node receive cross-chain verify results sent by home network node")
    @Parameter(parameterName = KEY_CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = KEY_NODE_ID, parameterType = "String")
    public Response crossNodeRs(Map params) {
        Map<String, Boolean> map = new HashMap<>();
        boolean result;
        try {
            ObjectUtils.canNotEmpty(params.get(KEY_CHAIN_ID), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get(KEY_NODE_ID), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get(KEY_MESSAGE_BODY), TxErrorCode.PARAMETER_ERROR.getMsg());

            Chain chain = chainManager.getChain((int) params.get(KEY_CHAIN_ID));
            if(null == chain){
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }

            String nodeId = (String)params.get(KEY_NODE_ID);
            //解析验证结果消息
            BroadcastCrossNodeRsMessage message = new BroadcastCrossNodeRsMessage();
            byte[] decode = HexUtil.decode((String)params.get(KEY_MESSAGE_BODY));
            message.parse(new NulsByteBuffer(decode));
            //查询处理中的跨链交易
 /*           CrossChainTx ctx = crossChainTxStorageService.getTx(chain.getChainId(), message.getRequestHash());

            if (ctx == null) {
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }

            //获取跨链交易验证结果
            List<CrossTxVerifyResult> verifyResultList = ctx.getCtxVerifyResultList();
            if (verifyResultList == null) {
                verifyResultList = new ArrayList<>();
            }
            //添加新的跨链验证结果
//            CrossTxVerifyResult verifyResult = new CrossTxVerifyResult();
//            verifyResult.setChainId(chainId);
//            verifyResult.setNodeId(nodeId);
//            verifyResult.setHeight(message.getHeight());
//            verifyResultList.add(verifyResult);
//            ctx.setCtxVerifyResultList(verifyResultList);
//            ctx.setState(TxConstant.CTX_VERIFY_RESULT_2);
            //保存跨链交易验证结果
            result = crossChainTxStorageService.putTx(chainId, ctx);*/
        } catch (NulsException e) {
            Log.error(e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        //map.put("value", result);
        return success(map);
    }

}
