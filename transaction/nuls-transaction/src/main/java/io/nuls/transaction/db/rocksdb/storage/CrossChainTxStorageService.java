package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.transaction.model.bo.CrossChainTx;

import java.util.List;

/**
 * 验证过程中的跨链交易
 * Cross-chain transaction in verification
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface CrossChainTxStorageService {

    /**
     * 新增或修改跨链交易数据
     *
     * @param chainId
     * @param ctx
     * @return
     */
    boolean putTx(int chainId, CrossChainTx ctx);

    /**
     * 批量新增跨链交易数据
     *
     * @param chainId
     * @param ctxList
     * @return
     */
    boolean putTxs(int chainId, List<CrossChainTx> ctxList);

    /**
     * 删除跨链交易
     *
     * @param chainId
     * @param hash
     * @return
     */
    boolean removeTx(int chainId, NulsDigestData hash);

    /**
     * 根据交易哈希查询跨链交易
     *
     * @param chainId
     * @param hash
     * @return
     */
    CrossChainTx getTx(int chainId, NulsDigestData hash);

    /**
     * 查询指定链下所有跨链交易
     * Query all cross-chain transactions in the specified chain
     *
     * @param chainId
     * @return
     */
    List<CrossChainTx> getTxList(int chainId);

}
