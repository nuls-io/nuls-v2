package io.nuls.transaction.service;

import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionConfirmedPO;

import java.util.List;

/**
 * 已确认交易的服务接口
 * @author: Charlie
 * @date: 2018/11/30
 */
public interface ConfirmedTxService {

    /**
     * Get a confirmed transaction
     *
     * 获取一笔已打包进区块并且确认的交易
     * @param chain
     * @param hash
     * @return TransactionConfirmedPO
     */
    TransactionConfirmedPO getConfirmedTransaction(Chain chain, NulsHash hash);

    /**
     * 保存创世块的交易
     * @param chain
     * @param txStrList
     * @param blockHeader
     * @return
     * @throws NulsException
     */
    boolean saveGengsisTxList(Chain chain, List<String> txStrList, String blockHeader) throws NulsException;

    /**
     * 保存区块中已确认交易
     * @param chain
     * @param txStrList
     * @param blockHeader
     * @return
     */
    boolean saveTxList(Chain chain, List<String> txStrList, String blockHeader) throws NulsException;



    /**
     * 批量回滚已确认交易
     * @param chain
     * @param txHashList
     * @param blockHeader
     * @return
     */
    boolean rollbackTxList(Chain chain, List<NulsHash> txHashList, String blockHeader) throws NulsException;


    /**
     * 获取区块的完整交易 只从已确认的交易中查询
     * 如果没有查询到,或者查询到的不是区块完整的交易数据 则返回空list
     * @param chain
     * @param hashList
     * @return List<String> tx list
     */
    List<String> getTxList(Chain chain, List<String> hashList);

    /**
     * 获取区块的完整交易 先查未确认交易, 再查已确认交易
     * allHits:true 如果没有查询到,或者查询到的不是区块完整的交易数据 则返回空list
     * allHits:false 查到多少返回多少，如果没有查询到则返回空list
     * @param chain
     * @param hashList
     * @return List<String> tx list
     */
    List<String> getTxListExtend(Chain chain, List<String> hashList, boolean allHits);
}
