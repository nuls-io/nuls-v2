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
    Result createResetLocalVerifierTx(int chainId,String address,String password) throws NulsException, IOException;

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

}
