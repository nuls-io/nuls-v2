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
    void newBroadcastTx(Chain chain, Transaction transaction) throws NulsException;


    /**
     * 由节点产生的新交易,该交易已通过验证器验证和账本验证,可放入待打包队列以及未确认存储
     * @param chain
     * @param transaction
     * @throws NulsException
     */
    boolean newTx(Chain chain, Transaction transaction);


    /**
     * 验证交易
     * @param chain
     * @param tx
     * @return
     */
    boolean verify(Chain chain, Transaction tx);

    /**
     * 验证交易,不执行基础的校验
     * @param chain
     * @param tx
     * @param incloudBasic
     * @return
     */
    boolean verify(Chain chain, Transaction tx, boolean incloudBasic);

    /**
     * 交易基础验证
     * 基础字段
     * 交易size
     * 交易类型
     * 交易签名
     * from的地址必须全部是发起链(本链or相同链）地址
     * from里面的资产是否存在
     * to里面的地址必须是相同链的地址
     * 交易手续费
     *
     * @param chain
     * @param tx
     * @param txRegister
     * @throws NulsException
     */
    void baseValidateTx(Chain chain, Transaction tx, TxRegister txRegister) throws NulsException;

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
     * @param txList
     * @return List<String> tx
     * @throws NulsException
     */
    List<String> transactionModuleValidator(Chain chain, List<String> txList) throws NulsException;

    boolean crossTransactionCommit(Chain chain, List<String> tx, String blockHeader) throws NulsException;

    boolean crossTransactionRollback(Chain chain, List<String> tx, String blockHeader) throws NulsException;


    /**
     *  共识打包获取打包所需交易
     * @param chain
     * @param endtimestamp 获取交易截止时间
     * @param maxTxDataSize
     * @param blockTime 区块时间
     * @param blockHeight 打包高度
     * @param packingAddress
     * @param preStateRoot
     * @return
     */
    TxPackage getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize, long blockTime, long blockHeight,
                             String packingAddress, String preStateRoot);

    /**
     * 收到新区快时，验证完整交易列表
     * @param chain
     * @param list
     * @param blockHeight
     * @param blockTime
     * @param packingAddress
     * @param stateRoot
     * @param preStateRoot
     * @return
     * @throws NulsException
     */
    VerifyTxResult batchVerify(Chain chain, List<String> list, long blockHeight, long blockTime, String packingAddress, String stateRoot, String preStateRoot) throws NulsException;


    /**
     * 从已验证未打包交易中删除无效的交易集合, 并回滚账本
     *
     * @param chain
     * @param txList
     * @return
     */
    void clearInvalidTx(Chain chain, List<Transaction> txList);

    /**
     * 从已验证未打包交易中删除单个无效的交易
     *
     * @param chain
     * @param tx
     * @return
     */
    void clearInvalidTx(Chain chain, Transaction tx);

    /**
     * 从已验证未打包交易中删除单个无效的交易
     * @param chain
     * @param tx
     * @param cleanLedgerUfmTx 调用账本的未确认回滚
     */
    void clearInvalidTx(Chain chain, Transaction tx, boolean cleanLedgerUfmTx);

}
