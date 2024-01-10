package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.AccountKeyStore;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.AccountKeyStoreDTO;
import io.nuls.account.model.dto.AccountOfflineDTO;
import io.nuls.account.model.dto.SimpleAccountDTO;
import io.nuls.account.model.po.AccountBlockPO;
import io.nuls.account.model.po.AccountContractCallPO;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.storage.AccountForTransferOnContractCallStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.account.util.Preconditions;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.basic.Page;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import org.checkerframework.checker.units.qual.A;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.account.util.LoggerUtil.LOG;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/5
 */
@Component
@NulsCoresCmd(module = ModuleE.AC)
public class AccountCmd extends BaseCmd {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountKeyStoreService keyStoreService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AccountForTransferOnContractCallStorageService accountForTransferOnContractCallStorageService;

    public AccountCmd() {
    }

    @CmdAnnotation(cmd = "ac_createAccount", version = 1.0, description = "Create a specified number of accounts/create a specified number of accounts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "count", requestType = @TypeDescriptor(value = int.class), parameterDes = "Number of accounts to be created"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = String.class, description = "The set of account addresses created"),
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Number of accounts created
            int count = countObj != null ? (int) countObj : 0;
            //Account password
            String password = (String) passwordObj;
            //Create an account
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
            description = "Create an offline account, This account is not saved to the database, And will directly return all information of the account/" +
                    "create an offline account, which is not saved to the database and will directly return all information to the account.")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "count", requestType = @TypeDescriptor(value = int.class), parameterDes = "Number of accounts to be created"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = AccountOfflineDTO.class, description = "Offline account collection"),
    }))
    public Response createOfflineAccount(Map params) {
        Map<String, List<AccountOfflineDTO>> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        List<AccountOfflineDTO> accounts = new ArrayList<>();
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Number of accounts created
            int count = countObj != null ? (int) countObj : 0;
            //Account password
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
                    accounts.add(new AccountOfflineDTO(account));
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

    @CmdAnnotation(cmd = "ac_createContractAccount", version = 1.0, description = "Create a smart contract account/create smart contract account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address", description = "Smart contract address")
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
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


    @CmdAnnotation(cmd = "ac_getAccountByAddress", version = 1.0, description = "Obtain account information through address/get account info according to address")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = SimpleAccountDTO.class))
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Search account based on address
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
        return success(new SimpleAccountDTO(account));
    }


    @CmdAnnotation(cmd = "ac_getAccountList", version = 1.0, description = "Get all account collections,And put it in cache/query all account collections and put them in cache")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = SimpleAccountDTO.class, description = "Return account collection"),
    }))
    public Response getAccountList(Map params) {
        Map<String, List<SimpleAccountDTO>> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        List<SimpleAccountDTO> simpleAccountList = new ArrayList<>();
        Chain chain = null;
        try {
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            List<Account> accountList;
            chain = chainManager.getChain((Integer) chainIdObj);
            if (chain != null) {
                accountList = accountService.getAccountListByChain(chain.getChainId());
            } else {
                accountList = accountService.getAccountList();
            }
            if (null == accountList) {
                map.put(RpcConstant.LIST, simpleAccountList);
                return success(map);
            }
            accountList.forEach(account -> simpleAccountList.add(new SimpleAccountDTO((account))));
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


    @CmdAnnotation(cmd = "ac_getEncryptedAddressList", version = 1.0, description = "Get a list of local encrypted accounts/Get a list of locally encrypted accounts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = String.class, description = "Return account address set"),
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
            chain = chainManager.getChain((Integer) chainIdObj);
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

    @CmdAnnotation(cmd = "ac_getAddressList", version = 1.0, description = "Pagination query account address list/Paging query account address list")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = RpcParameterNameConstant.PAGE_NUMBER, requestType = @TypeDescriptor(value = int.class), parameterDes = "Page number"),
            @Parameter(parameterName = RpcParameterNameConstant.PAGE_SIZE, requestType = @TypeDescriptor(value = int.class), parameterDes = "Number of records per page")
    })
    @ResponseData(name = "Return value", description = "Return aPageObject, Account Collection",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
    public Response getAddressList(Map params) {
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Page number
            Integer pageNumber = (Integer) pageNumberObj;
            //Display quantity per page
            Integer pageSize = (Integer) pageSizeObj;

            if (pageNumber < 1 || pageSize < 1) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            //Return account address list based on paging parameters Returns the account address list according to paging parameters
            Page<String> page = new Page<>(pageNumber, pageSize);
            List<String> addressList = new ArrayList<>();
            //query all accounts in a chain
            List<Account> accountList = accountService.getAccountListByChain(chain.getChainId());
            if (null == accountList) {
                page.setList(addressList);
                return success(page);
            }
            page.setTotal(accountList.size());
            int start = (pageNumber - 1) * pageSize;
            if (start >= accountList.size()) {
                page.setList(addressList);
                return success(page);
            }
            int end = pageNumber * pageSize;
            if (end > accountList.size()) {
                end = accountList.size();
            }
            accountList = accountList.subList(start, end);
            //Only return account address Only return to account address
            accountList.forEach(account -> addressList.add(account.getAddress().getBase58()));
            page.setList(addressList);
            return success(page);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    @CmdAnnotation(cmd = "ac_removeAccount", version = 1.0, description = "Remove specified account/Remove specified account")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "Whether successful")
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account password
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


    @CmdAnnotation(cmd = "ac_getPubKey", version = 1.0, description = "Based on account address and password,Query account public key/Get the account's public key")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap, including onekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "pubKey", description = "Public key")
    }))
    public Response getPubKey(Map params) {
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account password
            String password = (String) passwordObj;

            //Get the account private key
            String publicKey = accountService.getPublicKey(chain.getChainId(), address, password);
            Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            map.put("pubKey", publicKey);
            return success(map);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    /**
     * By account address and password,Query account private key
     * inquire the account's private key according to the address.
     * only returns the private key of the encrypted account, and the unencrypted account does not return.
     *
     * @param params [chainId,address,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_getPriKeyByAddress", version = 1.0, description = "By account address and password,Query account private key/Inquire the account's private key according to the address")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap, including twokey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "priKey", description = "Private key"),
            @Key(name = "pubKey", description = "Public key")
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account password
            String password = (String) passwordObj;
            if (!AddressTool.validAddress(chain.getChainId(), address)) {
                return failed(AccountErrorCode.ADDRESS_ERROR);
            }
            int chainId = chain.getChainId();
            Account account = accountService.getAccount(chainId, address);
            unencryptedPrivateKey = accountService.getPrivateKey(chain.getChainId(), account, password);
            Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            map.put("priKey", unencryptedPrivateKey);
            map.put("pubKey", HexUtil.encode(account.getPubKey()));
            return success(map);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    @CmdAnnotation(cmd = "ac_getAllPriKey", version = 1.0,
            description = "To obtain all local account private keys, it is necessary to ensure that all account passwords are consistent. If the passwords in the local account are inconsistent, an error message will be returned/" +
                    "Get the all local private keys. if the password in the local account is different, the error message will be returned.")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.LIST, valueType = List.class, valueElement = String.class, description = "Private key set")
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account password
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
     * Set notes for the account
     * set remark for accounts
     *
     * @param params [chainId,address,remark]
     * @return
     */
    @CmdAnnotation(cmd = "ac_setRemark", version = 1.0, description = "Set notes for the account/Set remark for accounts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "Remarks")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "Whether successful")
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account notes
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
     * Import account based on private key
     * import accounts by private key
     *
     * @param params [chainId,priKey,password,overwrite]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importAccountByPriKey", version = 1.0, description = "Import account based on private key/Import accounts by private key")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Set a new password"),
            @Parameter(parameterName = "priKey", parameterType = "String", parameterDes = "Account private key"),
            @Parameter(parameterName = "overwrite", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "If the account already exists,Is it covered")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.ADDRESS, description = "Imported account address")
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
            //chainID
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account private key
            String priKey = (String) priKeyObj;
            //Account password
            String password = (String) passwordObj;
            //Is the account overwritten when it exists
            Boolean overwrite = (Boolean) overwriteObj;
            //Import account
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
     * according toAccountKeyStoreImport account
     * import accounts by AccountKeyStore
     *
     * @param params [chainId,keyStore,password,overwrite]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importAccountByKeystore", version = 1.0, description = "according toAccountKeyStoreImport account/Import accounts by AccountKeyStore")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Set a new password"),
            @Parameter(parameterName = "keyStore", parameterType = "String", parameterDes = "keyStorecharacter string"),
            @Parameter(parameterName = "overwrite", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "If the account already exists,Is it covered")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.ADDRESS, description = "Imported account address")
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //keyStore HEXcoding
            String keyStore = (String) keyStoreObj;
            //Account password
            String password = (String) passwordObj;
            //Is the account overwritten when it exists
            Boolean overwrite = (Boolean) overwriteObj;

            AccountKeyStoreDTO accountKeyStoreDto;
            try {
                accountKeyStoreDto = JSONUtils.json2pojo(new String(RPCUtil.decode(keyStore)), AccountKeyStoreDTO.class);
            } catch (IOException e) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
            }

            //Import account
            Account account = accountService.importAccountByKeyStore(accountKeyStoreDto.toAccountKeyStore(), chain, password, overwrite);
            map.put(RpcParameterNameConstant.ADDRESS, account.getAddress().toString());
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
     * Import all files in a folderkeystorefile
     * import accounts by AccountKeyStore
     *
     * @param params [chainId,dirPath,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importsKeyStoreFiles", version = 1.0, description = "according toAccountKeyStoreImport account/Import accounts by AccountKeyStore")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "dirPath", parameterType = "String", parameterDes = "Folder path"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.ADDRESS, description = "Imported account address")
    }))
    public Response importsKeyStoreFiles(Map params) {
        Chain chain = null;
        Map<String, String> map = new HashMap<>();
        BufferedReader reader = null;
        StringBuffer stringBuffer = null;
        try {
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object pathObj = params == null ? null : params.get(RpcParameterNameConstant.DIR_PATH);
            if (params == null || chainIdObj == null || pathObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //dirPath
            String dirPath = (String) pathObj;
            File baseDir = new File(dirPath);
            if (!baseDir.exists() || !baseDir.isDirectory()) {
                throw new NulsException(AccountErrorCode.ACCOUNTKEYSTORE_FILE_NOT_EXIST);
            }

            File[] array = baseDir.listFiles();
            String keystore;
            List<AccountKeyStore> accountKeyStoreList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                //If it iskeystorefile
                if (array[i].isFile() && array[i].getName().endsWith(".keystore")) {
                    //Get file content
                    reader = new BufferedReader(new FileReader(array[i]));
                    stringBuffer = new StringBuffer();
                    String tempStr;
                    while ((tempStr = reader.readLine()) != null) {
                        stringBuffer.append(tempStr);
                    }
                    reader.close();
                    keystore = stringBuffer.toString();

                    AccountKeyStoreDTO accountKeyStoreDto;
                    try {
                        accountKeyStoreDto = JSONUtils.json2pojo(keystore, AccountKeyStoreDTO.class);
                        if (StringUtils.isBlank(accountKeyStoreDto.getAddress()) ||
                                StringUtils.isBlank(accountKeyStoreDto.getEncryptedPrivateKey()) ||
                                StringUtils.isBlank(accountKeyStoreDto.getPubKey())
                        ) {
                            throw new NulsException(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED, array[i].getName());
                        }
                        accountKeyStoreList.add(accountKeyStoreDto.toAccountKeyStore());
                    } catch (IOException e) {
                        throw new NulsException(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED, array[i].getName());
                    }
                }
            }
            accountService.importAccountListByKeystore(accountKeyStoreList, chain);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return success(map);
    }

    @CmdAnnotation(cmd = "ac_exportKeyStoreJson", version = 1.0, description = "exportAccountKeyStorecharacter string/export account KeyStore json")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")})
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "keyStore", description = "keyStorecharacter string")
    }))
    public Response exportKeyStoreJson(Map params) {
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account password
            String password = (String) passwordObj;

            AccountKeyStore accountKeyStore = keyStoreService.getKeyStore(chain.getChainId(), address, password);
            AccountKeyStoreDTO storeDTO = new AccountKeyStoreDTO(accountKeyStore);
            map.put("keystore", JSONUtils.obj2json(storeDTO));
            return success(map);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    @CmdAnnotation(cmd = "ac_exportAccountKeyStore", version = 1.0, description = "Account backup, exportAccountKeyStorecharacter string/export account KeyStore")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password"),
            @Parameter(parameterName = "filePath", parameterType = "String", parameterDes = "Backup address", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.PATH, description = "The actual backup file address")
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account password
            String password = (String) passwordObj;
            //File backup address
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

    @CmdAnnotation(cmd = "ac_updatePassword", version = 1.0, description = "Change the account password based on the original password/Modify the account password by the original password")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Old account password"),
            @Parameter(parameterName = "newPassword", parameterType = "String", parameterDes = "Account New Password")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "Is it successfully set up")
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Old account password
            String password = (String) passwordObj;
            //Account New Password
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

    @CmdAnnotation(cmd = "ac_updateOfflineAccountPassword", version = 1.0, description = "Offline account password modification/Offline account change password")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Old account password"),
            @Parameter(parameterName = "newPassword", parameterType = "String", parameterDes = "Account New Password"),
            @Parameter(parameterName = "priKey", parameterType = "String", parameterDes = "Account private key")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.ENCRYPTED_PRIKEY, description = "Return the encrypted private key after modification")
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account private key
            String priKey = (String) priKeyObj;
            //Old account password
            String password = (String) passwordObj;
            //Account New Password
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

    @CmdAnnotation(cmd = "ac_validationPassword", version = 1.0, description = "Verify if the account password is correct/Verify that the account password is correct")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "Is the account password correct")
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
            //chainID
            int chainId = (int) chainIdObj;
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account password
            String password = (String) passwordObj;

            //check the account is exist
            Account account = accountService.getAccount(chainId, address);
            if (null == account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST, address);
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

    @CmdAnnotation(cmd = "ac_validationWhitelistForTransferOnContractCall", version = 1.0, description = "Verify if the account is on the contract whitelist")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE, valueType = boolean.class, description = "Is the account on the contract whitelist")
    }))
    public Response validationWhitelistForTransferOnContractCall(Map params) {
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
            //chainID
            int chainId = (int) chainIdObj;
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;

            //check the account
            boolean result = accountService.validationWhitelistForTransferOnContractCall(chainId, address);
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

    @CmdAnnotation(cmd = "ac_getAllContractCallAccount", version = 1.0, description = "Query the whitelist of accounts that allow regular transfers when calling contracts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class))
    public Response getAllContractCallAccount(Map params) {
        Chain chain = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        try {
            // check parameters
            if (params == null || chainIdObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            List<AccountContractCallPO> accountList = accountForTransferOnContractCallStorageService.getAccountList();
            if (accountList == null) {
                accountList = Collections.EMPTY_LIST;
            }
            List<String> collect = accountList.stream().map(a -> AddressTool.getStringAddressByBytes(a.getAddress())).collect(Collectors.toList());
            Map<String, Object> result = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            result.put("value", collect);
            return success(result);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * Data Summary Signature
     * data digest signature
     *
     * @param params [chainId,address,password,digestHex]
     * @return
     */
    @CmdAnnotation(cmd = "ac_signDigest", version = 1.0, description = "Data Summary Signature/Data digest signature")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password"),
            @Parameter(parameterName = "data", parameterType = "String", parameterDes = "Data to be signed")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.SIGNATURE, description = "Data after signature")
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account password
            String password = (String) passwordObj;
            //Data to be signed
            String dataStr = (String) dataObj;
            //Decoding data into byte arrays
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
     * Block Data Summary Signature
     * block data digest signature
     *
     * @param params [chainId,address,password,digestHex]
     * @return
     */
    @CmdAnnotation(cmd = "ac_signBlockDigest", version = 1.0, description = "Block Data Summary Signature/Block data digest signature")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "Account address"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "Account password"),
            @Parameter(parameterName = "data", parameterType = "String", parameterDes = "Data to be signed")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.SIGNATURE, description = "Data after signature")
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
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            //Account address
            String address = (String) addressObj;
            //Account password
            String password = (String) passwordObj;
            //Data to be signed
            String dataStr = (String) dataObj;
            //Decoding data into byte arrays
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
     * Verify data signature
     * verify sign
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_verifySignData", version = 1.0, description = "Verify data signature/Verification Data Signature")
    @Parameters(value = {
            @Parameter(parameterName = "pubKey", parameterType = "String", parameterDes = "Account public key"),
            @Parameter(parameterName = "sig", parameterType = "String", parameterDes = "autograph"),
            @Parameter(parameterName = "data", parameterType = "String", parameterDes = "Data to be signed")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.SIGNATURE, valueType = boolean.class, description = "Is the signature correct")
    }))
    public Response verifySignData(Map params) {
        Map<String, Boolean> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        try {
            // check parameters
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object pubKeyObj = params.get(RpcParameterNameConstant.PUB_KEY);
            Object sigObj = params.get(RpcParameterNameConstant.SIG);
            Object dataObj = params.get(RpcParameterNameConstant.DATA);
            if (pubKeyObj == null || sigObj == null || dataObj == null) {
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
