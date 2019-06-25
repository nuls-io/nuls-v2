package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.AccountKeyStoreDto;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.AccountTool;
import io.nuls.account.util.Preconditions;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.RPCUtil;
import io.nuls.base.data.Address;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.basic.Page;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.account.util.LoggerUtil.LOG;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/5
 */
@Component
public class AccountCmd extends BaseCmd {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountKeyStoreService keyStoreService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ChainManager chainManager;

    @CmdAnnotation(cmd = "ac_createAccount", version = 1.0, description = "创建指定个数的账户/create a specified number of accounts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "count", parameterType = "int", parameterDes = "需要创建账户的数量"),
            @Parameter(parameterName = "password", parameterType = "int", parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = String.class, description = "创建的账户地址集合"),
    }))
    public Response createAccount(Map params) {
        Map<String, List<String>> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        List<String> list = new ArrayList<>();
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object countObj = params == null ? null : params.get(RpcParameterNameConstant.COUNT);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //创建账户个数
            int count = countObj != null ? (int) countObj : 0;
            //账户密码
            String password = (String) passwordObj;
            //创建账户
            List<Account> accountList = accountService.createAccount(chain, count, password);
            if (accountList != null) {
                accountList.forEach(account -> list.add(account.getAddress().toString()));
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put(RpcConstant.LIST, list);
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_createOfflineAccount", version = 1.0,
            description = "创建离线账户, 该账户不保存到数据库, 并将直接返回账户的所有信息/" +
                    "create an offline account, which is not saved to the database and will directly return all information to the account.")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "count", parameterType = "int", parameterDes = "需要创建账户的数量"),
            @Parameter(parameterName = "password", parameterType = "int", parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = AccountOfflineDto.class, description = "离线账户集合"),
    }))
    public Response createOfflineAccount(Map params) {
        Map<String, List<AccountOfflineDto>> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        List<AccountOfflineDto> accounts = new ArrayList<>();
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object countObj = params == null ? null : params.get(RpcParameterNameConstant.COUNT);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //创建账户个数
            int count = countObj != null ? (int) countObj : 0;
            //账户密码
            String password = (String) passwordObj;

            //Check parameter is correct.
            if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            if (!FormatValidUtils.validPassword(password)) {
                throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
            }

            try {
                for (int i = 0; i < count; i++) {
                    Account account = AccountTool.createAccount(chain.getChainId());
                    if (StringUtils.isNotBlank(password)) {
                        account.encrypt(password);
                    }
                    accounts.add(new AccountOfflineDto(account));
                }
            } catch (NulsException e) {
                throw e;
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put(RpcConstant.LIST, accounts);
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_createContractAccount", version = 1.0, description = "创建智能合约账户/create smart contract account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address", description = "智能合约地址")
    }))
    public Response createContractAccount(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            if (params == null || chainIdObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            Address contractAddress = AccountTool.createContractAddress(chain.getChainId());
            if (contractAddress != null) {
                map.put("address", contractAddress.toString());
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }


    @CmdAnnotation(cmd = "ac_getAccountByAddress", version = 1.0, description = "根据地址获取账户/get account according to address")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = SimpleAccountDto.class))
    public Response getAccountByAddress(Map params) {
        Account account;
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //根据地址查询账户
            account = accountService.getAccount(chain.getChainId(), address);
            if (null == account) {
                return success(null);
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(new SimpleAccountDto(account));
    }

    @CmdAnnotation(cmd = "ac_getAccountList", version = 1.0, description = "获取所有账户集合,并放入缓存/query all account collections and put them in cache")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = SimpleAccountDto.class, description = "返回账户集合"),
    }))
    public Response getAccountList(Map params) {
        Map<String, List<SimpleAccountDto>> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        List<SimpleAccountDto> simpleAccountList = new ArrayList<>();
        Chain chain = null;
        try {
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            List<Account> accountList;
            chain = chainManager.getChain((int) chainIdObj);
            if (chain != null) {
                //query all accounts in a chain
                accountList = accountService.getAccountListByChain(chain.getChainId());
            } else {
                //query all accounts
                accountList = accountService.getAccountList();
            }

            if (null == accountList) {
                return success();
            }
            accountList.forEach(account -> simpleAccountList.add(new SimpleAccountDto((account))));
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put(RpcConstant.LIST, simpleAccountList);
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_getUnencryptedAddressList", version = 1.0, description = "获取本地未加密账户列表/Get a list of local unencrypted accounts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = String.class, description = "返回账户地址集合"),
    }))
    public Response getUnencryptedAddressList(Map params) {
        Map<String, List<String>> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        List<String> unencryptedAddressList = new ArrayList<>();
        Chain chain = null;
        try {
            //query all accounts
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            List<Account> accountList;
            chain = chainManager.getChain((int) chainIdObj);
            if (chain != null) {
                //query all accounts in a chain
                accountList = accountService.getAccountListByChain(chain.getChainId());
            } else {
                //query all accounts
                accountList = accountService.getAccountList();
            }
            if (null == accountList) {
                return success();
            }
            for (Account account : accountList) {
                if (!account.isEncrypted()) {
                    unencryptedAddressList.add(account.getAddress().getBase58());
                }
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put(RpcConstant.LIST, unencryptedAddressList);
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_getEncryptedAddressList", version = 1.0, description = "获取本地加密账户列表/Get a list of locally encrypted accounts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = String.class, description = "返回账户地址集合"),
    }))
    public Response getEncryptedAddressList(Map params) {
        Chain chain = null;
        Map<String, List<String>> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        List<String> encryptedAddressList = new ArrayList<>();
        map.put(RpcConstant.LIST, encryptedAddressList);
        try {
            //query all accounts
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            List<Account> accountList;
            chain = chainManager.getChain((int) chainIdObj);
            if (chain != null) {
                //query all accounts in a chain
                accountList = accountService.getAccountListByChain(chain.getChainId());
            } else {
                //query all accounts
                accountList = accountService.getAccountList();
            }
            if (null == accountList) {
                return success(map);
            }
            for (Account account : accountList) {
                if (account.isEncrypted()) {
                    encryptedAddressList.add(account.getAddress().getBase58());
                }
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_getAddressList", version = 1.0, description = "分页查询账户地址列表/Paging query account address list")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = RpcParameterNameConstant.PAGE_NUMBER, parameterType = "int", parameterDes = "页码"),
            @Parameter(parameterName = RpcParameterNameConstant.PAGE_SIZE, parameterType = "int", parameterDes = "每一页记录数")
    })
    @ResponseData(name = "返回值", description = "返回一个Page对象，账户集合",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
    public Response getAddressList(Map params) {
        Page<String> resultPage;
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object pageNumberObj = params == null ? null : params.get(RpcParameterNameConstant.PAGE_NUMBER);
            Object pageSizeObj = params == null ? null : params.get(RpcParameterNameConstant.PAGE_SIZE);
            if (params == null || chainIdObj == null || pageNumberObj == null || pageSizeObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //页码
            Integer pageNumber = (Integer) pageNumberObj;
            //每页显示数量
            Integer pageSize = (Integer) pageSizeObj;

            if (pageNumber < 1 || pageSize < 1) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }

            //query all accounts in a chain
            List<Account> accountList = accountService.getAccountListByChain(chain.getChainId());
            if (null == accountList) {
                return success(null);
            }
            //根据分页参数返回账户地址列表 Returns the account address list according to paging parameters
            Page<String> page = new Page<>(pageNumber, pageSize);
            page.setTotal(accountList.size());
            int start = (pageNumber - 1) * pageSize;
            if (start >= accountList.size()) {
                return success(page);
            }
            int end = pageNumber * pageSize;
            if (end > accountList.size()) {
                end = accountList.size();
            }
            accountList = accountList.subList(start, end);
            resultPage = new Page<>(page);
            List<String> addressList = new ArrayList<>();
            //只返回账户地址 Only return to account address
            accountList.forEach(account -> addressList.add(account.getAddress().getBase58()));
            resultPage.setList(addressList);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(resultPage);
    }

    @CmdAnnotation(cmd = "ac_removeAccount", version = 1.0, description = "移除指定账户/Remove specified account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "是否成功")
    }))
    public Response removeAccount(Map params) {
        boolean result;
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || addressObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;

            //Remove specified account
            result = accountService.removeAccount(chain.getChainId(), address, password);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        map.put(RpcConstant.VALUE, result);
        return success(map);
    }

    /**
     * 根据地址查询账户私匙,只返回加密账户私钥，未加密账户不返回
     * inquire the account's private key according to the address.
     * only returns the private key of the encrypted account, and the unencrypted account does not return.
     *
     * @param params [chainId,address,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_getPriKeyByAddress", version = 1.0, description = "根据地址查询账户私匙,只返回加密账户私钥，未加密账户不返回/Inquire the account's private key according to the address")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map，包含二个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "priKey", valueType = boolean.class, description = "私钥"),
            @Key(name = "valid", valueType = boolean.class, description = "账户是否存在")
    }))
    public Response getPriKeyByAddress(Map params) {
        String unencryptedPrivateKey;
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || addressObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;

            //Get the account private key
            unencryptedPrivateKey = accountService.getPrivateKey(chain.getChainId(), address, password);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        map.put("priKey", unencryptedPrivateKey);
        //账户是否存在
        map.put("valid", true);
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_getAllPriKey", version = 1.0,
            description = "获取所有本地账户账户私钥，必须保证所有账户密码一致，如果本地账户中的密码不一致，将返回错误信息/" +
                    "Get the all local private keys. if the password in the local account is different, the error message will be returned.")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = String.class, description = "私钥集合")
    }))
    public Response getAllPriKey(Map params) {
        Map<String, List<String>> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        List<String> privateKeyList = new ArrayList<>();
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户密码
            String password = (String) passwordObj;

            //Get the account private key
            privateKeyList = accountService.getAllPrivateKey(chain.getChainId(), password);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        map.put(RpcConstant.LIST, privateKeyList);
        return success(map);
    }

    /**
     * 为账户设置备注
     * set remark for accounts
     *
     * @param params [chainId,address,remark]
     * @return
     */
    @CmdAnnotation(cmd = "ac_setRemark", version = 1.0, description = "为账户设置备注/Set remark for accounts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "备注")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功")
    }))
    public Response setRemark(Map params) {
        Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        boolean result;
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object remarkObj = params == null ? null : params.get(RpcParameterNameConstant.REMARK);
            if (params == null || chainIdObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户备注
            String remark = (String) remarkObj;

            //Get the account private key
            result = accountService.setRemark(chain.getChainId(), address, remark);
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

    /**
     * 根据私钥导入账户
     * import accounts by private key
     *
     * @param params [chainId,priKey,password,overwrite]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importAccountByPriKey", version = 1.0, description = "根据私钥导入账户/Import accounts by private key")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "设置新密码"),
            @Parameter(parameterName = "priKey", parameterType = "String",  parameterDes = "账户私钥"),
            @Parameter(parameterName = "overwrite", parameterType = "boolean", parameterDes = "如果账户已存在,是否覆盖")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.ADDRESS,  description = "导入的账户地址")
    }))
    public Response importAccountByPriKey(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object priKeyObj = params == null ? null : params.get(RpcParameterNameConstant.PRIKEY);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object overwriteObj = params == null ? null : params.get(RpcParameterNameConstant.OVERWRITE);
            if (params == null || chainIdObj == null || priKeyObj == null || passwordObj == null || overwriteObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户私钥
            String priKey = (String) priKeyObj;
            //账户密码
            String password = (String) passwordObj;
            //账户存在时是否覆盖
            Boolean overwrite = (Boolean) overwriteObj;
            //导入账户
            Account account = accountService.importAccountByPrikey(chain, priKey, password, overwrite);
            map.put(RpcConstant.ADDRESS, account.getAddress().toString());
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }


    /**
     * 根据AccountKeyStore导入账户
     * import accounts by AccountKeyStore
     *
     * @param params [chainId,keyStore,password,overwrite]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importAccountByKeystore", version = 1.0, description = "根据AccountKeyStore导入账户/Import accounts by AccountKeyStore")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "设置新密码"),
            @Parameter(parameterName = "keyStore", parameterType = "String",  parameterDes = "keyStore字符串"),
            @Parameter(parameterName = "overwrite", parameterType = "boolean", parameterDes = "如果账户已存在,是否覆盖")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.ADDRESS,  description = "导入的账户地址")
    }))
    public Response importAccountByKeystore(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object keyStoreObj = params == null ? null : params.get(RpcParameterNameConstant.KEYSTORE);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object overwriteObj = params == null ? null : params.get(RpcParameterNameConstant.OVERWRITE);
            if (params == null || chainIdObj == null || keyStoreObj == null || passwordObj == null || overwriteObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //keyStore HEX编码
            String keyStore = (String) keyStoreObj;
            //账户密码
            String password = (String) passwordObj;
            //账户存在时是否覆盖
            Boolean overwrite = (Boolean) overwriteObj;

            AccountKeyStoreDto accountKeyStoreDto;
            try {
                accountKeyStoreDto = JSONUtils.json2pojo(new String(RPCUtil.decode(keyStore)), AccountKeyStoreDto.class);
            } catch (IOException e) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
            }

            //导入账户
            Account account = accountService.importAccountByKeyStore(accountKeyStoreDto.toAccountKeyStore(), chain, password, overwrite);
            map.put(RpcParameterNameConstant.ADDRESS, account.getAddress().toString());
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        }catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_exportAccountKeyStore", version = 1.0, description = "账户备份，导出AccountKeyStore字符串/export account KeyStore")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户密码"),
            @Parameter(parameterName = "filePath", parameterType = "String",  parameterDes = "备份地址", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.PATH,  description = "实际备份文件的地址")
    }))
    public Response exportAccountKeyStore(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object filePathObj = params == null ? null : params.get(RpcParameterNameConstant.FILE_PATH);
            if (params == null || chainIdObj == null || addressObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;
            //文件备份地址
            String filePath = (String) filePathObj;
            //backup account to keystore
            String backupFileName = keyStoreService.backupAccountToKeyStore(filePath, chain.getChainId(), address, password);
            map.put(RpcConstant.PATH, backupFileName);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_setPassword", version = 1.0, description = "设置账户密码/Set account password")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户新密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "是否设置成功")
    }))
    public Response setPassword(Map params) {
        Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        boolean result;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || addressObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;

            //set account password
            result = accountService.setPassword(chain.getChainId(), address, password);
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

    @CmdAnnotation(cmd = "ac_setOfflineAccountPassword", version = 1.0, description = "设置离线账户密码/Set offline account password")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户新密码"),
            @Parameter(parameterName = "priKey", parameterType = "String",  parameterDes = "账户私钥")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.ENCRYPTED_PRIKEY,  description = "返回加密后的私钥")
    }))
    public Response setOfflineAccountPassword(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            Object priKeyObj = params.get(RpcParameterNameConstant.PRIKEY);
            Object passwordObj = params.get(RpcParameterNameConstant.PASSWORD);
            if (chainIdObj == null || addressObj == null || priKeyObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户私钥
            String priKey = (String) priKeyObj;
            //账户密码
            String password = (String) passwordObj;

            //set account password
            String encryptedPriKey = accountService.setOfflineAccountPassword(chain.getChainId(), address, priKey, password);
            map.put(RpcConstant.ENCRYPTED_PRIKEY, encryptedPriKey);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_updatePassword", version = 1.0, description = "根据原密码修改账户密码/Modify the account password by the original password")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户旧密码"),
            @Parameter(parameterName = "newPassword", parameterType = "String",  parameterDes = "账户新密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "是否设置成功")
    }))
    public Response updatePassword(Map params) {
        Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        boolean result;
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params.get(RpcParameterNameConstant.PASSWORD);
            Object newPasswordObj = params.get(RpcParameterNameConstant.NEW_PASSWORD);
            if (chainIdObj == null || addressObj == null || passwordObj == null || newPasswordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户旧密码
            String password = (String) passwordObj;
            //账户新密码
            String newPassword = (String) newPasswordObj;

            //change account password
            result = accountService.changePassword(chain.getChainId(), address, password, newPassword);
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

    @CmdAnnotation(cmd = "ac_updateOfflineAccountPassword", version = 1.0, description = "离线账户修改密码/Offline account change password")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户旧密码"),
            @Parameter(parameterName = "newPassword", parameterType = "String",  parameterDes = "账户新密码"),
            @Parameter(parameterName = "priKey", parameterType = "String",  parameterDes = "账户私钥")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.ENCRYPTED_PRIKEY,  description = "返回修改后加密的私钥")
    }))
    public Response updateOfflineAccountPassword(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            Object priKeyObj = params.get(RpcParameterNameConstant.PRIKEY);
            Object passwordObj = params.get(RpcParameterNameConstant.PASSWORD);
            Object newPasswordObj = params.get(RpcParameterNameConstant.NEW_PASSWORD);
            if (chainIdObj == null || addressObj == null || priKeyObj == null || passwordObj == null || newPasswordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户私钥
            String priKey = (String) priKeyObj;
            //账户旧密码
            String password = (String) passwordObj;
            //账户新密码
            String newPassword = (String) newPasswordObj;

            //set account password
            String encryptedPriKey = accountService.changeOfflinePassword(chain.getChainId(), address, priKey, password, newPassword);
            map.put(RpcConstant.ENCRYPTED_PRIKEY, encryptedPriKey);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_isEncrypted", version = 1.0, description = "根据账户地址获取账户是否加密/Whether the account is encrypted by the account address")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "是否加密")
    }))
    public Response isEncrypted(Map params) {
        Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            if (chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户是否加密
            boolean result = accountService.isEncrypted(chain.getChainId(), address);
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

    @CmdAnnotation(cmd = "ac_validationPassword", version = 1.0, description = "验证账户密码是否正确/Verify that the account password is correct")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "账户密码是否正确")
    }))
    public Response validationPassword(Map params) {
        Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params.get(RpcParameterNameConstant.PASSWORD);
            if (chainIdObj == null || addressObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;

            //check the account is exist
            Account account = accountService.getAccount(chainId, address);
            if (null == account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            //verify that the account password is correct
            boolean result = account.validatePassword(password);
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

    /**
     * 数据摘要签名
     * data digest signature
     *
     * @param params [chainId,address,password,digestHex]
     * @return
     */
    @CmdAnnotation(cmd = "ac_signDigest", version = 1.0, description = "数据摘要签名/Data digest signature")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户密码"),
            @Parameter(parameterName = "data", parameterType = "String",  parameterDes = "待签名数据")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.SIGNATURE,  description = "签名后数据")
    }))
    public Response signDigest(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params.get(RpcParameterNameConstant.PASSWORD);
            Object dataObj = params.get(RpcParameterNameConstant.DATA);
            if (chainIdObj == null || addressObj == null || passwordObj == null || dataObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;
            //待签名的数据
            String dataStr = (String) dataObj;
            //数据解码为字节数组
            byte[] data = RPCUtil.decode(dataStr);
            //sign digest data
            P2PHKSignature signature = accountService.signDigest(data, chain.getChainId(), address, password);
            if (null == signature || signature.getSignData() == null) {
                throw new NulsRuntimeException(AccountErrorCode.SIGNATURE_ERROR);
            }
            try {
                map.put(RpcConstant.SIGNATURE, RPCUtil.encode(signature.serialize()));
            } catch (IOException e) {
                throw new NulsRuntimeException(AccountErrorCode.SERIALIZE_ERROR);
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        }catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    /**
     * 区块数据摘要签名
     * block data digest signature
     *
     * @param params [chainId,address,password,digestHex]
     * @return
     */
    @CmdAnnotation(cmd = "ac_signBlockDigest", version = 1.0, description = "区块数据摘要签名/Block data digest signature")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String",  parameterDes = "账户密码"),
            @Parameter(parameterName = "data", parameterType = "String",  parameterDes = "待签名数据")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.SIGNATURE, description = "签名后数据")
    }))
    public Response signBlockDigest(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Chain chain = null;
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params.get(RpcParameterNameConstant.PASSWORD);
            Object dataObj = params.get(RpcParameterNameConstant.DATA);
            if (chainIdObj == null || addressObj == null || passwordObj == null || dataObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;
            //待签名的数据
            String dataStr = (String) dataObj;
            //数据解码为字节数组
            byte[] data = RPCUtil.decode(dataStr);
            //sign digest data
            BlockSignature signature = accountService.signBlockDigest(data, chain.getChainId(), address, password);
            if (null == signature || signature.getSignData() == null) {
                throw new NulsRuntimeException(AccountErrorCode.SIGNATURE_ERROR);
            }
            try {
                map.put(RpcConstant.SIGNATURE, RPCUtil.encode(signature.serialize()));
            } catch (IOException e) {
                throw new NulsRuntimeException(AccountErrorCode.SERIALIZE_ERROR);
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }

    /**
     * 验证数据签名
     * verify sign
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_verifySignData", version = 1.0, description = "验证数据签名/Verification Data Signature")
    @Parameters(value = {
            @Parameter(parameterName = "pubKey", parameterType = "String",  parameterDes = "账户公钥"),
            @Parameter(parameterName = "sig", parameterType = "String",  parameterDes = "签名"),
            @Parameter(parameterName = "data", parameterType = "String",  parameterDes = "待签名数据")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.SIGNATURE, valueType = boolean.class, description = "签名是否正确")
    }))
    public Response verifySignData(Map params) {
        Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object pubKeyObj = params.get(RpcParameterNameConstant.PUB_KEY);
            Object sigObj = params.get(RpcParameterNameConstant.SIG);
            Object dataObj = params.get(RpcParameterNameConstant.DATA);
            if ( pubKeyObj == null || sigObj == null || dataObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            byte[] pubKey = RPCUtil.decode(pubKeyObj.toString());
            byte[] sig = RPCUtil.decode(sigObj.toString());
            byte[] data = RPCUtil.decode(dataObj.toString());
            boolean result = true;
            if (!ECKey.verify(data, sig, pubKey)) {
                result = false;
            }
            map.put("value", result);
        } catch (NulsRuntimeException e) {
           LOG.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LOG.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(map);
    }


    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }
}
