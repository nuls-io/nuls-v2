package io.nuls.transaction.service;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
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
    boolean newCrossTx(Chain chain, int nodeId, Transaction tx);

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
     * @param chain
     * @param hash
     * @param state
     * @return
     */
    boolean updateCrossTxState(Chain chain, NulsDigestData hash, int state);

}
