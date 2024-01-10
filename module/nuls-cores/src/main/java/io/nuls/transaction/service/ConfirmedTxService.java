package io.nuls.transaction.service;

import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionConfirmedPO;

import java.util.List;

/**
 * Confirmed transaction service interface
 * @author: Charlie
 * @date: 2018/11/30
 */
public interface ConfirmedTxService {

    /**
     * Get a confirmed transaction
     *
     * Obtain a transaction that has been packaged into a block and confirmed
     * @param chain
     * @param hash
     * @return TransactionConfirmedPO
     */
    TransactionConfirmedPO getConfirmedTransaction(Chain chain, NulsHash hash);

    /**
     * Save transactions for Genesis blocks
     * @param chain
     * @param txStrList
     * @param blockHeader
     * @return
     * @throws NulsException
     */
    boolean saveGengsisTxList(Chain chain, List<String> txStrList, String blockHeader) throws NulsException;

    /**
     * Save confirmed transactions in the block
     * @param chain
     * @param txStrList
     * @param blockHeader
     * @return
     */
    boolean saveTxList(Chain chain, List<String> txStrList, List<String> contractList, String blockHeader) throws NulsException;



    /**
     * Batch rollback of confirmed transactions
     * @param chain
     * @param txHashList
     * @param blockHeader
     * @return
     */
    boolean rollbackTxList(Chain chain, List<NulsHash> txHashList, String blockHeader) throws NulsException;


    /**
     * Obtain complete transactions for blocks Only query from confirmed transactions
     * If no query is found,Or the transaction data queried is not complete for the block Then return emptylist
     * @param chain
     * @param hashList
     * @return List<String> tx list
     */
    List<String> getTxList(Chain chain, List<String> hashList);

    /**
     * Obtain complete transactions for blocks First check for unconfirmed transactions, Re check confirmed transactions
     * allHits:true If no query is found,Or the transaction data queried is not complete for the block Then return emptylist
     * allHits:false Return as much as found, if not found, return emptylist
     * @param chain
     * @param hashList
     * @return List<String> tx list
     */
    List<String> getTxListExtend(Chain chain, List<String> hashList, boolean allHits);


    /**
     * Query incoming and outgoing transactionshashin,Transactions that do not exist in the unconfirmed databasehash
     * @param chain
     * @param hashList
     * @return
     */
    List<String> getNonexistentUnconfirmedHashList(Chain chain, List<String> hashList);
}
