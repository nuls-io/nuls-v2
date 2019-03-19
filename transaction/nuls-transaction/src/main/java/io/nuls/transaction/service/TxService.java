package io.nuls.transaction.service;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxPackage;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.bo.VerifyTxResult;
import io.nuls.transaction.model.po.TransactionConfirmedPO;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
public interface TxService {

    /**
     * 注册交易
     * Register transaction
     *
     * @param chain
     * @param txRegister
     * @return boolean
     */
    boolean register(Chain chain, TxRegister txRegister);

    /**
     * 收到一个新的交易
     * Received a new transaction
     *
     * @param transaction
     * @return boolean
     * @throws NulsException NulsException
     */
    void newTx(Chain chain, Transaction transaction) throws NulsException;


    /**
     * 验证交易
     * @param chain
     * @param tx
     * @return
     */
    boolean verify(Chain chain, Transaction tx);

    /**
     * Get a transaction, first check the database from the confirmation transaction,
     * if not found, then query from the confirmed transaction
     *
     * 获取一笔交易, 先从未确认交易数据库中查询, 如果没有找到再从已确认的交易中查询
     *
     * @param chain chain
     * @param hash  tx hash
     * @return Transaction 如果没有找到则返回null
     */
    TransactionConfirmedPO getTransaction(Chain chain, NulsDigestData hash);



    /**
     * 单个跨链交易本地验证器
     *
     * @param chain       链id
     * @param transaction 跨链交易
     * @return boolean
     * @throws NulsException
     */
    boolean crossTransactionValidator(Chain chain, Transaction transaction) throws NulsException;

    /**
     * 如果有txData相同的交易,则过滤掉后面一个
     * @param chain
     * @param txHexList
     * @return List<String> txHex
     * @throws NulsException
     */
    List<String> transactionModuleValidator(Chain chain, List<String> txHexList) throws NulsException;

    boolean crossTransactionCommit(Chain chain, List<String> txHex, String blockHeaderHex) throws NulsException;

    boolean crossTransactionRollback(Chain chain, List<String> txHex, String blockHeaderHex) throws NulsException;


    /**
     * 打包
     *
     * @param chain
     * @param endtimestamp
     * @param maxTxDataSize
     * @return
     * @throws NulsException
     */
    TxPackage getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize, long blockTime, long blockHeight,
                             String packingAddress, String preStateRoot) throws NulsException;

    /**
     * 收到新区快时，验证完整交易列表
     *
     * @param chain
     * @param list
     * @return
     * @throws NulsException
     */
    VerifyTxResult batchVerify(Chain chain, List<String> list, long blockHeight) throws NulsException;


    /**
     * 从已验证未打包交易中删除无效的交易集合, 并回滚账本
     *
     * @param chain
     * @param txList
     * @return
     */
    void clearInvalidTx(Chain chain, List<Transaction> txList);

    /**
     * 从已验证未打包交易中删除单个无效的交易, 并回滚账本
     *
     * @param chain
     * @param tx
     * @return
     */
    void clearInvalidTx(Chain chain, Transaction tx);
}
