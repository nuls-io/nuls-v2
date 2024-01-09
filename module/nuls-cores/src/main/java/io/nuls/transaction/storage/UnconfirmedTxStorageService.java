package io.nuls.transaction.storage;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.model.po.TransactionUnconfirmedPO;

import java.util.List;

/**
 * Transactions that have been validated but not packaged(Unconfirmed transaction)
 * Save verified transaction (unpackaged)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface UnconfirmedTxStorageService {

    /**
     * Save Verified Transactions
     *
     * @param chainId
     * @param tx
     * @return Whether the save was successful
     */
    boolean putTx(int chainId, Transaction tx);


    /**
     * Batch save unconfirmed transactions
     * @param chainId
     * @param txNetPOList
     * @return
     */
    boolean putTxList(int chainId, List<TransactionNetPO> txNetPOList);

    /**
     * According to the transactionhashQuery verified transaction data
     *
     * @param chainId
     * @param hash
     * @return transaction data
     */
    TransactionUnconfirmedPO getTx(int chainId, NulsHash hash);

    /**
     * Determine if the transaction exists in the unconfirmed transaction database
     * @param chainId
     * @param hash
     * @return
     */
    boolean isExists(int chainId, NulsHash hash);

    /**
     * According to the transactionhashQuery verified transaction data
     *
     * @param chainId
     * @param hash
     * @return transaction data
     */
    TransactionUnconfirmedPO getTx(int chainId, String hash);

    /**
     * According to the transactionhashDelete verified transaction data
     *
     * @param chainId
     * @param hash
     * @return Whether the deletion was successful
     */
    boolean removeTx(int chainId, NulsHash hash);

    boolean removeTx(int chainId, byte[] hash);

    /**
     * According to the transactionhashBatch query of verified transaction data
     *
     * @param chainId
     * @param hashList NulsHash serialize entity
     * @return Transaction Data List
     */
    List<Transaction> getTxList(int chainId, List<byte[]> hashList);

    /**
     * According to the transactionhashBatch deletion of verified transaction data
     *
     * @param chainId
     * @param hashList NulsHash serialize entity
     * @return Whether the deletion was successful
     */
    boolean removeTxList(int chainId, List<byte[]> hashList);

    /**
     * Query all unconfirmed transactionskey
     *
     * @param chainId
     * @return
     */
    List<byte[]> getAllTxkeyList(int chainId);

    /**
     * Query unconfirmed transaction data, including save time
     *
     * @param chainId
     * @return
     */
    List<TransactionUnconfirmedPO> getTransactionUnconfirmedPOList(int chainId, List<byte[]> hashList);

    /**
     * according tohash Obtain existingkey
     * @param chainId
     * @param hashList
     * @return
     */
    List<byte[]> getExistKeys(int chainId, List<byte[]> hashList);

    /**
     * according tohash Obtain existingkey
     * @param chainId
     * @param hashList
     * @return
     */
    List<String> getExistKeysStr(int chainId, List<byte[]> hashList);
}
