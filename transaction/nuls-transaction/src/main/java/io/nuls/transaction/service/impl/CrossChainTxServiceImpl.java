package io.nuls.transaction.service.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxUnprocessedStorageService;
import io.nuls.transaction.message.BroadcastCrossNodeRsMessage;
import io.nuls.transaction.message.VerifyCrossResultMessage;
import io.nuls.transaction.message.base.BaseMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxSignResult;
import io.nuls.transaction.model.bo.CrossTxVerifyResult;
import io.nuls.transaction.rpc.call.AccountCall;
import io.nuls.transaction.rpc.call.ConsensusCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.CrossChainTxService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: qinyifeng
 * @date: 2018/12/19
 */
@Service
public class CrossChainTxServiceImpl implements CrossChainTxService {

    @Autowired
    private CrossChainTxStorageService crossChainTxStorageService;
    @Autowired
    private CrossChainTxUnprocessedStorageService crossChainTxUnprocessedStorageService;

    @Override
    public void newCrossTx(Chain chain, String nodeId, Transaction tx) {
        if (tx == null) {
            return;
        }
        int chainId = chain.getChainId();
        //判断是否存在
        CrossChainTx ctxExist = crossChainTxUnprocessedStorageService.getTx(chainId, tx.getHash());
        if (null != ctxExist) {
            return;
        }
        ctxExist = crossChainTxStorageService.getTx(chainId, tx.getHash());
        if (null != ctxExist) {
            return;
        }
        CrossChainTx ctx = new CrossChainTx();
        ctx.setTx(tx);
        ctx.setSenderChainId(chainId);
        ctx.setSenderNodeId(nodeId);
        ctx.setState(TxConstant.CTX_UNPROCESSED_0);
        crossChainTxUnprocessedStorageService.putTx(chain.getChainId(), ctx);
    }

    @Override
    public boolean removeTx(Chain chain, NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        return crossChainTxStorageService.removeTx(chain.getChainId(), hash);
    }

    @Override
    public CrossChainTx getTx(Chain chain, NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        return crossChainTxStorageService.getTx(chain.getChainId(), hash);
    }

    @Override
    public List<CrossChainTx> getTxList(Chain chain) {
        return crossChainTxStorageService.getTxList(chain.getChainId());
    }

    @Override
    public boolean updateCrossTxState(Chain chain, NulsDigestData hash, int state) {
        CrossChainTx crossChainTx = crossChainTxStorageService.getTx(chain.getChainId(), hash);
        if (null != crossChainTx) {
            chain.getLogger().error(hash.getDigestHex() + TxErrorCode.TX_NOT_EXIST.getMsg());
            return false;
        }
        crossChainTx.setState(state);
        return crossChainTxStorageService.putTx(chain.getChainId(), crossChainTx);
    }

    /**
     * 1.是否存在
     * 2.是否共识节点
     * 3.验证签名
     * 4.统计结果是否满足打包条件
     */
    @Override
    public boolean crossNodeResultProcess(Chain chain, BroadcastCrossNodeRsMessage message) throws NulsException {
        CrossChainTx ctx = getTx(chain, message.getRequestHash());
        if (ctx == null) {
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        //该节点的结果是否已经收到过, 收到过则忽略
        List<CrossTxSignResult> signRsList = ctx.getSignRsList();
        for (CrossTxSignResult crossTxSignResult : signRsList) {
            if (crossTxSignResult.getAgentAddress().equals(message.getAgentAddress())) {
                //已收到过
                return false;
            }
        }
        String agentAddress = message.getAgentAddress();
        //todo 验证是否是共识节点.
        ConsensusCall.isConsensusNode(chain, agentAddress);
        return true;
    }

    @Override
    public boolean ctxResultProcess(Chain chain, BaseMessage message, String nodeId) throws NulsException {
        if (message instanceof VerifyCrossResultMessage) {
            //处理跨链节点验证结果
            return verifyCrossResultProcess(chain, (VerifyCrossResultMessage) message, nodeId);
        } else if (message instanceof BroadcastCrossNodeRsMessage) {
            //处理链内节点验证结果
            return crossNodeResultProcess(chain, (BroadcastCrossNodeRsMessage) message);
        }
        return false;
    }

    /**
     * 处理跨链节点验证结果
     * @param chain
     * @param message
     * @param nodeId
     * @return
     * @throws NulsException
     */
    private boolean verifyCrossResultProcess(Chain chain, VerifyCrossResultMessage message, String nodeId) throws NulsException {
        //查询处理中的跨链交易
        CrossChainTx ctx = getTx(chain, message.getRequestHash());
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
        verifyResult.setChainId(chain.getChainId());
        verifyResult.setNodeId(nodeId);
        verifyResult.setHeight(message.getHeight());
        verifyResultList.add(verifyResult);
        ctx.setCtxVerifyResultList(verifyResultList);
        //TODO 获取共识节点的节点地址
        String agentAddress = "";
        //判断当前节点是共识节点还是普通节点
        if (ConsensusCall.isConsensusNode(chain, agentAddress)) {
            //共识节点
            double percent = ctx.getCtxVerifyResultList().size() / ctx.getVerifyNodeList().size() * 100;
            //超过全部链接节点51%的节点验证通过,则节点判定交易的验证通过
            if (percent >= 51) {
                //TODO 使用该地址到账户模块对跨链交易atx_trans_hash签名
                P2PHKSignature signature = AccountCall.signDigest(agentAddress, null, message.getRequestHash().getDigestHex());
                BroadcastCrossNodeRsMessage rsMessage = new BroadcastCrossNodeRsMessage();
                rsMessage.setCommand(TxCmd.NW_CROSS_NODE_RS);
                rsMessage.setRequestHash(message.getRequestHash());
                rsMessage.setSignature(signature);
                rsMessage.setAgentAddress(agentAddress);
                //广播交易hash
                NetworkCall.broadcast(chain.getChainId(), rsMessage);
                ctx.setState(TxConstant.CTX_VERIFY_RESULT_2);
            }
        } else {
            //普通节点
            if (verifyResultList.size() >= 3) {
                //广播交易hash
                NetworkCall.broadcastTxHash(chain.getChainId(), message.getRequestHash());
                ctx.setState(TxConstant.CTX_VERIFY_RESULT_2);
            }
        }

        //保存跨链交易验证结果
        crossChainTxStorageService.putTx(chain.getChainId(), ctx);
        return true;
    }
}
