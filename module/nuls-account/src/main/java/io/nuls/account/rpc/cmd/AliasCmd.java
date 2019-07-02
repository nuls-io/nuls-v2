package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.account.util.LoggerUtil.LOG;

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
    @Autowired
    private ChainManager chainManager;

    @CmdAnnotation(cmd = "ac_setAlias", version = 1.0, description = "设置别名/Set the alias of account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户密码"),
            @Parameter(parameterName = "alias", parameterType = "String",  parameterDes = "别名")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash",  description = "设置别名交易hash")
    }))
    public Response setAlias(Map params) {
        Chain chain = null;
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            address = (String) addressObj;
            password = (String) passwordObj;
            alias = (String) aliasObj;
            Transaction transaction = aliasService.setAlias(chain, address, password, alias);
            if (transaction != null && transaction.getHash() != null) {
                txHash = transaction.getHash().toHex();
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        result.put("txHash", txHash);
        return success(result);
    }

//    /**
//     * Gets to set the alias transaction fee
//     *
//     * @param params
//     * @return
//     */
//    @CmdAnnotation(cmd = "ac_getAliasFee", version = 1.0, description = "设置别名手续费/Gets to set the alias transaction fee")
//    public Response getAliasFee(Map params) {
//        Chain chain = null;
//        String address, alias;
//        BigInteger fee;
//        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
//        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
//        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
//        try {
//            // check parameters
//            if (params == null || chainIdObj == null || addressObj == null) {
//                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
//            }
//            chain = chainManager.getChain((int) chainIdObj);
//            if (null == chain) {
//                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
//            }
//            address = (String) addressObj;
//            alias = (String) aliasObj;
//            fee = aliasService.getAliasFee(chain, address, alias);
//        } catch (NulsRuntimeException e) {
//            errorLogProcess(chain, e);
//            return failed(e.getErrorCode());
//        } catch (Exception e) {
//            errorLogProcess(chain, e);
//            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
//        }
//        Map<String, String> result = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
//        result.put("fee", fee == null ? "" : fee.toString());
//        return success(result);
//    }

    @CmdAnnotation(cmd = "ac_getAliasByAddress", version = 1.0, description = "根据地址获取别名/get the alias by address")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "alias",  description = "别名")
    }))
    public Response getAliasByAddress(Map params) {
        String alias;
        Chain chain = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        String address;
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            address = (String) addressObj;
            alias = aliasService.getAliasByAddress(chain.getChainId(), address);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        result.put("alias", alias);
        return success(result);
    }

    /**
     * check whether the account is usable
     *
     * @param params
     * @return CmdResponse
     */
    @CmdAnnotation(cmd = "ac_isAliasUsable", version = 1.0, description = "检查别名是否可用/check whether the account is usable")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "alias", parameterType = "String",  parameterDes = "别名")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "别名是否可以使用")
    }))
    public Response isAliasUsable(Map params) {
        boolean isAliasUsable = false;
        Chain chain = null;
        String alias;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        try {
            // check parameters
            if (params == null || chainIdObj == null || aliasObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            alias = (String) aliasObj;
            isAliasUsable = aliasService.isAliasUsable(chain.getChainId(), alias);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> result = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        result.put("value", isAliasUsable);
        return success(result);
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }

}
