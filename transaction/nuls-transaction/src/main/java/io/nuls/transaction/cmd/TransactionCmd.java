package io.nuls.transaction.cmd;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public class TransactionCmd extends BaseCmd {

    /**
     * Register module transactions, validators, processors(commit, rollback), etc.
     *
     * @param params
     * @return
     */
    public CmdResponse register(List params) {

        return success("success", null);
    }

    /**
     * Receive a new transaction serialization data
     *
     * @param params
     * @return
     */
    public CmdResponse newTx(List params) {

        return success("success", null);
    }

    /**
     * Extract a packaged transaction list based on the packaging end time and transactions total size
     *
     * @param params
     * @return
     */
    public CmdResponse packableTxs(List params) {

        return success("success", null);
    }

    /**
     * Execute the transaction processor commit
     *
     * @param params
     * @return
     */
    public CmdResponse commit(List params) {

        return success("success", null);
    }

    /**
     * Execute transaction processor rollback
     *
     * @param params
     * @return
     */
    public CmdResponse rollback(List params) {

        return success("success", null);
    }

    /**
     * Save the transaction in the new block that was verified to the database
     * 保存新区块的交易
     *
     * @param params
     * @return
     */
    public CmdResponse save(List params) {

        return success("success", null);
    }

    /**
     * Get the transaction that have been packaged into the block from the database
     *
     * @param params
     * @return
     */
    public CmdResponse getTx(List params) {

        return success("success", null);
    }

    /**
     * Delete transactions that have been packaged into blocks from the database, block rollback, etc.
     *
     * @param params
     * @return
     */
    public CmdResponse delete(List params) {

        return success("success", null);
    }

    /**
     * Local validation of transactions (including cross-chain transactions),
     * including calling the validator, verifying the coinData.
     * Cross-chain verification of cross-chain transactions is not included.
     *
     * @param params
     * @return
     */
    public CmdResponse verify(List params) {

        return success("success", null);
    }

    /**
     * Returns the relationship list of the transaction and its corresponding commit processor and rollback processor
     *
     * @param params
     * @return
     */
    public CmdResponse getTxProcessors(List params) {

        return success("success", null);
    }

    /**
     * Query the transaction list based on conditions such as account, chain, asset, and paging information.
     *
     * @param params
     * @return
     */
    public CmdResponse getTxs(List params) {

        return success("success", null);
    }



}
