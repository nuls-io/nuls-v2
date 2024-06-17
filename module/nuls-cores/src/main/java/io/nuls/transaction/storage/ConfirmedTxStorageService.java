package io.nuls.transaction.storage;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.po.TransactionConfirmedPO;

import java.util.List;

/**
 * Transactions that have been packaged into block confirmation(Confirmed transaction)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface ConfirmedTxStorageService {

    /**
     * Save transaction data
     * @param chainId
     * @param tx
     * @return
     */
    boolean saveTx(int chainId, TransactionConfirmedPO tx);

    /**
     * Batch save transactions
     * @param chainId
     * @param txList
     * @return
     */
    boolean saveTxList(int chainId, List<TransactionConfirmedPO> txList);

    /**
     * Obtain transaction data
     * @param chainId chainID
     * @param hash transactionhash
     * @return
     */
    TransactionConfirmedPO getTx(int chainId, NulsHash hash);

    /**
     * Obtain transaction data
     * @param chainId chainID
     * @param hash transactionhash
     * @return
     */
    TransactionConfirmedPO getTx(int chainId, String hash);

    /**
     * Delete transaction data
     * @param chainId chainID
     * @param hash transactionhash
     * @return
     */
    boolean removeTx(int chainId,NulsHash hash);

    /**
     * Delete transaction data
     * @param chainId chainID
     * @param hash transactionhash
     * @return
     */
    boolean removeTx(int chainId, String hash);

    /**
     * According to the transactionhashBatch deletion of confirmed transaction data
     * @param chainId
     * @param hashList
     * @return Whether the deletion was successful
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
     * Does the transaction exist
     * @param chainId
     * @param hash
     * @return
     */
    boolean isExists(int chainId, NulsHash hash);

    /**
     * According to the transactionhashBatch query of confirmed transaction data
     *
     * @param chainId
     * @param hashList NulsHash serialize entity
     * @return Transaction Data List
     */
    List<Transaction> getTxList(int chainId, List<byte[]> hashList);


    /**
     * according tohash Obtain existingkey
     * @param chainId
     * @param hashList
     * @return List<byte[]>
     */
    List<byte[]> getExistTxs(int chainId, List<byte[]> hashList);

}
