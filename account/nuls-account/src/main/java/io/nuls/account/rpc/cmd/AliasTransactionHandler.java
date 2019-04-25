package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AliasService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.TxUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.TxType;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.protocol.TransactionProcessor;
import io.nuls.tools.protocol.TxMethodType;

import java.util.HashMap;
import java.util.Map;

@Service
public class AliasTransactionHandler extends BaseCmd {

    @Autowired
    private AliasService aliasService;

    /**
     * validate the transaction of alias
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_aliasTxValidate", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "validate the transaction of alias")
    @TransactionProcessor(txType = TxType.ACCOUNT_ALIAS, methodType = TxMethodType.VALID)
    public Response aliasTxValidate(Map params) {
        boolean result;
        int chainId;
        String tx;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txObj = params == null ? null : params.get(RpcParameterNameConstant.TX);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            tx = (String) txObj;
            Transaction transaction = TxUtil.getInstanceRpcStr(tx, Transaction.class);
            result = aliasService.aliasTxValidate(chainId, transaction);
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            result = false;
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            result = false;
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        return success(resultMap);
    }

    /**
     * commit the alias transaction
     */
    @CmdAnnotation(cmd = "ac_aliasTxCommit", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "commit the alias transaction")
    @TransactionProcessor(txType = TxType.ACCOUNT_ALIAS, methodType = TxMethodType.COMMIT)
    public Response aliasTxCommit(Map params) {
        boolean result;
        int chainId;
        String tx;
        //TODO is it need to verify secondaryData?
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txObj = params == null ? null : params.get(RpcParameterNameConstant.TX);
        Object blockHeaderDigest = params == null ? null : params.get(RpcParameterNameConstant.BLOCK_HEADER_DIGEST);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            tx = (String) txObj;
            Transaction transaction = TxUtil.getInstanceRpcStr(tx, Transaction.class);
            Alias alias = new Alias();
            alias.parse(new NulsByteBuffer(transaction.getTxData()));
            result = aliasService.aliasTxCommit(chainId, alias);
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        return success(resultMap);
    }

    /**
     * rollback the alias info which saved in the storage
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_aliasTxRollback", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "rollback the alias info which saved in the storage")
    @TransactionProcessor(txType = TxType.ACCOUNT_ALIAS, methodType = TxMethodType.ROLLBACK)
    public Response aliasTxRollback(Map params) {
        boolean result;
        int chainId;
        String tx;
        //TODO is it need to verify secondaryData?
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txObj = params == null ? null : params.get(RpcParameterNameConstant.TX);
        Object blockHeaderDigest = params == null ? null : params.get(RpcParameterNameConstant.BLOCK_HEADER_DIGEST);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            tx = (String) txObj;
            Transaction transaction = TxUtil.getInstanceRpcStr(tx, Transaction.class);
            Alias alias = new Alias();
            alias.parse(new NulsByteBuffer(transaction.getTxData()));
            result = aliasService.rollbackAlias(chainId, alias);
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        return success(resultMap);
    }

}
