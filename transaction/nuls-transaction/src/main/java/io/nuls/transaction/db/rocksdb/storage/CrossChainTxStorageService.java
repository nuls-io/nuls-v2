package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.transaction.model.bo.CrossChainTx;

/**
 * 验证过程中的跨链交易
 * Cross-chain transaction in verification
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface CrossChainTxStorageService {

    boolean putTx(CrossChainTx ctx);

    CrossChainTx getTx(NulsDigestData hash);

    boolean removeTx(NulsDigestData hash);
}
