package io.nuls.transaction.storage;

import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.po.TransactionConfirmedPO;

import java.util.List;

/**
 * 已打包进区块确认的交易(已确认交易)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface ConfirmedTxStorageService {

    /**
     * 保存交易数据
     * @param chainId
     * @param tx
     * @return
     */
    boolean saveTx(int chainId, TransactionConfirmedPO tx);

    /**
     * 批量保存交易
     * @param chainId
     * @param txList
     * @return
     */
    boolean saveTxList(int chainId, List<TransactionConfirmedPO> txList);

    /**
     * 获取交易数据
     * @param chainId 链ID
     * @param hash 交易hash
     * @return
     */
    TransactionConfirmedPO getTx(int chainId, byte[] hash);

    /**
     * 获取交易数据
     * @param chainId 链ID
     * @param hash 交易hash
     * @return
     */
    TransactionConfirmedPO getTx(int chainId, String hash);

    /**
     * 删除交易数据
     * @param chainId 链ID
     * @param hash 交易hash
     * @return
     */
    boolean removeTx(int chainId,byte[] hash);

    /**
     * 删除交易数据
     * @param chainId 链ID
     * @param hash 交易hash
     * @return
     */
    boolean removeTx(int chainId, String hash);

    /**
     * 根据交易hash批量删除已确认交易数据
     * @param chainId
     * @param hashList
     * @return 删除是否成功
     */
    boolean removeTxListByHashBytes(int chainId, List<byte[]> hashList);

    /**
     *
     * @param chainId
     * @param txList
     * @return
     */
    boolean removeTxList(int chainId, List<Transaction> txList);

    /**
     * 交易是否存在
     * @param chainId
     * @param hash
     * @return
     */
    boolean isExists(int chainId, byte[] hash);

}
