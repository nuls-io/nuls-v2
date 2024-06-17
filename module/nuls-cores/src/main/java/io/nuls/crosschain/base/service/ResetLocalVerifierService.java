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
 * @Description: Realign the data of the witness list for this chain
 *
 */
public interface ResetLocalVerifierService {

    /**
     * Create a reset local validator transaction
     *
     * @return processor result
     * */
    Result createResetLocalVerifierTx(int chainId,String address,String password) ;

    /**
     * Transaction verification
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    Map<String,Object> validate(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Transaction submission
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean commitTx(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Transaction Rollback
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean rollbackTx(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Determine if the initialization validator transaction is used to reset the main chain validator list on the parallel chain
     * @param txHash
     * @return
     */
    boolean isResetOtherVerifierTx(String txHash);

    /**
     * Reset the main chain verifier transaction on the parallel chain. Byzantine signature has been completed and removed from the cache
     * @param txHash
     */
    void finishResetOtherVerifierTx(String txHash);

}
