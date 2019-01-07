package io.nuls.account.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.dto.AccountKeyStoreDto;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.MulitpleAddressTransferDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.AccountTool;
import io.nuls.account.util.log.LogUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Page;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.FormatValidUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 创建指定个数的账户
     * create a specified number of accounts
     *
     * @param params [chainId,count,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_createAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "create a specified number of accounts")
    public Response createAccount(Map params) {
        LogUtil.debug("ac_createAccount start");
        Map<String, List<String>> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object countObj = params == null ? null : params.get(RpcParameterNameConstant.COUNT);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //创建账户个数
            int count = countObj != null ? (int) countObj : 0;
            //账户密码
            String password = (String) passwordObj;
            //创建账户
            List<Account> accountList = accountService.createAccount(chainId, count, password);
            if (accountList != null) {
                accountList.forEach(account -> list.add(account.getAddress().toString()));
            }
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_createAccount end");
        return success(list);
    }

    /**
     * 创建离线账户, 该账户不保存到数据库, 并将直接返回账户的所有信息
     * create an offline account, which is not saved to the database and will directly return all information to the account.
     *
     * @param params [chainId,count,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_createOfflineAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "create an offline account")
    public Response createOfflineAccount(Map params) {
        LogUtil.debug("ac_createOfflineAccount start");
        Map<String, List<AccountOfflineDto>> map = new HashMap<>();
        List<AccountOfflineDto> accounts = new ArrayList<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object countObj = params == null ? null : params.get(RpcParameterNameConstant.COUNT);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //创建账户个数
            int count = countObj != null ? (int) countObj : 0;
            //账户密码
            String password = (String) passwordObj;

            //Check parameter is correct.
            if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            if (StringUtils.isNotBlank(password) && !FormatValidUtils.validPassword(password)) {
                throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
            }

            try {
                for (int i = 0; i < count; i++) {
                    Account account = AccountTool.createAccount(chainId);
                    if (StringUtils.isNotBlank(password)) {
                        account.encrypt(password);
                    }
                    accounts.add(new AccountOfflineDto(account));
                }
            } catch (NulsException e) {
                throw e;
            }
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_createOfflineAccount end");
        return success(accounts);
    }

    /**
     * 根据地址获取账户
     * get account according to address
     *
     * @param params [chainId,address]
     * @return
     */
    @CmdAnnotation(cmd = "ac_getAccountByAddress", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "get account according to address")
    public Response getAccountByAddress(Map params) {
        LogUtil.debug("ac_getAccountByAddress start");
        Account account;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //根据地址查询账户
            account = accountService.getAccount(chainId, address);
            if (null == account) {
                return success(null);
            }
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_getAccountByAddress end");
        return success(new SimpleAccountDto(account));
    }

    /**
     * 获取所有账户集合,并放入缓存
     * query all account collections and put them in cache
     *
     * @param params []
     * @return
     */
    @CmdAnnotation(cmd = "ac_getAccountList", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "query all account collections and put them in cache")
    public Response getAccountList(Map params) {
        LogUtil.debug("ac_getAccountList start");
        Map<String, List<SimpleAccountDto>> map = new HashMap<>();
        List<SimpleAccountDto> simpleAccountList = new ArrayList<>();
        try {
            //query all accounts
            List<Account> accountList = accountService.getAccountList();
            if (null == accountList) {
                return success(null);
            }
            accountList.forEach(account -> simpleAccountList.add(new SimpleAccountDto((account))));
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_getAccountList end");
        return success(simpleAccountList);
    }

    /**
     * 获取本地未加密账户列表
     * Get a list of local unencrypted accounts
     *
     * @param params []
     * @return
     */
    @CmdAnnotation(cmd = "ac_getUnencryptedAddressList", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "query all account collections and put them in cache")
    public Response getUnencryptedAddressList(Map params) {
        LogUtil.debug("getUnencryptedAddressList start");
        List<String> unencryptedAddressList = new ArrayList<>();
        try {
            //query all accounts
            List<Account> accountList = accountService.getAccountList();
            if (null == accountList) {
                return success(null);
            }
            for (Account account : accountList) {
                if (!account.isEncrypted()) {
                    unencryptedAddressList.add(account.getAddress().getBase58());
                }
            }
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("getUnencryptedAddressList end");
        return success(unencryptedAddressList);
    }

    /**
     * 分页查询账户地址列表
     * paging query account address list
     *
     * @param params [chainId,pageNumber,pageSize]
     * @return
     */
    @CmdAnnotation(cmd = "ac_getAddressList", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "paging query account address list")
    public Response getAddressList(Map params) {
        LogUtil.debug("ac_getAddressList start");
        Page<String> resultPage;
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
            int chainId = (int) chainIdObj;
            //页码
            Integer pageNumber = (Integer) pageNumberObj;
            //每页显示数量
            Integer pageSize = (Integer) pageSizeObj;

            if (chainId <= 0 || pageNumber < 1 || pageSize < 1) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }

            //query all accounts
            List<Account> accountList = accountService.getAccountList();
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
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_getAddressList end");
        return success(resultPage);
    }

    /**
     * 移除指定账户
     * remove specified account
     *
     * @param params [chainId,address,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_removeAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "remove specified account")
    public Response removeAccount(Map params) {
        LogUtil.debug("ac_removeAccount start");
        boolean result;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;

            //Remove specified account
            result = accountService.removeAccount(chainId, address, password);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_removeAccount end");
        Map<String, Boolean> map = new HashMap<>();
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
    @CmdAnnotation(cmd = "ac_getPriKeyByAddress", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "inquire the account's private key according to the address")
    public Response getPriKeyByAddress(Map params) {
        LogUtil.debug("ac_getPriKeyByAddress start");
        String unencryptedPrivateKey;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;

            //Get the account private key
            unencryptedPrivateKey = accountService.getPrivateKey(chainId, address, password);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_getPriKeyByAddress end");
        Map<String, Object> map = new HashMap<>();
        map.put("priKey", unencryptedPrivateKey);
        //账户是否存在
        map.put("valid", true);
        return success(map);
    }

    /**
     * 获取所有本地账户账户私钥，必须保证所有账户密码一致
     * 如果本地账户中的密码不一致，将返回错误信息
     * get the all local private keys
     * if the password in the local account is different, the error message will be returned.
     *
     * @param params [chainId,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_getAllPriKey", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "get the all local private keys")
    public Response getAllPriKey(Map params) {
        LogUtil.debug("ac_getAllPriKey start");
        Map<String, List<String>> map = new HashMap<>();
        List<String> privateKeyList = new ArrayList<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户密码
            String password = (String) passwordObj;

            //Get the account private key
            privateKeyList = accountService.getAllPrivateKey(chainId, password);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_getAllPriKey end");
        return success(privateKeyList);
    }

    /**
     * 为账户设置备注
     * set remark for accounts
     *
     * @param params [chainId,address,remark]
     * @return
     */
    @CmdAnnotation(cmd = "ac_setRemark", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "set remark for accounts")
    @Parameter(parameterName = "chainId", parameterType = "short", parameterValidRange = "", parameterValidRegExp = "")
    @Parameter(parameterName = "address", parameterType = "String", parameterValidRange = "", parameterValidRegExp = "")
    @Parameter(parameterName = "remark", parameterType = "String", parameterValidRange = "", parameterValidRegExp = "")
    public Response setRemark(Map params) {
        LogUtil.debug("ac_setRemark start");
        Map<String, Boolean> map = new HashMap<>(1);
        boolean result;
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
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户备注
            String remark = (String) remarkObj;

            //Get the account private key
            result = accountService.setRemark(chainId, address, remark);
            map.put(RpcConstant.VALUE, result);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_setRemark end");
        return success(map);
    }

    /**
     * 根据私钥导入账户
     * import accounts by private key
     *
     * @param params [chainId,priKey,password,overwrite]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importAccountByPriKey", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "import accounts by private key")
    public Response importAccountByPriKey(Map params) {
        LogUtil.debug("ac_importAccountByPriKey start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object priKeyObj = params == null ? null : params.get(RpcParameterNameConstant.PRIKEY);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object overwriteObj = params == null ? null : params.get(RpcParameterNameConstant.OVERWRITE);
            if (params == null || chainIdObj == null || priKeyObj == null || overwriteObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户私钥
            String priKey = (String) priKeyObj;
            //账户密码
            String password = (String) passwordObj;
            //账户存在时是否覆盖
            Boolean overwrite = (Boolean) overwriteObj;
            //导入账户
            Account account = accountService.importAccountByPrikey(chainId, priKey, password, overwrite);
            map.put(RpcConstant.ADDRESS, account.getAddress().toString());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_importAccountByPriKey end");
        return success(map);
    }


    /**
     * 根据AccountKeyStore导入账户
     * import accounts by AccountKeyStore
     *
     * @param params [chainId,keyStore,password,overwrite]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importAccountByKeystore", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "import accounts by AccountKeyStore")
    public Response importAccountByKeystore(Map params) {
        LogUtil.debug("ac_importAccountByKeystore start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object keyStoreObj = params == null ? null : params.get(RpcParameterNameConstant.KEYSTORE);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object overwriteObj = params == null ? null : params.get(RpcParameterNameConstant.OVERWRITE);
            if (params == null || chainIdObj == null || keyStoreObj == null || overwriteObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //keyStore HEX编码
            String keyStore = (String) keyStoreObj;
            //账户密码
            String password = (String) passwordObj;
            //账户存在时是否覆盖
            Boolean overwrite = (Boolean) overwriteObj;

            AccountKeyStoreDto accountKeyStoreDto;
            try {
                accountKeyStoreDto = JSONUtils.json2pojo(new String(HexUtil.decode(keyStore)), AccountKeyStoreDto.class);
            } catch (IOException e) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNTKEYSTORE_FILE_DAMAGED);
            }

            //导入账户
            Account account = accountService.importAccountByKeyStore(accountKeyStoreDto.toAccountKeyStore(), chainId, password, overwrite);
            map.put("address", account.getAddress().toString());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_importAccountByKeystore end");
        return success(map);
    }

    /**
     * 账户备份，导出AccountKeyStore字符串
     * export account KeyStore
     *
     * @param params [chainId,address,password,path]
     * @return
     */
    @CmdAnnotation(cmd = "ac_exportAccountKeyStore", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "export account KeyStore")
    public Response exportAccountKeyStore(Map params) {
        LogUtil.debug("ac_exportAccountKeyStore start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object filePathObj = params == null ? null : params.get(RpcParameterNameConstant.FILE_PATH);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;
            //文件备份地址
            String filePath = (String) filePathObj;
            //backup account to keystore
            String backupFileName = keyStoreService.backupAccountToKeyStore(filePath, chainId, address, password);
            map.put(RpcConstant.PATH, backupFileName);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }

        LogUtil.debug("ac_exportAccountKeyStore end");
        return success(map);
    }

    /**
     * 设置账户密码
     * set account password
     *
     * @param params [chainId,address,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_setPassword", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "set account password")
    public Response setPassword(Map params) {
        LogUtil.debug("ac_setPassword start");
        Map<String, Boolean> map = new HashMap<>(1);
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
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;

            //set account password
            result = accountService.setPassword(chainId, address, password);
            map.put(RpcConstant.VALUE, result);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }

        LogUtil.debug("ac_setPassword end");
        return success(map);
    }

    /**
     * 设置离线账户密码
     * set offline account password
     *
     * @param params [chainId,address,priKey,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_setOfflineAccountPassword", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "set offline account password")
    public Response setOfflineAccountPassword(Map params) {
        LogUtil.debug("ac_setOfflineAccountPassword start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object priKeyObj = params == null ? null : params.get(RpcParameterNameConstant.PRIKEY);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            if (params == null || chainIdObj == null || addressObj == null || priKeyObj == null || passwordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户私钥
            String priKey = (String) priKeyObj;
            //账户密码
            String password = (String) passwordObj;

            //set account password
            String encryptedPriKey = accountService.setOfflineAccountPassword(chainId, address, priKey, password);
            map.put(RpcConstant.ENCRYPTED_PRIKEY, encryptedPriKey);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_setOfflineAccountPassword end");
        return success(map);
    }

    /**
     * 根据原密码修改账户密码
     * modify the account password by the original password
     *
     * @param params [chainId,address,password,newPassword]
     * @return
     */
    @CmdAnnotation(cmd = "ac_updatePassword", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "modify the account password by the original password")
    public Response updatePassword(Map params) {
        LogUtil.debug("ac_updatePassword start");
        Map<String, Boolean> map = new HashMap<>(1);
        boolean result;
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object newPasswordObj = params == null ? null : params.get(RpcParameterNameConstant.NEW_PASSWORD);
            if (params == null || chainIdObj == null || addressObj == null || passwordObj == null || newPasswordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户旧密码
            String password = (String) passwordObj;
            //账户新密码
            String newPassword = (String) newPasswordObj;

            //change account password
            result = accountService.changePassword(chainId, address, password, newPassword);
            map.put(RpcConstant.VALUE, result);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_updatePassword end");
        return success(map);
    }

    /**
     * [离线账户修改密码] 根据原密码修改账户密码
     * offline account change password
     *
     * @param params [chainId,address,priKey,password,newPassword]
     * @return
     */
    @CmdAnnotation(cmd = "ac_updateOfflineAccountPassword", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "offline account change password")
    public Response updateOfflineAccountPassword(Map params) {
        LogUtil.debug("ac_updateOfflineAccountPassword start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object priKeyObj = params == null ? null : params.get(RpcParameterNameConstant.PRIKEY);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object newPasswordObj = params == null ? null : params.get(RpcParameterNameConstant.NEW_PASSWORD);
            if (params == null || chainIdObj == null || addressObj == null || priKeyObj == null || passwordObj == null || newPasswordObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户私钥
            String priKey = (String) priKeyObj;
            //账户旧密码
            String password = (String) passwordObj;
            //账户新密码
            String newPassword = (String) newPasswordObj;

            //set account password
            String encryptedPriKey = accountService.changeOfflinePassword(chainId, address, priKey, password, newPassword);
            map.put(RpcConstant.ENCRYPTED_PRIKEY, encryptedPriKey);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_updateOfflineAccountPassword end");
        return success(map);
    }

    /**
     * 根据账户地址获取账户是否加密
     * whether the account is encrypted by the account address
     *
     * @param params [chainId,address]
     * @return
     */
    @CmdAnnotation(cmd = "ac_isEncrypted", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "whether the account is encrypted by the account address")
    public Response isEncrypted(Map params) {
        LogUtil.debug("ac_isEncrypted start");
        Map<String, Boolean> map = new HashMap<>(1);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户是否加密
            boolean result = accountService.isEncrypted(chainId, address);
            map.put(RpcConstant.VALUE, result);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_isEncrypted end");
        return success(map);
    }

    /**
     * 验证账户密码是否正确
     * verify that the account password is correct
     *
     * @param params [chainId,address,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_validationPassword", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "verify that the account password is correct")
    public Response validationPassword(Map params) {
        LogUtil.debug("ac_validationPassword start");
        Map<String, Boolean> map = new HashMap<>(1);
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
            int chainId = (int) chainIdObj;
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
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_validationPassword end");
        return success(map);
    }

    /**
     * 数据摘要签名
     * data digest signature
     *
     * @param params [chainId,address,password,digestHex]
     * @return
     */
    @CmdAnnotation(cmd = "ac_signDigest", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "data digest signature")
    public Response signDigest(Map params) {
        LogUtil.debug("ac_signDigest start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object dataHexObj = params == null ? null : params.get(RpcParameterNameConstant.DATA_HEX);
            if (params == null || chainIdObj == null || addressObj == null || dataHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;
            //待签名的数据
            String dataHex = (String) dataHexObj;
            //数据解码为字节数组
            byte[] data = HexUtil.decode(dataHex);
            //sign digest data
            P2PHKSignature signature = accountService.signDigest(data, chainId, address, password);
            if (null == signature || signature.getSignData() == null) {
                throw new NulsRuntimeException(AccountErrorCode.SIGNATURE_ERROR);
            }
            try {
                map.put(RpcConstant.SIGNATURE_HEX, HexUtil.encode(signature.serialize()));
            } catch (IOException e) {
                throw new NulsRuntimeException(AccountErrorCode.SERIALIZE_ERROR);
            }
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_signDigest end");
        return success(map);
    }

    /**
     * 区块数据摘要签名
     * block data digest signature
     *
     * @param params [chainId,address,password,digestHex]
     * @return
     */
    @CmdAnnotation(cmd = "ac_signBlockDigest", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "block data digest signature")
    public Response signBlockDigest(Map params) {
        LogUtil.debug("ac_signDigest start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
            Object dataHexObj = params == null ? null : params.get(RpcParameterNameConstant.DATA_HEX);
            if (params == null || chainIdObj == null || addressObj == null || dataHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            //链ID
            int chainId = (int) chainIdObj;
            //账户地址
            String address = (String) addressObj;
            //账户密码
            String password = (String) passwordObj;
            //待签名的数据
            String dataHex = (String) dataHexObj;
            //数据解码为字节数组
            byte[] data = HexUtil.decode(dataHex);
            //sign digest data
            BlockSignature signature = accountService.signBlockDigest(data, chainId, address, password);
            if (null == signature || signature.getSignData() == null) {
                throw new NulsRuntimeException(AccountErrorCode.SIGNATURE_ERROR);
            }
            try {
                map.put(RpcConstant.SIGNATURE_HEX, HexUtil.encode(signature.serialize()));
            } catch (IOException e) {
                throw new NulsRuntimeException(AccountErrorCode.SERIALIZE_ERROR);
            }
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_signDigest end");
        return success(map);
    }

    /**
     * 创建多账户转账交易
     * create a multi-account transfer transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_multipleAddressTransfer", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "create a multi-account transfer transaction")
    public Response multipleAddressTransfer(Map params) {
        LogUtil.debug("ac_multipleAddressTransfer start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            if (params == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MulitpleAddressTransferDto transferDto = JSONUtils.json2pojo(JSONUtils.obj2json(params), MulitpleAddressTransferDto.class);
            List<CoinDto> inputList = transferDto.getInputs();
            List<CoinDto> outputList = transferDto.getOutputs();
            if (inputList == null || outputList == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // check address validity
            BigInteger fromTotal = BigInteger.ZERO;
            for (CoinDto from : inputList) {
                if (!AddressTool.validAddress(from.getAssetsChainId(), from.getAddress())) {
                    throw new NulsException(AccountErrorCode.ADDRESS_ERROR);
                }
                fromTotal=fromTotal.add(from.getAmount());
            }
            BigInteger toTotal = BigInteger.ZERO;
            for (CoinDto to : outputList) {
                if (!AddressTool.validAddress(to.getAssetsChainId(), to.getAddress())) {
                    throw new NulsException(AccountErrorCode.ADDRESS_ERROR);
                }
                toTotal=toTotal.add(to.getAmount());
            }

            // check transfer amount
            if (BigIntegerUtils.isLessThan(fromTotal, BigInteger.ZERO) || BigIntegerUtils.isLessThan(toTotal, BigInteger.ZERO)) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            // check transaction remark
            if (!validTxRemark(transferDto.getRemark())) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            String txDigestHex = transactionService.multipleAddressTransfer(transferDto.getChainId(), inputList, outputList, transferDto.getRemark());
            map.put("value", txDigestHex);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (IOException e) {
            return failed(e.getMessage());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
        LogUtil.debug("ac_multipleAddressTransfer end");
        return success(map);
    }

    /**
     * 校验转账交易备注是否有效
     *
     * @param remark
     * @return
     */
    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(NulsConfig.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
}
