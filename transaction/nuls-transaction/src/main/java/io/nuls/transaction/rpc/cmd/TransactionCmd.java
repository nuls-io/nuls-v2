package io.nuls.transaction.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.dto.ModuleTxRegisterDto;
import io.nuls.transaction.model.dto.TxRegisterDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    @CmdAnnotation(cmd = "tx_register", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "module transaction registration")
    public Response register(Map params) {
        // check parameters
        if (params == null) {
            throw new NulsRuntimeException(TxErrorCode.NULL_PARAMETER);
        }
        try {
            //ModuleTxRegisterDto moduleTxRegisterDto = JSONUtils.json2pojo(JSONUtils.obj2json(params), ModuleTxRegisterDto.class, TxRegisterDto.class);
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ModuleTxRegisterDto moduleTxRegisterDto = JSONUtils.json2pojo(JSONUtils.obj2json(params), ModuleTxRegisterDto.class);
            List<TxRegisterDto> txRegisterList = moduleTxRegisterDto.getList();
            //List<TxRegisterDto> txRegisterList = JSONUtils.json2list(JSONUtils.obj2json(params.get("list")), TxRegisterDto.class);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        return success("success");
    }

    /**
     * Receive a new transaction serialization data
     *
     * @param params
     * @return
     */
    public Response newTx(List params) {

        return success("success");
    }

    /**
     * Extract a packaged transaction list based on the packaging end time and transactions total size
     *
     * @param params
     * @return
     */
    public Response packableTxs(List params) {

        return success("success");
    }

    /**
     * Execute the transaction processor commit
     *
     * @param params
     * @return
     */
    public Response commit(List params) {

        return success("success");
    }

    /**
     * Execute transaction processor rollback
     *
     * @param params
     * @return
     */
    public Response rollback(List params) {

        return success("success");
    }

    /**
     * Save the transaction in the new block that was verified to the database
     * 保存新区块的交易
     *
     * @param params
     * @return
     */
    public Response save(List params) {

        return success("success");
    }

    /**
     * Get the transaction that have been packaged into the block from the database
     *
     * @param params
     * @return
     */
    public Response getTx(List params) {

        return success("success");
    }

    /**
     * Delete transactions that have been packaged into blocks from the database, block rollback, etc.
     *
     * @param params
     * @return
     */
    public Response delete(List params) {

        return success("success");
    }

    /**
     * Local validation of transactions (including cross-chain transactions),
     * including calling the validator, verifying the coinData.
     * Cross-chain verification of cross-chain transactions is not included.
     *
     * @param params
     * @return
     */
    public Response verify(List params) {

        return success("success");
    }

    /**
     * Returns the relationship list of the transaction and its corresponding commit processor and rollback processor
     *
     * @param params
     * @return
     */
    public Response getTxProcessors(List params) {

        return success("success");
    }

    /**
     * Query the transaction list based on conditions such as account, chain, asset, and paging information.
     *
     * @param params
     * @return
     */
    public Response getTxs(List params) {

        return success("success");
    }


}
