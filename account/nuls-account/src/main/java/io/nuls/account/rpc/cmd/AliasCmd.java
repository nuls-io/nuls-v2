package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.AccountKeyStore;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.dto.AccountKeyStoreDto;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.util.AccountTool;
import io.nuls.base.data.Page;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.FormatValidUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.util.ArrayList;
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
    @CmdAnnotation(cmd = "ac_setAlias", version = 1.0, preCompatible = true)
    public CmdResponse setAlias(List<String> params) {
        return null;
    }

    /**
     * Gets to set the alias transaction fee
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_getAliasFee", version = 1.0, preCompatible = true)
    public CmdResponse getAliasFee(List<String> params) {
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
    @CmdAnnotation(cmd = "ac_getAliasByAddress", version = 1.0, preCompatible = true)
    public CmdResponse getAliasByAddress(List<String> params) {
        Log.debug("ac_getAliasByAddress start,params size:{}", params == null ? 0 : params.size());
        String alias;
        short chainId;
        String address;
        try {
            if (params.size() != 2 || params.get(0) == null || params.get(1) == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = Short.parseShort(params.get(0));
            address = params.get(1);
            alias = aliasService.getAliasByAddress(chainId, address);
        } catch (NulsRuntimeException e) {
            Log.info("", e);
            return failed(e.getErrorCode(), null);
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION, null);
        }
        Map<String, String> result = new HashMap<>();
        result.put("alias", alias);
        Log.debug("ac_getAliasByAddress end");
        return success(AccountConstant.SUCCESS_MSG, result);
    }

    /**
     * check whether the account is usable
     *
     * @param params
     * @return CmdResponse
     */
    @CmdAnnotation(cmd = "ac_isAliasUsable", version = 1.0, preCompatible = true)
    public CmdResponse isAliasUsable(List<String> params) {
        Log.debug("ac_isAliasUsable start,params size:{}", params == null ? 0 : params.size());
        boolean isAliasUsable = false;
        short chainId;
        String alias;
        try {
            if (params.size() != 2 || params.get(0) == null || params.get(1) == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = Short.parseShort(params.get(0));
            alias = params.get(1);
            isAliasUsable = aliasService.isAliasUsable(chainId, alias);
        } catch (NulsRuntimeException e) {
            Log.info("", e);
            return failed(e.getErrorCode(), null);
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION, null);
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("value", isAliasUsable);
        Log.debug("ac_isAliasUsable end");
        return success(AccountConstant.SUCCESS_MSG, result);
    }

    /**
     * set multi sign alias
     *
     * @param params
     * @return
     **/
    @CmdAnnotation(cmd = "ac_setMultiSigAlias", version = 1.0, preCompatible = true)
    public CmdResponse setMultiSigAlias(List<String> params) {
        return null;
    }

    /**
     * validate the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_accountTxValidate", version = 1.0, preCompatible = true)
    public CmdResponse accountTxValidate(List<String> params) {
        return null;
    }

    /**
     * validate the transaction of alias
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_aliasTxValidate", version = 1.0, preCompatible = true)
    public CmdResponse aliasTxValidate(List<String> params) {
        return null;
    }

    /**
     * commit the alias transaction
     */
    @CmdAnnotation(cmd = "ac_aliasTxCommit", version = 1.0, preCompatible = true)
    public CmdResponse aliasTxCommit(List<String> params) throws NulsException {
        return null;
    }

    /**
     * rollback the alias info which saved in the db
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_rollbackAlias", version = 1.0, preCompatible = true)
    public CmdResponse rollbackAlias(List<String> params) throws NulsException {
        return null;
    }

}
