package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.bo.TxWrapper;

/**
 * 已打包进区块确认的交易
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface TransactionStorageService {

    /**
     * 保存交易数据
     * @param txWrapper 交易数据封装对象
     * @return
     */
    boolean saveTx(TxWrapper txWrapper);

    /**
     * 获取交易数据
     * @param chainId 链ID
     * @param hash 交易hash
     * @return
     */
    Transaction getTx(int chainId,NulsDigestData hash);

    /**
     * 删除交易数据
     * @param chainId 链ID
     * @param hash 交易hash
     * @return
     */
    boolean removeTx(int chainId,NulsDigestData hash);
}
