package io.nuls.crosschain.base.service;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;

import java.util.List;
import java.util.Map;

public interface ResetChainService {
    /**
     * Reset chain information transaction initialization batch verification
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param txMap         Consensus Module All Transaction Classification
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);

    /**
     * Reset chain information transaction initialization submission
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Reset chain information transaction initialization rollback
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader);
}
