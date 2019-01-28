package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.dto.AccountKeyStoreDto;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Page;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class AccountCmdTest {

    //protected static AccountService accountService;

    protected int chainId = 12345;
    protected String password = "nuls123456";
    protected String newPassword = "c12345678";
    protected String version = "1.0";
    protected String success = "1";

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    private List<String> createAccount(int chainId, int count, String password) {
        List<String> accountList = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("count", count);
            params.put("password", password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            if (!AccountConstant.SUCCESS_CODE.equals(cmdResp.getResponseStatus())) {
                return null;
            }
            accountList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount")).get("list");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountList;
    }

    public SimpleAccountDto getAccountByAddress(int chainId, String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("address", address);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountByAddress", params);

        SimpleAccountDto accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("ac_getAccountByAddress")), SimpleAccountDto.class);
        return accountDto;
    }

    /**
     * 根据地址查询私钥
     *
     * @param chainId
     * @param address
     * @return
     * @throws Exception
     */
    public String getPriKeyByAddress(int chainId, String address, String password) {
        String priKey = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
            priKey = (String) result.get("priKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return priKey;
    }

    @Test
    public void createAccountTest() throws Exception {
        int count = 3;
        //test to create an account that is not empty.
        List<String> accountList = createAccount(chainId, count, password);
        //checking the number of accounts returned
        assertEquals(accountList.size(), count);
        for (String address : accountList) {
            System.out.println(address);
        }

        //Test to create an empty password account
        accountList = createAccount(chainId, count, null);
        //Checking the number of accounts returned
        assertEquals(accountList.size(), count);

        //Test the largest number of generated accounts.
        accountList = createAccount(chainId, 101, null);
        assertNull(accountList);


    }

    @Test
    public void createOfflineAccountTest() throws Exception {
        int count = 10;
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("count", count);
        params.put("password", password);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createOfflineAccount", params);
        //List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(cmdResp.getResponseData()), AccountOfflineDto.class);
        List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createOfflineAccount")).get("list")), AccountOfflineDto.class);
        assertEquals(accountList.size(), count);
        for (AccountOfflineDto account : accountList) {
            System.out.println(account.getAddress());
        }
    }

    @Test
    public void removeAccountTest() throws Exception {
        List<String> accountList = createAccount(chainId, 2, password);

        SimpleAccountDto account = getAccountByAddress(chainId, accountList.get(0));
        assertNotNull(account);

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("address", accountList.get(0));
        params.put("password", password);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params);
        assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        account = getAccountByAddress(chainId, accountList.get(0));
        assertNull(account);
    }

    @Test
    public void getAccountByAddressTest() throws Exception {
        List<String> accountList = createAccount(chainId, 1, password);
        SimpleAccountDto accountDto = getAccountByAddress(chainId, accountList.get(0));
        assertEquals(accountList.get(0), accountDto.getAddress());
    }

    @Test
    public void getAccountListTest() throws Exception {
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountList", null);
        //List<SimpleAccountDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(cmdResp.getResponseData()), SimpleAccountDto.class);
        List<SimpleAccountDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAccountList")).get("list")), SimpleAccountDto.class);
        accountList.forEach(account -> System.out.println(account.getAddress()));
    }

    @Test
    public void getAddressListTest() throws Exception {
        List<String> accountList = createAccount(chainId, 1, password);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put("chainId", chainId);
        params.put("pageNumber", 1);
        params.put("pageSize", 10);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAddressList", params);
        Page<String> resultPage = JSONUtils.json2pojo(JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("ac_getAddressList")), Page.class);
        assertTrue(resultPage.getTotal() > 0);
        resultPage.getList().forEach(System.out::println);

        //test paging parameter pageNumber error
        params.put("pageNumber", 0);
        cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAddressList", params);
        assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        //Test paging parameter pageSize error
        params.put("pageNumber", 1);
        params.put("pageSize", -1);
        cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAddressList", params);
        assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
    }

    @Test
    public void getAllPrivateKeyTest() {
        try {
            List<String> accountList = createAccount(1, 1, password);
            //query all accounts privateKey
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", 0);
            params.put("password", password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAllPriKey", params);
            if (AccountConstant.SUCCESS_CODE.equals(cmdResp.getResponseStatus())) {
                List<String> privateKeyAllList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAllPriKey")).get("list");
                //query all accounts privateKey the specified chain
                params.put("chainId", 1);
                cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getAllPriKey", params);
                List<String> privateKeyList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAllPriKey")).get("list");
                assertTrue(privateKeyList.size() >= accountList.size());
                assertTrue(privateKeyAllList.size() >= privateKeyList.size());
            }
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getPrivateKeyTest() {
        try {
            //create password accounts
            List<String> accountList = createAccount(chainId, 1, password);
            //query specified account private key
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", accountList.get(0));
            params.put("password", password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
            assertNotNull(result.get("priKey"));
            assertTrue((boolean) result.get("valid"));
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setRemarkTest() {
        try {
            String remark = "test remark";
            //Create encrypted account
            List<String> accountList = createAccount(chainId, 1, password);
            //Set the correct remarks for the account
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", accountList.get(0));
            params.put("remark", remark);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setRemark", params);
            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void importAccountByPriKeyTest() {
        try {
            //create encrypted account
            List<String> accountList = createAccount(chainId, 1, password);
            String priKey = getPriKeyByAddress(chainId, accountList.get(0), password);
            assertNotNull(priKey);
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("priKey", "5d3ee4ab8f9d5c03fb061ad14fe014c999a35c4a03d19a56d10cb4ad95d8463c");
            params.put("password", "nuls123456");
            params.put("overwrite", true);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
//            assertEquals(accountList.get(0), address);
            //账户已存在，不覆盖，返回错误提示  If the account exists, it will not be covered,return error message.
            params.put("overwrite", false);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
/*
            //移除账户，再导入 Remove the account and import it according to the private key.
            Map<String, Object> params2 = new HashMap<>();
            params2.put(Constants.VERSION_KEY_STR, version);
            params2.put("chainId", chainId);
            params2.put("address", accountList.get(0));
            params2.put("password", password);
            CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params2);
            //账户不存在则创建 If account does not exist, create
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //测试未加密账户
            //create an unencrypted account for test
            //由于getPriKeyByAddress只返回加密账户的私钥，所以无法得到未加密账户私钥，所以使用固定值测试
            String addressx = "XfbZd1RYgTtQBb7xeP3bziAd2kmQL3930";
            priKey = "00cf6b28b2885c550506006b72fab1ab85cbf7e1aafdc6c1661e2b82f7f0089185";
            //账户已存在则覆盖 If the account exists, it covers.
            params.remove("password");
            params.put("priKey", priKey);
            params.put("overwrite", true);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            address = (String) result.get("address");
            assertEquals(addressx, address);*/
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void importAccountByKeystoreTest() {
        try {
            //create encrypted account
            List<String> accountList = createAccount(chainId, 1, password);
            String priKey = getPriKeyByAddress(chainId, accountList.get(0), password);
            assertNotNull(priKey);

            //构造keystore对象
            SimpleAccountDto account = getAccountByAddress(chainId, accountList.get(0));
            AccountKeyStoreDto keyStoreDto = new AccountKeyStoreDto();
            keyStoreDto.setAddress(account.getAddress());
            keyStoreDto.setPubKey(account.getPubkeyHex());
            keyStoreDto.setEncryptedPrivateKey(account.getEncryptedPrikeyHex());
            //keyStoreDto.setPrikey(priKey);

            //生成keystore HEX编码
            String keyStoreHex = HexUtil.encode(JSONUtils.obj2json(keyStoreDto).getBytes());
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("keyStore", keyStoreHex);
            params.put("password", password);
            params.put("overwrite", true);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByKeystore");
            String address = (String) result.get("address");
            assertEquals(accountList.get(0), address);

            //账户已存在，不覆盖，返回错误提示  If the account exists, it will not be covered,return error message.
            params.put("overwrite", false);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //移除账户，再导入 Remove the account and import it according to the private key.
            Map<String, Object> params2 = new HashMap<>();
            params2.put(Constants.VERSION_KEY_STR, version);
            params2.put("chainId", chainId);
            params2.put("address", accountList.get(0));
            params2.put("password", password);
            CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params2);
            //账户不存在则创建 If account does not exist, create
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //测试未加密账户
            //Create an unencrypted account for test
            //由于getPriKeyByAddress只返回加密账户的私钥，所以无法得到未加密账户私钥，所以使用固定值测试
            String addressx = "XfbZd1RYgTtQBb7xeP3bziAd2kmQL3930";
            priKey = "00cf6b28b2885c550506006b72fab1ab85cbf7e1aafdc6c1661e2b82f7f0089185";

            //构造keystore对象
            account = getAccountByAddress(chainId, addressx);
            keyStoreDto = new AccountKeyStoreDto();
            keyStoreDto.setAddress(account.getAddress());
            keyStoreDto.setPubKey(account.getPubkeyHex());
            //keyStoreDto.setEncryptedPrivateKey(account.getEncryptedPrikeyHex());
            keyStoreDto.setPrikey(priKey);

            //生成keystore HEX编码
            keyStoreHex = HexUtil.encode(JSONUtils.obj2json(keyStoreDto).getBytes());
            //账户已存在则覆盖 If the account exists, it covers.
            params.put("keyStore", keyStoreHex);
            params.remove("password");
            params.put("overwrite", true);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByKeystore");
            address = (String) result.get("address");
            assertEquals(addressx, address);
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void exportAccountKeyStoreTest() {
        try {
            //create encrypted account
            List<String> accountList = createAccount(chainId, 1, password);
            String address = accountList.get(0);

            //测试不指定备份路径
            String pathDir = "";
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            params.put("pathDir", pathDir);
            //导出账户keystore路径  export account keyStore path
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_exportAccountKeyStore", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_exportAccountKeyStore");
            String path = (String) result.get("path");
            assertNotNull(path);

            //测试指定非windows备份路径
            pathDir = "测试1/back/up";
            params.put("pathDir", pathDir);
            //导出账户keystore路径  export account keyStore path
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_exportAccountKeyStore", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_exportAccountKeyStore");
            path = (String) result.get("path");
            assertNotNull(path);

            //测试指定windows备份路径
            //Create an unencrypted account for test
            accountList = createAccount(chainId, 1, null);
            pathDir = "D:\\workspace\\github\\nuls_2.0\\测试2\\back\\up";
            params.put("address", accountList.get(0));
            params.remove("password");
            params.put("pathDir", pathDir);
            //导出账户keystore路径  export account keyStore path
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_exportAccountKeyStore", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_exportAccountKeyStore");
            path = (String) result.get("path");
            assertNotNull(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setPasswordTest() {
        try {
            //创建未加密账户 create unencrypted account
            List<String> accountList = createAccount(chainId, 1, null);
            String address = accountList.get(0);

            //为账户设置密码 set password for account
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setPassword");
            Boolean value = (Boolean) result.get("value");
            assertTrue(value);

            //为账户重复设置密码 Repeat password for account
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            //不能再次设置密码 Password cannot be set again.
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updatePasswordTest() {
        try {
            //创建未加密账户 create unencrypted account
            List<String> accountList = createAccount(chainId, 1, null);
            String address = accountList.get(0);

            //为未设置密码的账户修改密码 change password for account
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            params.put("newPassword", newPassword);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_updatePassword", params);
            //未设置密码不能修改密码，必须先设置密码再修改 No password can be changed, password must be set first, then password should be changed.
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //为账户设置密码 set password for account
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            Map result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setPassword");
            Boolean value = (Boolean) result.get("value");
            assertTrue(value);

            //为账户修改密码 change password for account
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_updatePassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_updatePassword");
            value = (Boolean) result.get("value");
            assertTrue(value);

            //使用错误旧密码为账户修改密码 using old password to change password for account
            params.put("password", "errorpwd123");
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_updatePassword", params);
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setOfflineAccountPasswordTest() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("count", 1);
            //创建未加密离线账户 create unencrypted account
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createOfflineAccount", params);
            //List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResponseData())).get("list")), AccountOfflineDto.class);
            List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createOfflineAccount")).get("list")), AccountOfflineDto.class);
            String address = accountList.get(0).getAddress();
            String priKey = accountList.get(0).getPriKey();

            params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("priKey", priKey);
            params.put("password", password);
            //为账户设置密码 set password for account
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setOfflineAccountPassword", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setOfflineAccountPassword");
            String encryptedPriKey = (String) result.get("encryptedPriKey");
            assertNotNull(encryptedPriKey);

            params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("encryptedPriKey", encryptedPriKey);
            params.put("newPassword", newPassword);
            //为离线账户重复设置密码 repeat password for account
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setOfflineAccountPassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setOfflineAccountPassword");
            String encryptedPriKeyNew = (String) result.get("encryptedPriKey");
            //可以为离线账户再次设置密码 Password cannot be set again.
            assertNotEquals(encryptedPriKeyNew, encryptedPriKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateOfflineAccountPasswordTest() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("count", 1);
            //创建未加密离线账户 create unencrypted account
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createOfflineAccount", params);
            List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createOfflineAccount")).get("list")), AccountOfflineDto.class);
            String address = accountList.get(0).getAddress();
            String priKey = accountList.get(0).getPriKey();

            //创建加密离线账户 create encrypted account
            params.put("password", password);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_createOfflineAccount", params);
            accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createOfflineAccount")).get("list")), AccountOfflineDto.class);
            String encryptedPriKey2 = accountList.get(0).getEncryptedPriKey();

            //为账户设置密码 set password for account
            params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("priKey", priKey);
            params.put("password", password);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setOfflineAccountPassword", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setOfflineAccountPassword");
            String encryptedPriKey = (String) result.get("encryptedPriKey");
            assertNotNull(encryptedPriKey);

            //测试错误的地址 testing the wrong address
            params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("priKey", encryptedPriKey2);
            params.put("password", password);
            params.put("newPassword", newPassword);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_updateOfflineAccountPassword", params);
            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_updateOfflineAccountPassword"));
            assertEquals(AccountErrorCode.ADDRESS_ERROR.getCode(), result.get("code"));

            //测试错误的私钥 testing the wrong private key
            params.put("address", address);
            params.put("priKey", "86" + encryptedPriKey);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_updateOfflineAccountPassword", params);
            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_updateOfflineAccountPassword"));
            assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), result.get("code"));

            //测试错误的密码 testing the wrong password
            params.put("priKey", encryptedPriKey);
            params.put("password", password + "errorpwd");
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_updateOfflineAccountPassword", params);
            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_updateOfflineAccountPassword"));
            assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), result.get("code"));

            //为离线账户修改密码 modify password for offline account
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("priKey", encryptedPriKey);
            params.put("password", password);
            params.put("newPassword", newPassword);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_updateOfflineAccountPassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_updateOfflineAccountPassword");
            String encryptedPriKeyNew = (String) result.get("encryptedPriKey");
            assertNotEquals(encryptedPriKeyNew, encryptedPriKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isEncryptedTest() {
        try {
            // create account
            //创建未加密账户 create unencrypted account
            List<String> accountList = createAccount(chainId, 1, null);
            String address = accountList.get(0);

            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            //验证账户是否加密 verify that the account is encrypted
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_isEncrypted", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_isEncrypted");
            Boolean value = (Boolean) result.get("value");
            assertFalse(value);

            //为账户设置密码 set password for account
            params.put("password", password);
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setPassword");
            value = (Boolean) result.get("value");
            assertTrue(value);

            //验证账户是否加密 verify that the account is encrypted
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_isEncrypted", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_isEncrypted");
            value = (Boolean) result.get("value");
            assertTrue(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void validationPasswordTest() {
        try {
            //创建未加密账户 create unencrypted account
            List<String> accountList = createAccount(chainId, 1, null);
            String address = accountList.get(0);

            //验证账户是否正确 verify that the account password is correct
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_validationPassword", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_validationPassword");
            Boolean value = (Boolean) result.get("value");
            assertFalse(value);

            //为账户设置密码 set password for account
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setPassword");
            value = (Boolean) result.get("value");
            assertTrue(value);

            //验证账户是否正确 verify that the account password is correct
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_validationPassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_validationPassword");
            value = (Boolean) result.get("value");
            assertTrue(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void signDigestTest() {
        try {
            //创建加密账户 create encrypted account
            List<String> accountList = createAccount(chainId, 1, password);
            String address = accountList.get(0);
            //byte[] addressBytes = accountList.get(0).getAddress().getAddressBytes();
            byte[] addressBytes = accountList.get(0).getBytes();

            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(TimeService.currentTimeMillis());
            Alias alias = new Alias(addressBytes, "别名");
            tx.setTxData(alias.serialize());

//            CoinDataResult coinDataResult = accountLedgerService.getCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size() , TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
//            if (!coinDataResult.isEnough()) {
//                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
//            }
            CoinData coinData = new CoinData();
            //coinData.setFrom(coinDataResult.getCoinList());
            CoinTo coin = new CoinTo();
            coin.setAddress(AddressTool.getAddress("Nse5FeeiYk1opxdc5RqYpEWkiUDGNuLs" + HexUtil.encode(ByteUtils.shortToBytes((short) chainId))));
            coin.setAmount(new BigInteger("1"));
            coin.setAssetsChainId(chainId);
            coin.setAssetsId(1);
            coinData.addTo(coin);

            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));

            String dataHex = HexUtil.encode(tx.getHash().getDigestBytes());
            //测试密码正确
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            params.put("dataHex", dataHex);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", params);
            HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_signDigest"));
            String signatureHex = (String) result.get(RpcConstant.SIGNATURE_HEX);
            assertNotNull(signatureHex);

            //测试密码不正确
            params.put("password", password + "error");
            cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", params);
            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_signDigest"));
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
            assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), result.get("code"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
