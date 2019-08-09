package io.nuls.transaction.storage;

import io.nuls.base.data.NulsHash;
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
    TransactionConfirmedPO getTx(int chainId, NulsHash hash);

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
    boolean removeTx(int chainId,NulsHash hash);

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
    boolean isExists(int chainId, NulsHash hash);

    /**
     * 根据交易hash批量查询已确认交易数据
     *
     * @param chainId
     * @param hashList NulsHash serialize entity
     * @return 交易数据列表
     */
    List<Transaction> getTxList(int chainId, List<byte[]> hashList);


    /**
     * 根据hash 获取存在的key
     * @param chainId
     * @param hashList
     * @return List<byte[]>
     */
    List<byte[]> getExistKeys(int chainId, List<byte[]> hashList);


    /**
     * 根据hash 获取存在的key
     * @param chainId
     * @param hashList
     * @return List<String>
     */
    List<String> getExistKeysStr(int chainId, List<byte[]> hashList);

}
