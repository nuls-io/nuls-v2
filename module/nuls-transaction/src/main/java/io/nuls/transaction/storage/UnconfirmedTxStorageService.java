package io.nuls.transaction.storage;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.model.po.TransactionUnconfirmedPO;

import java.util.List;

/**
 * 验证通过但未打包的交易(未确认交易)
 * Save verified transaction (unpackaged)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface UnconfirmedTxStorageService {

    /**
     * 保存已验证交易
     *
     * @param chainId
     * @param tx
     * @return 保存是否成功
     */
    boolean putTx(int chainId, Transaction tx, long originalSendNanoTime);

    boolean putTx(int chainId, Transaction tx);


    /**
     * 批量保存未确认交易
     * @param chainId
     * @param txNetPOList
     * @return
     */
    boolean putTxList(int chainId, List<TransactionNetPO> txNetPOList);

    /**
     * 根据交易hash查询已验证交易数据
     *
     * @param chainId
     * @param hash
     * @return 交易数据
     */
    TransactionUnconfirmedPO getTx(int chainId, NulsHash hash);

    /**
     * 判断交易是否在未确认交易数据库中存在
     * @param chainId
     * @param hash
     * @return
     */
    boolean isExists(int chainId, NulsHash hash);

    /**
     * 根据交易hash查询已验证交易数据
     *
     * @param chainId
     * @param hash
     * @return 交易数据
     */
    TransactionUnconfirmedPO getTx(int chainId, String hash);

    /**
     * 根据交易hash删除已验证交易数据
     *
     * @param chainId
     * @param hash
     * @return 删除是否成功
     */
    boolean removeTx(int chainId, NulsHash hash);

    boolean removeTx(int chainId, byte[] hash);

    /**
     * 根据交易hash批量查询已验证交易数据
     *
     * @param chainId
     * @param hashList NulsHash serialize entity
     * @return 交易数据列表
     */
    List<Transaction> getTxList(int chainId, List<byte[]> hashList);

    /**
     * 根据交易hash批量删除已验证交易数据
     *
     * @param chainId
     * @param hashList NulsHash serialize entity
     * @return 删除是否成功
     */
    boolean removeTxList(int chainId, List<byte[]> hashList);

    /**
     * 查询所有未确认交易的key
     *
     * @param chainId
     * @return
     */
    List<byte[]> getAllTxkeyList(int chainId);

    /**
     * 查询未确认交易数据，包含保存时间
     *
     * @param chainId
     * @return
     */
    List<TransactionUnconfirmedPO> getTransactionUnconfirmedPOList(int chainId, List<byte[]> hashList);

    /**
     * 根据hash 获取存在的key
     * @param chainId
     * @param hashList
     * @return
     */
    List<byte[]> getExistKeys(int chainId, List<byte[]> hashList);

    /**
     * 根据hash 获取存在的key
     * @param chainId
     * @param hashList
     * @return
     */
    List<String> getExistKeysStr(int chainId, List<byte[]> hashList);
}
