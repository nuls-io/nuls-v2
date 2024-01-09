package io.nuls.crosschain.base.service;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import java.util.List;
import java.util.Map;

/**
 * Verifier change transaction processing class
 * @author tag
 * @date 2019/6/19
 */

public interface VerifierChangeTxService {

    /**
     * Cross chain transaction batch verification
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param txMap         Consensus Module All Transaction Classification
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);

    /**
     * Verifier change transaction submission
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Verifier change transaction rollback
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader);
}
