package io.nuls.crosschain.base.service;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;

import java.util.List;
import java.util.Map;

/**
 * 验证人初始化交易处理类
 * @author tag
 * @date 2019/8/7
 */
public interface VerifierInitService {
    /**
     * 验证人初始化交易验证
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param txMap         Consensus Module All Transaction Classification
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);

    /**
     * 验证人初始化交易提交
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 验证人初始化交易回滚
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader);
}
