package io.nuls.crosschain.base.service;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2020/11/23 11:15
 * @Description: 重新对齐本链验证人列表的数据
 *
 */
public interface ResetLocalVerifierService {

    /**
     * 创建重置本地验证人交易
     *
     * @return processor result
     * */
    Result createResetLocalVerifierTx(int chainId,String address,String password) ;

    /**
     * 交易验证
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    Map<String,Object> validate(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 交易提交
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean commitTx(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 交易回滚
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean rollbackTx(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 判断初始化验证人交易是不是用于重置平行链上的主链验证人列表
     * @param txHash
     * @return
     */
    boolean isResetOtherVerifierTx(String txHash);

    /**
     * 重置平行链上的主链验证人交易已经完成拜占庭签名，从缓存中移除
     * @param txHash
     */
    void finishResetOtherVerifierTx(String txHash);

}
