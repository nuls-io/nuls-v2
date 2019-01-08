package io.nuls.transaction.service;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.message.BroadcastCrossNodeRsMessage;
import io.nuls.transaction.message.VerifyCrossResultMessage;
import io.nuls.transaction.message.base.BaseMessage;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;

import java.util.List;

/**
 * 验证过程中的跨链交易
 * Cross-chain transaction in verification
 *
 * @author: qinyifeng
 * @date: 2018/12/19
 */
public interface CrossChainTxService {

    /**
     * 接收其他链新的跨链交易
     *
     * @param chain
     * @param tx
     * @return
     */
    void newCrossTx(Chain chain, String nodeId, Transaction tx);

    /**
     * 删除跨链交易
     *
     * @param chain
     * @param hash
     * @return
     */
    boolean removeTx(Chain chain, NulsDigestData hash);

    /**
     * 根据交易哈希查询跨链交易
     *
     * @param chain
     * @param hash
     * @return
     */
    CrossChainTx getTx(Chain chain, NulsDigestData hash);

    /**
     * 查询指定链下所有跨链交易
     * Query all cross-chain transactions in the specified chain
     *
     * @param chain
     * @return
     */
    List<CrossChainTx> getTxList(Chain chain);

    /**
     * 更新跨链交易在跨链过程中的验证状态
     *
     * @param chain
     * @param hash
     * @param state
     * @return
     */
    boolean updateCrossTxState(Chain chain, NulsDigestData hash, int state);


    /**
     * 接收链内其他节点广播的跨链验证结果, 并保存.
     * 1.如果接收者是主网 当一个交易的签名者超过共识节点总数的80%，则通过
     * 2.如果接受者是友链 如果交易的签名者是友链最近x块的出块者
     *
     * @param chain
     * @param message
     * @throws NulsException
     */
    boolean crossNodeResultProcess(Chain chain, BroadcastCrossNodeRsMessage message) throws NulsException;

    /**
     * 接收跨链和链内其他节点广播的跨链验证结果, 并保存.
     * 1.VerifyCrossResultMessage：接收跨链节点验证结果
     * 2.BroadcastCrossNodeRsMessage：接收链内节点验证结果
     *
     * @param chain
     * @param message
     * @param nodeId
     */
    boolean ctxResultProcess(Chain chain, BaseMessage message, String nodeId) throws NulsException;

}
