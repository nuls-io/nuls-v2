package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.bo.TxWrapper;

/**
 * 验证通过但未打包的交易
 * Save verified transaction (unpackaged)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface TxVerifiedStorageService {

    boolean putTx(TxWrapper txWrapper);

    Transaction getTx(int chainId,NulsDigestData hash);

    boolean removeTx(int chainId,NulsDigestData hash);
}
