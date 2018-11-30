package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

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
    public Object setAlias(Map params) {
        return null;
    }

    /**
     * Gets to set the alias transaction fee
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_getAliasFee", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "Gets to set the alias transaction fee")
    public Object getAliasFee(Map params) {
        return null;
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
    public Object getAliasByAddress(Map params) {
        Log.debug("ac_getAliasByAddress start,params size:{}", params == null ? 0 : params.size());
        String alias;
        int chainId = 0;
        String address;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            address = (String) addressObj;
            alias = aliasService.getAliasByAddress(chainId, address);
        } catch (NulsRuntimeException e) {
            Log.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>();
        result.put("alias", alias);
        Log.debug("ac_getAliasByAddress end");
        return success(result);
    }

    /**
     * check whether the account is usable
     *
     * @param params
     * @return CmdResponse
     */
    @CmdAnnotation(cmd = "ac_isAliasUsable", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "check whether the account is usable")
    public Object isAliasUsable(Map params) {
        Log.debug("ac_isAliasUsable start,params size:{}", params == null ? 0 : params.size());
        boolean isAliasUsable = false;
        int chainId = 0;
        String alias;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
            if (params == null || chainIdObj == null || aliasObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            alias = (String) aliasObj;
            isAliasUsable = aliasService.isAliasUsable(chainId, alias);
        } catch (NulsRuntimeException e) {
            Log.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("value", isAliasUsable);
        Log.debug("ac_isAliasUsable end");
        return success(result);
    }

    /**
     * set multi sign alias
     *
     * @param params
     * @return
     **/
    @CmdAnnotation(cmd = "ac_setMultiSigAlias", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "set multi sign alias")
    public Object setMultiSigAlias(Map params) {
        return null;
    }

    /**
     * validate the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_accountTxValidate", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "validate the transaction")
    public Object accountTxValidate(Map params) {
        return null;
    }

    /**
     * validate the transaction of alias
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_aliasTxValidate", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "validate the transaction of alias")
    public Object aliasTxValidate(Map params) {
        Log.debug("ac_aliasTxValidate start,params size:{}", params == null ? 0 : params.size());
        boolean result = false;
        int chainId = 0;
        String txHex;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object txHexObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX);
            if (params == null || chainIdObj == null || txHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txHex = (String) txHexObj;
            Transaction transaction = new Transaction(AccountConstant.TX_TYPE_ACCOUNT_ALIAS);
            transaction.parse(new NulsByteBuffer(HexUtil.hexStringToBytes(txHex)));
            result = aliasService.aliasTxValidate(chainId,transaction);
        } catch (NulsRuntimeException e) {
            Log.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        Log.debug("ac_aliasTxCommit end");
        return success(result);
    }

    /**
     * commit the alias transaction
     */
    @CmdAnnotation(cmd = "ac_aliasTxCommit", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "commit the alias transaction")
    public Object aliasTxCommit(Map params) throws NulsException {
        Log.debug("ac_aliasTxCommit start,params size:{}", params == null ? 0 : params.size());
        boolean result = false;
        int chainId = 0;
        String txHex;
        //TODO is it need to verify secondaryDataHex?
        String secondaryDataHex;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object txHexObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX);
            Object secondaryDataHexObj = params == null ? null : params.get(RpcParameterNameConstant.SECONDARY_DATA_Hex);
            if (params == null || chainIdObj == null || txHexObj == null || secondaryDataHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txHex = (String) txHexObj;
            secondaryDataHex = (String) secondaryDataHexObj;
            Transaction transaction = new Transaction(AccountConstant.TX_TYPE_ACCOUNT_ALIAS);
            transaction.parse(new NulsByteBuffer(HexUtil.hexStringToBytes(txHex)));
            Alias alias = new Alias();
            alias.parse(new NulsByteBuffer(transaction.getTxData()));
            result = aliasService.aliasTxCommit(chainId,alias);
        } catch (NulsRuntimeException e) {
            Log.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        Log.debug("ac_aliasTxCommit end");
        return success(result);
    }

    /**
     * rollback the alias info which saved in the db
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_aliasTxRollback", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "rollback the alias info which saved in the db")
    public Object rollbackAlias(Map params) throws NulsException {
        Log.debug("ac_aliasTxRollback start,params size:{}", params == null ? 0 : params.size());
        boolean result = false;
        int chainId = 0;
        String txHex;
        //TODO is it need to verify secondaryDataHex?
        String secondaryDataHex;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object txHexObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX);
            Object secondaryDataHexObj = params == null ? null : params.get(RpcParameterNameConstant.SECONDARY_DATA_Hex);
            if (params == null || chainIdObj == null || txHexObj == null || secondaryDataHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txHex = (String) txHexObj;
            secondaryDataHex = (String) secondaryDataHexObj;
            Transaction transaction = new Transaction(AccountConstant.TX_TYPE_ACCOUNT_ALIAS);
            transaction.parse(new NulsByteBuffer(HexUtil.hexStringToBytes(txHex)));
            Alias alias = new Alias();
            alias.parse(new NulsByteBuffer(transaction.getTxData()));
            result = aliasService.rollbackAlias(chainId,alias);
        } catch (NulsRuntimeException e) {
            Log.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        Log.debug("ac_aliasTxRollback end");
        return success(result);
    }

}
