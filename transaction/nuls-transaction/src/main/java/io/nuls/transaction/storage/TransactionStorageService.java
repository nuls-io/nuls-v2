package io.nuls.transaction.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;

/**
 * 已打包进区块确认的交易
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface TransactionStorageService {

    boolean saveTx(Transaction tx);

    Transaction getTx(NulsDigestData hash);

    boolean removeTx(NulsDigestData hash);
}
