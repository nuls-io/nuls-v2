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
     * @param ctx
     * @return
     */
    boolean putTx(CrossChainTx ctx);

    /**
     * 删除跨链交易
     * @param hash
     * @return
     */
    boolean removeTx(NulsDigestData hash);

    /**
     * 根据交易哈希查询跨链交易
     * @param hash
     * @return
     */
    CrossChainTx getTx(NulsDigestData hash);

    /**
     * 查询所有跨链交易
     * @return
     */
    List<CrossChainTx> getAllTx();
}
