package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.TxUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.Parameters;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.protocol.ResisterTx;
import io.nuls.tools.protocol.TxMethodType;
import io.nuls.tools.protocol.TxProperty;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: EdwardChan
 * @description: the Entry of Alias RPC
 * @date: Nov.20th 2018
 */
@Component
public class AliasCmd extends BaseCmd {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private AccountKeyStoreService keyStoreService;


    /**
     * set the alias of account
     *
     * @param params
     * @return txhash
     */
    @CmdAnnotation(cmd = "ac_setAlias", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "set the alias of account")
    @Parameters({
            @Parameter(parameterName = "chainId",parameterType = "int",canNull = false),
            @Parameter(parameterName = "address",parameterType = "string",canNull = false),
            @Parameter(parameterName = "password",parameterType = "string",canNull = false),
            @Parameter(parameterName = "alias",parameterType = "string",canNull = false)
    })
    public Response setAlias(Map params) {
        int chainId;
        String address, password, alias, txHash = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            address = (String) addressObj;
            password = (String) passwordObj;
            alias = (String) aliasObj;
            Transaction transaction = aliasService.setAlias(chainId, address, password, alias);
            if (transaction != null && transaction.getHash() != null) {
                txHash = transaction.getHash().getDigestHex();
            }
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>();
        result.put("txHash", txHash);
        return success(result);
    }

    /**
     * Gets to set the alias transaction fee
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_getAliasFee", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "Gets to set the alias transaction fee")
    public Response getAliasFee(Map params) {
        int chainId = 0;
        String address, alias;
        BigInteger fee;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            address = (String) addressObj;
            alias = (String) aliasObj;
            fee = aliasService.getAliasFee(chainId, address, alias);
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>();
        result.put("fee", fee == null ? "" : fee.toString());
        //TODO is need to format?
        return success(result);
    }

    /**
     * get the alias by address
     *
     * @param params
     * @return CmdResponse
     * @auther EdwardChan
     * <p>
     * Nov.20th 2018
     */
    @CmdAnnotation(cmd = "ac_getAliasByAddress", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "get the alias by address")
    public Response getAliasByAddress(Map params) {
        String alias;
        int chainId = 0;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        String address;
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            address = (String) addressObj;
            alias = aliasService.getAliasByAddress(chainId, address);
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>();
        result.put("alias", alias);
        return success(result);
    }

    /**
     * check whether the account is usable
     *
     * @param params
     * @return CmdResponse
     */
    @CmdAnnotation(cmd = "ac_isAliasUsable", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "check whether the account is usable")
    public Response isAliasUsable(Map params) {
        boolean isAliasUsable = false;
        int chainId;
        String alias;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        try {
            // check parameters
            if (params == null || chainIdObj == null || aliasObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            alias = (String) aliasObj;
            isAliasUsable = aliasService.isAliasUsable(chainId, alias);
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("value", isAliasUsable);
        return success(result);
    }

    /**
     * validate the transaction of alias
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_aliasTxValidate", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "validate the transaction of alias")
    @ResisterTx(txType = TxProperty.ACCOUNT_ALIAS, methodType = TxMethodType.VALID, methodName = "ac_aliasTxValidate")
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
            result=false;
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            result=false;
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        return success(resultMap);
    }

    /**
     * commit the alias transaction
     */
    @CmdAnnotation(cmd = "ac_aliasTxCommit", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "commit the alias transaction")
    public Response aliasTxCommit(Map params) throws NulsException {
        boolean result = false;
        int chainId = 0;
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
    public Response rollbackAlias(Map params) throws NulsException {
        boolean result = false;
        int chainId = 0;
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
