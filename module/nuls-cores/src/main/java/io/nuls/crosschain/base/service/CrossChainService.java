package io.nuls.crosschain.base.service;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;

import java.util.List;
import java.util.Map;

/**
 * Cross chain module service interface class
 * @author tag
 * @date 2019/4/8
 */
public interface CrossChainService {

    /**
     * Create cross chain transactions
     * @param params Parameters required to create cross chain transactions
     *
     * @return processor result
     * */
    Result createCrossTx(Map<String,Object> params);

    /**
     * receiveAPI_MODULECross chain transactions for assembly
     * @param params api_moduleNew Cross Chain Transactions
     *
     * @return processor result
     * */
    Result newApiModuleCrossTx(Map<String,Object> params);

    /**
     * Cross chain transaction verification
     * @param params Parameters required for cross chain transaction verification
     *
     * @return processor result
     * */
    Result validCrossTx(Map<String,Object> params);

    /**
     * Cross chain transaction submission
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean commitCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Cross chain transaction rollback
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean rollbackCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Cross chain transaction batch verification
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param txMap         Consensus Module All Transaction Classification
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    Map<String, Object> crossTxBatchValid(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);

    /**
     * Query the processing results of cross chain transactions on the main network
     * @param params transactionHash
     *
     * @return processor result
     * */
    Result getCrossTxState(Map<String,Object> params);

    /**
     * Query the list of registered cross chain chain information
     *
     * @param params    nothing
     * @return processor result
     * */
    Result getRegisteredChainInfoList(Map<String,Object> params);

    /**
     * Query the list of registered cross chain chain information
     *
     * @param params    nothing
     * @return processor result
     * */
    Result getByzantineCount(Map<String,Object> params);

    /**
     * Set cross chain transaction types
     * Setting up cross-chain transaction types
     *
     * @return
     */
    default int getCrossChainTxType() {
        return TxType.CROSS_CHAIN;
    }
}
