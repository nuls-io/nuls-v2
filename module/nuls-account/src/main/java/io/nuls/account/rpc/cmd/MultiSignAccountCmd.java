package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.MultiSignTransactionResultDTO;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.util.*;

import static io.nuls.account.util.LoggerUtil.LOG;

/**
 * @author: EdwardChan
 * @description:
 * @date: Dec.20th 2018
 */
@Component
public class MultiSignAccountCmd extends BaseCmd {
    @Autowired
    private MultiSignAccountService multiSignAccountService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ChainManager chainManager;

    @CmdAnnotation(cmd = "ac_createMultiSigAccount", version = 1.0, description = "创建多签账户/create a multi sign account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "pubKeys", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class),
                    parameterDes = "公钥集合(任意普通地址的公钥或存在于当前节点中的普通账户地址)"),
            @Parameter(parameterName = "minSigns", requestType = @TypeDescriptor(value = int.class), parameterDes = "最小签名数")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address",  description = "多签账户地址")
    }))
    public Response createMultiSigAccount(Map params) {
        Chain chain = null;
        Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object pubKeysObj = params == null ? null : params.get(RpcParameterNameConstant.PUB_KEYS);
            Object minSignsObj = params == null ? null : params.get(RpcParameterNameConstant.MIN_SIGNS);
            if (params == null || chainIdObj == null || pubKeysObj == null || minSignsObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            int chainId = chain.getChainId();
            int minSigns = (int) minSignsObj;
            List<String> pubKeysList = (List<String>) pubKeysObj;
            MultiSigAccount multiSigAccount = multiSignAccountService.createMultiSigAccount(chainId, pubKeysList, minSigns);
            if (multiSigAccount == null) {
                throw new NulsRuntimeException(AccountErrorCode.FAILED);
            }
            List<String> pubKeys = new ArrayList<>();
            for(byte[] pubkey : multiSigAccount.getPubKeyList()){
                pubKeys.add(HexUtil.encode(pubkey));
            }
            map.put("address", multiSigAccount.getAddress().getBase58());
            map.put("pubKeys", pubKeys);
            map.put("minSign", multiSigAccount.getM());
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }


    @CmdAnnotation(cmd = "ac_removeMultiSigAccount", version = 1.0, description = "移除多签账户/remove the multi sign account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "多签账户地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "是否移除成功")
    }))
    public Response removeMultiSigAccount(Map params) {
        Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            String address = (String) addressObj;
            boolean result = multiSignAccountService.removeMultiSigAccount(chain.getChainId(), address);
            map.put(RpcConstant.VALUE, result);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_setMultiSigAlias", version = 1.0, description = "设置多签账户别名/set the alias of multi sign account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "多签账户地址"),
            @Parameter(parameterName = "alias", parameterType = "String", parameterDes = "别名"),
            @Parameter(parameterName = "signAddress", parameterType = "String", canNull = true, parameterDes = "第一个签名账户地址(不填则只创建交易不签名)"),
            @Parameter(parameterName = "signPassword", parameterType = "String", canNull = true, parameterDes = "第一个签名账户密码(不填则只创建交易不签名)")
    })
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txHash",  description = "交易hash,交易已完成(已广播)"),
            @Key(name = "completed", valueType = boolean.class, description = "true:交易已完成(已广播),false:交易没完成,没有达到最小签名数")
    }))
    public Object setMultiAlias(Map params) {
        Chain chain = null;
        String address, alias, signAddress, signPassword;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        Object signAddressObj = params == null ? null : params.get(RpcParameterNameConstant.SIGN_ADDREESS);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.SIGN_PASSWORD);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null || aliasObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            int chainId = chain.getChainId();
            address = (String) addressObj;
            signPassword = (String) passwordObj;
            alias = (String) aliasObj;
            signAddress = (String) signAddressObj;
            if (null != signAddress && !AddressTool.validAddress(chainId, signAddress)) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            if (!AddressTool.validAddress(chainId, address) || !AddressTool.isMultiSignAddress(address)) {
                throw new NulsRuntimeException(AccountErrorCode.IS_NOT_MULTI_SIGNATURE_ADDRESS);
            }
            if (StringUtils.isBlank(alias)) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            if (!FormatValidUtils.validAlias(alias)) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_FORMAT_WRONG);
            }
            if (!aliasService.isAliasUsable(chainId, alias)) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_EXIST);
            }
            MultiSignTransactionResultDTO multiSignTransactionResultDto = transactionService.setMultiSignAccountAlias(chain, address, alias, signAddress, signPassword);
            boolean result = false;
            if (multiSignTransactionResultDto.isBroadcasted()) {
                result = true;
            }
            Transaction tx = multiSignTransactionResultDto.getTransaction();
            Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            map.put("completed", result);
            map.put("txHash", tx.getHash().toHex());
            map.put("tx", RPCUtil.encode(tx.serialize()));
            return success(map);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    @CmdAnnotation(cmd = "ac_getMultiSigAccount", version = 1.0, description = "根据多签账户地址获取完整多签账户/Search for multi-signature account by address")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "多签账户地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE,  description = "多签账户序列化数据字符串"),

    }))
    public Object getMultiSigAccount(Map params) {
        Chain chain = null;
        String address;
        MultiSigAccount multiSigAccount;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
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
            if (!AddressTool.validAddress(chain.getChainId(), address) || !AddressTool.isMultiSignAddress(address)) {
                throw new NulsRuntimeException(AccountErrorCode.IS_NOT_MULTI_SIGNATURE_ADDRESS);
            }
            multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(address);
            String data = null == multiSigAccount ? null : RPCUtil.encode(multiSigAccount.serialize());
            Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            map.put(RpcConstant.VALUE, data);
            return success(map);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = "ac_isMultiSignAccountBuilder", version = 1.0, description = "验证是否多签账户的创建者之一/Whether it is multiSig account Builder")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "多签账户地址"),
            @Parameter(parameterName = "pubKey", parameterType = "String", parameterDes = "创建者公钥或已存在于当前节点的地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = Boolean.class, description = "是否多签账户的创建者之一"),

    }))
    public Object isMultiSignAccountBuilder(Map params){
        Chain chain = null;
        String address;
        MultiSigAccount multiSigAccount;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        Object pubkeyObj = params == null ? null : params.get(RpcParameterNameConstant.PUB_KEY);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            String pubkey = (String)pubkeyObj;
            byte[] pubkeyByte = null;
            if (AddressTool.validAddress(chain.getChainId(), pubkey) && AddressTool.validNormalAddress(AddressTool.getAddress(pubkey), chain.getChainId())) {
                //按地址处理,获取该地址的公钥
                Account account = accountService.getAccount(chain.getChainId(), pubkey);
                if(null == account){
                    throw new NulsException(AccountErrorCode.ACCOUNT_NOT_EXIST);
                }
                pubkeyByte = account.getPubKey();
            }else{
                pubkeyByte = HexUtil.decode(pubkey);
            }

            address = (String) addressObj;
            if (!AddressTool.validAddress(chain.getChainId(), address) || !AddressTool.isMultiSignAddress(address)) {
                throw new NulsRuntimeException(AccountErrorCode.IS_NOT_MULTI_SIGNATURE_ADDRESS);
            }
            multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(address);
            if(null == multiSigAccount){
                throw new NulsException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            boolean rs = false;
            for(byte[] pubk : multiSigAccount.getPubKeyList()){
                if(Arrays.equals(pubk, pubkeyByte)){
                    rs = true;
                    break;
                }
            }
            Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            map.put(RpcConstant.VALUE, rs);
            return success(map);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }



    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }

}
