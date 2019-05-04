package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AliasService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.validator.TxValidator;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.protocol.TransactionProcessor;
import io.nuls.rpc.protocol.TxMethodType;
import io.nuls.tools.constant.TxType;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountTransactionHandler extends BaseCmd {

    @Autowired
    private AliasService aliasService;

    @Autowired
    private TxValidator txValidator;

    /**
     * validate the transaction of alias
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "aliasTxValidate", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "validate the transaction of alias")
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
    @CmdAnnotation(cmd = "aliasTxCommit", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "commit the alias transaction")
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
    @CmdAnnotation(cmd = "aliasTxRollback", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "rollback the alias info which saved in the storage")
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

    /**
     * 转账交易验证
     */
    @CmdAnnotation(cmd = "transferTxValidate", version = 1.0, description = "create transfer transaction validate 1.0")
    @Parameter(parameterName = RpcParameterNameConstant.CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = RpcParameterNameConstant.TX, parameterType = "String")
    @TransactionProcessor(txType = TxType.TRANSFER, methodType = TxMethodType.VALID)
    public Response transferTxValidate(Map<String, Object> params) {
        Map<String, Boolean> resultMap = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        boolean result;
        try {
            if (params.get(RpcParameterNameConstant.CHAIN_ID) == null || params.get(RpcParameterNameConstant.TX) == null) {
                LoggerUtil.logger.warn("ac_transferTxValidate params is null");
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            int chainId = (Integer) params.get(RpcParameterNameConstant.CHAIN_ID);
            String tx = (String) params.get(RpcParameterNameConstant.TX);
            if (chainId <= 0) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            Transaction transaction = TxUtil.getInstanceRpcStr(tx, Transaction.class);
            result = txValidator.validateTx(chainId, transaction);
        } catch (NulsException e) {
            LoggerUtil.logger.warn("", e);
            result = false;
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            result = false;
        }
        resultMap.put("value", result);
        return success(resultMap);
    }

//    /**
//     * 转账交易提交
//     */
//    @CmdAnnotation(cmd = "ac_transferTxCommit", version = 1.0, description = "create transfer transaction commit 1.0")
//    public Response transferTxCommit(Map<String, Object> params) {
//        Map<String, Boolean> resultMap = new HashMap<>();
//        resultMap.put("value", true);
//        return success(resultMap);
//    }
//
//    /**
//     * 转账交易回滚
//     */
//    @CmdAnnotation(cmd = "ac_transferTxRollback", version = 1.0, description = "create transfer transaction rollback 1.0")
//    public Response transferTxRollback(Map<String, Object> params) {
//        Map<String, Boolean> resultMap = new HashMap<>();
//        resultMap.put("value", true);
//        return success(resultMap);
//    }

}
