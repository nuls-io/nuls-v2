package io.nuls.transaction.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;

/**
 * 持久化在缓存中的验证通过(待打包)的交易
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface TransactionCacheStorageService {

    boolean putTx(Transaction tx);

    Transaction getTx(NulsDigestData hash);

    boolean removeTx(NulsDigestData hash);

    int getStartIndex();

    Transaction pollTx();
}
