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
@NulsCoresCmd(module = ModuleE.AC)
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

    @CmdAnnotation(cmd = "ac_createMultiSignAccount", version = 1.0, description = "Create a multi signature account/create a multi sign account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "pubKeys", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class),
                    parameterDes = "Public key set(Public key of any ordinary address or ordinary account address existing in the current node)"),
            @Parameter(parameterName = "minSigns", requestType = @TypeDescriptor(value = int.class), parameterDes = "Minimum number of signatures")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address",  description = "Multiple account addresses signed")
    }))
    public Response createMultiSignAccount(Map params) {
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
            int minSigns = (Integer) minSignsObj;
            List<String> pubKeysList = (List<String>) pubKeysObj;
            if(pubKeysList.size() == 0 || minSigns == 0){
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            if (minSigns == 0) {
                minSigns = pubKeysList.size();
            }
            if(pubKeysList.size() < minSigns){
                return failed(AccountErrorCode.SIGN_COUNT_TOO_LARGE);
            }

            MultiSigAccount multiSigAccount = multiSignAccountService.createMultiSigAccount(chain, pubKeysList, minSigns);
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


    @CmdAnnotation(cmd = "ac_removeMultiSignAccount", version = 1.0, description = "Remove multiple signed accounts/remove the multi sign account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Multiple account addresses signed")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "Was removal successful")
    }))
    public Response removeMultiSignAccount(Map params) {
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

    @CmdAnnotation(cmd = "ac_setMultiSignAlias", version = 1.0, description = "Set multiple account aliases/set the alias of multi sign account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Multiple account addresses signed"),
            @Parameter(parameterName = "alias", parameterType = "String", parameterDes = "alias"),
            @Parameter(parameterName = "signAddress", parameterType = "String", canNull = true, parameterDes = "First signature account address(If left blank, only create transactions without signing)"),
            @Parameter(parameterName = "signPassword", parameterType = "String", canNull = true, parameterDes = "First signature account password(If left blank, only create transactions without signing)")
    })
    @ResponseData(name = "Return value", description = "Return aMap,Including threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign"),
            @Key(name = "txHash",  description = "transactionhash"),
            @Key(name = "completed", valueType = boolean.class, description = "true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures")
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

    @CmdAnnotation(cmd = "ac_getMultiSignAccount", version = 1.0, description = "Obtain the complete multi signature account based on the address of the multi signature account/Search for multi-signature account by address")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Multiple account addresses signed")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE,  description = "Serializing data strings for multiple account signatures"),

    }))
    public Object getMultiSignAccount(Map params) {
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

    @CmdAnnotation(cmd = "ac_isMultiSignAccountBuilder", version = 1.0, description = "Verify if one of the creators of the multi signed account/Whether it is multiSign account Builder")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Multiple account addresses signed"),
            @Parameter(parameterName = "pubKey", parameterType = "String", parameterDes = "Creator public key or address that already exists on the current node")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = Boolean.class, description = "Is it one of the creators who signed multiple accounts"),

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
                //Process by address,Obtain the public key for this address
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
                throw new NulsException(AccountErrorCode.MULTISIGN_ACCOUNT_NOT_EXIST);
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
