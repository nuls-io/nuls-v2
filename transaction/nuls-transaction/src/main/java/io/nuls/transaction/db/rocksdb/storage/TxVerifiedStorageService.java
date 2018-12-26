package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;

import java.util.List;

/**
 * 验证通过但未打包的交易
 * Save verified transaction (unpackaged)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface TxVerifiedStorageService {

    /**
     * 保存已验证交易
     *
     * @param chainId
     * @param tx
     * @return 保存是否成功
     */
    boolean putTx(int chainId, Transaction tx);

    /**
     * 根据交易hash查询已验证交易数据
     *
     * @param chainId
     * @param hash
     * @return 交易数据
     */
    Transaction getTx(int chainId, NulsDigestData hash);

    /**
     * 根据交易hash删除已验证交易数据
     *
     * @param chainId
     * @param hash
     * @return 删除是否成功
     */
    boolean removeTx(int chainId, NulsDigestData hash);

    /**
     * 根据交易hash批量查询已验证交易数据
     *
     * @param chainId
     * @param hashList NulsDigestData serialize data
     * @return 交易数据列表
     */
    List<Transaction> getTxList(int chainId, List<byte[]> hashList);

    /**
     * 根据交易hash批量删除已验证交易数据
     * @param chainId
     * @param hashList NulsDigestData serialize data
     * @return 删除是否成功
     */
    boolean removeTxList(int chainId, List<byte[]> hashList);
}
