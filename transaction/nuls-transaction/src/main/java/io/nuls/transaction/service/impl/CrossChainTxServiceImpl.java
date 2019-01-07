package io.nuls.transaction.service.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxUnprocessedStorageService;
import io.nuls.transaction.message.BroadcastCrossNodeRsMessage;
import io.nuls.transaction.message.VerifyCrossResultMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxSignResult;
import io.nuls.transaction.rpc.call.ConsensusCall;
import io.nuls.transaction.service.CrossChainTxService;

import java.util.List;

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
        if(null != ctxExist){
            return;
        }
        ctxExist = crossChainTxStorageService.getTx(chainId, tx.getHash());
        if(null != ctxExist){
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
        if(null != crossChainTx){
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
    public void crossNodeResultProcess(Chain chain, BroadcastCrossNodeRsMessage message) throws NulsException {
        CrossChainTx ctx = getTx(chain, message.getRequestHash());
        if (ctx == null) {
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        //该节点的结果是否已经收到过, 收到过则忽略
        List<CrossTxSignResult> signRsList = ctx.getSignRsList();
        for(CrossTxSignResult crossTxSignResult : signRsList){
            if(crossTxSignResult.getAgentAddress().equals(message.getAgentAddress())){
                //已收到过
                return;
            }
        }
        String agentAddress = message.getAgentAddress();
        //todo 验证是否是共识节点.
        ConsensusCall.isConsensusNode(chain,agentAddress);
    }
}
