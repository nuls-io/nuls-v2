package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.util.annotation.ResisterTx;
import io.nuls.account.util.annotation.TxMethodType;
import io.nuls.account.util.log.LogUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
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
    public Response setAlias(Map params) {
        LogUtil.debug("ac_setAlias start,params size:{}", params == null ? 0 : params.size());
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
            LogUtil.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>();
        result.put("txHash", txHash);
        LogUtil.debug("ac_getAliasByAddress end");
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
        LogUtil.debug("ac_getAliasFee start,params size:{}", params == null ? 0 : params.size());
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
            LogUtil.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>();
        result.put("fee", fee == null ? "" : fee.toString());
        //TODO is need to format?
        LogUtil.debug("ac_getAliasFee end");
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
        LogUtil.debug("ac_getAliasByAddress start,params size:{}", params == null ? 0 : params.size());
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
            LogUtil.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>();
        result.put("alias", alias);
        LogUtil.debug("ac_getAliasByAddress end");
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
        LogUtil.debug("ac_isAliasUsable start,params size:{}", params == null ? 0 : params.size());
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
            LogUtil.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("value", isAliasUsable);
        LogUtil.debug("ac_isAliasUsable end");
        return success(result);
    }

    /**
     * set multi sign alias
     *
     * @param params
     * @return
     **/
    @CmdAnnotation(cmd = "ac_setMultiSigAlias", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "set multi sign alias")
    public Response setMultiSigAlias(Map params) {
        return null;
    }

    /**
     * validate the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_accountTxValidate", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "validate the transaction")
    public Response accountTxValidate(Map params) {
        LogUtil.debug("ac_accountTxValidate start,params size:{}", params == null ? 0 : params.size());
        int chainId = 0;
        List<String> txHexList;
        List<Transaction> lists = null;
        List<Transaction> result = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txHexListObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX_LIST);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txHexListObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txHexList = (List<String>) txHexListObj;
            //TODO after the parameter format was determine,here will be modify
            if (txHexList != null) {
                //Transaction transaction = Transaction.getInstance(txHex);
                txHexList.forEach(txHex -> {
                    try {
                        lists.add(Transaction.getInstance(txHex));
                    } catch (NulsException e) {
                        e.printStackTrace();
                    }
                });
                result = aliasService.accountTxValidate(chainId, lists);
            }
        } catch (NulsRuntimeException e) {
            LogUtil.error("", e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            LogUtil.error("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, List<Transaction>> resultMap = new HashMap<>();
        resultMap.put("list", result);
        LogUtil.debug("ac_accountTxValidate end");
        return success(resultMap);
    }

    /**
     * validate the transaction of alias
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_aliasTxValidate", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "validate the transaction of alias")
    @ResisterTx(txType = AccountConstant.TX_TYPE_ACCOUNT_ALIAS, methodType = TxMethodType.VALID, methodName = "ac_aliasTxValidate")
    public Response aliasTxValidate(Map params) {
        LogUtil.debug("ac_aliasTxValidate start,params size:{}", params == null ? 0 : params.size());
        boolean result;
        int chainId;
        String txHex;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txHexObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txHex = (String) txHexObj;
            Transaction transaction = Transaction.getInstance(txHex);
            result = aliasService.aliasTxValidate(chainId, transaction);
        } catch (NulsRuntimeException e) {
            LogUtil.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        LogUtil.debug("ac_aliasTxCommit end");
        return success(resultMap);
    }

    /**
     * commit the alias transaction
     */
    @CmdAnnotation(cmd = "ac_aliasTxCommit", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "commit the alias transaction")
    @ResisterTx(txType = AccountConstant.TX_TYPE_ACCOUNT_ALIAS, methodType = TxMethodType.COMMIT, methodName = "ac_aliasTxCommit")
    public Response aliasTxCommit(Map params) throws NulsException {
        LogUtil.debug("ac_aliasTxCommit start,params size:{}", params == null ? 0 : params.size());
        boolean result = false;
        int chainId = 0;
        String txHex;
        //TODO is it need to verify secondaryDataHex?
        String secondaryDataHex;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txHexObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX);
        Object secondaryDataHexObj = params == null ? null : params.get(RpcParameterNameConstant.SECONDARY_DATA_Hex);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txHex = (String) txHexObj;
            Transaction transaction = Transaction.getInstance(txHex);
            Alias alias = new Alias();
            alias.parse(new NulsByteBuffer(transaction.getTxData()));
            result = aliasService.aliasTxCommit(chainId, alias);
        } catch (NulsRuntimeException e) {
            LogUtil.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        LogUtil.debug("ac_aliasTxCommit end");
        return success(resultMap);
    }

    /**
     * rollback the alias info which saved in the db
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_aliasTxRollback", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "rollback the alias info which saved in the db")
    @ResisterTx(txType = AccountConstant.TX_TYPE_ACCOUNT_ALIAS, methodType = TxMethodType.ROLLBACK, methodName = "ac_aliasTxRollback")
    public Response rollbackAlias(Map params) throws NulsException {
        LogUtil.debug("ac_aliasTxRollback start,params size:{}", params == null ? 0 : params.size());
        boolean result = false;
        int chainId = 0;
        String txHex;
        //TODO is it need to verify secondaryDataHex?
        String secondaryDataHex;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txHexObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX);
        Object secondaryDataHexObj = params == null ? null : params.get(RpcParameterNameConstant.SECONDARY_DATA_Hex);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txHex = (String) txHexObj;
            Transaction transaction = Transaction.getInstance(txHex);
            Alias alias = new Alias();
            alias.parse(new NulsByteBuffer(transaction.getTxData()));
            result = aliasService.rollbackAlias(chainId, alias);
        } catch (NulsRuntimeException e) {
            LogUtil.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        LogUtil.debug("ac_aliasTxRollback end");
        return success(resultMap);
    }

}
