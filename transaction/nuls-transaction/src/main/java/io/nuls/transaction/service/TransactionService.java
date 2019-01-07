package io.nuls.transaction.service;

import io.nuls.base.data.BlockHeaderDigest;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.dto.AccountSignDTO;
import io.nuls.transaction.model.dto.CoinDTO;

import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
public interface TransactionService {

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
    boolean newTx(Chain chain, Transaction transaction) throws NulsException;

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
    Transaction getTransaction(Chain chain, NulsDigestData hash);

    /**
     * 创建不包含多签地址跨链交易，支持多普通地址
     * Create a cross-chain transaction
     *
     * @param chain    当前链的id Current chain
     * @param listFrom 交易的转出者数据 payer coins
     * @param listTo   交易的接收者数据 payee  coins
     * @param remark   交易备注 remark
     * @return String
     * @throws NulsException NulsException
     */
    String createCrossTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) throws NulsException;

    /**
     * 创建跨链多签签名地址交易
     * 所有froms中有且仅有一个地址，并且只能是多签地址，但可以包含多个资产(from)
     *
     * @param chain          当前链 chain
     * @param listFrom       交易的转出者数据 payer coins
     * @param listTo         交易的接收者数据 payee  coins
     * @param remark         交易备注 remark
     * @param accountSignDTO
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> createCrossMultiTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark, AccountSignDTO accountSignDTO) throws NulsException;

    /**
     * 创建跨链多签签名地址交易
     * 所有froms中有且仅有一个地址，并且只能是多签地址，但可以包含多个资产(from)
     *
     * @param chain    当前链的id Current chain
     * @param listFrom 交易的转出者数据 payer coins
     * @param listTo   交易的接收者数据 payee  coins
     * @param remark   交易备注 remark
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> createCrossMultiTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) throws NulsException;

    /**
     * 对多签交易进行签名的数据组装
     *
     * @param chain    链信息
     * @param tx       待签名的交易数据
     * @param address  执行签名的账户地址
     * @param password 账户密码
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> signMultiTransaction(Chain chain, String tx, String address, String password) throws NulsException;

    /**
     * 处理多签交易的签名 追加签名
     *
     * @param chain 链信息
     * @param tx    交易数据 tx
     * @param ecKey 签名者的eckey
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> txMultiSignProcess(Chain chain, Transaction tx, ECKey ecKey) throws NulsException;


    /**
     * 处理多签交易的签名，第一次签名可以先组装多签账户的基础数据，到签名数据中
     *
     * @param chain                链信息
     * @param tx                   交易数据 tx
     * @param ecKey                签名者的 eckey数据
     * @param multiSignTxSignature 新的签名数据  sign data
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> txMultiSignProcess(Chain chain, Transaction tx, ECKey ecKey, MultiSignTxSignature multiSignTxSignature) throws NulsException;

    /**
     * 单个跨链交易本地验证器
     *
     * @param chain       链id
     * @param transaction 跨链交易
     * @return boolean
     * @throws NulsException
     */
    boolean crossTransactionValidator(Chain chain, Transaction transaction) throws NulsException;

    boolean crossTransactionCommit(Chain chain, Transaction transaction, BlockHeaderDigest blockHeader) throws NulsException;

    boolean crossTransactionRollback(Chain chain, Transaction transaction, BlockHeaderDigest blockHeader) throws NulsException;


    /**
     * 打包
     *
     * @param chain
     * @param endtimestamp
     * @param maxTxDataSize
     * @return
     * @throws NulsException
     */
    List<String> getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize) throws NulsException;

    /**
     * 收到新区快时，验证完整交易列表
     *
     * @param chain
     * @param list
     * @return
     * @throws NulsException
     */
    boolean batchVerify(Chain chain, List<String> list) throws NulsException;


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
