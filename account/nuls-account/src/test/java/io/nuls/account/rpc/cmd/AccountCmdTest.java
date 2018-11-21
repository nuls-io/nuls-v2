package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.dto.AccountKeyStoreDto;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.util.AccountTool;
import io.nuls.base.data.Page;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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

    protected short chainId = 12345;
    protected String password = "a12345678";
    protected String passwordNew = "c12345678";
    protected double version2 = 1.0;
    protected String version = "1.0";

    @BeforeClass
    public static void start() throws Exception {
        //CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
        RuntimeInfo.kernelUrl = "ws://127.0.0.1:8887";
        CmdDispatcher.syncKernel();
    }

    private void request() {
        try {
            // Build params map
            Map<String, Object> params = new HashMap<>();
            // Version information ("1.1" or 1.1 is both available)
            params.put(Constants.VERSION_KEY_STR, "1.0");
            // Call cmd
            System.out.println(CmdDispatcher.request("getHeight", params));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> createAccount(short chainId, int count, String password) {
        List<String> accountList = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("count", count);
            params.put("password", password);
            String response = CmdDispatcher.request("ac_createAccount", params);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            if (!AccountConstant.SUCCESS_CODE.equals(cmdResp.getCode()) || (count <= 0 || count > AccountTool.CREATE_MAX_SIZE)) {
//                return null;
//            }
//            accountList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
            Message cmdResp = JSONUtils.json2pojo(response, Message.class);
            accountList = (List<String>)JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getMessageData()), Response.class).getResponseData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountList;
    }

    public SimpleAccountDto getAccountByAddress(short chainId, String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", address);
        String response = CmdDispatcher.request("ac_getAccountByAddress", params);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        SimpleAccountDto accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), SimpleAccountDto.class);
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
    public String getPriKeyByAddress(short chainId, String address, String password) {
        String priKey = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            String response = CmdDispatcher.request("ac_getPriKeyByAddress", params);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            HashMap result = (HashMap) cmdResp.getResult();
            priKey = (String) result.get("priKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return priKey;
    }

    @Test
    public void createAccountTest() throws Exception {
        int count = 1;
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
        int count = 1;
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("count", count);
        params.put("password", password);
        String response = CmdDispatcher.request("ac_createOfflineAccount", params);
        Message cmdResp = JSONUtils.json2pojo(response, Message.class);
        List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getMessageData()), Response.class).getResponseData()), AccountOfflineDto.class);
        //List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), AccountOfflineDto.class);
        assertEquals(accountList.size(), count);
        for (AccountOfflineDto account : accountList) {
            System.out.println(account.getAddress());
        }
    }

//    @Test
//    public void removeAccountTest() throws Exception {
//        List<String> accountList = createAccount(chainId, 2, password);
//
//        SimpleAccountDto account = getAccountByAddress(chainId, accountList.get(0));
//        assertNotNull(account);
//
//        String response = CmdDispatcher.call("ac_removeAccount", new Object[]{chainId, accountList.get(0), password}, version);
//        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//        assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//
//        account = getAccountByAddress(chainId, accountList.get(0));
//        assertNull(account);
//    }
//
//    @Test
//    public void getAccountByAddressTest() throws Exception {
//        List<String> accountList = createAccount(chainId, 1, password);
//        String response = CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, accountList.get(0)}, version);
//        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//        SimpleAccountDto accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), SimpleAccountDto.class);
//        assertEquals(accountList.get(0), accountDto.getAddress());
//    }
//
//    @Test
//    public void getAccountListTest() throws Exception {
//        String response = CmdDispatcher.call("ac_getAccountList", new Object[]{}, version);
//        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//        List<SimpleAccountDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), SimpleAccountDto.class);
//        accountList.forEach(account -> System.out.println(account.getAddress()));
//    }
//
//    @Test
//    public void getAddressListTest() throws Exception {
//        List<String> accountList = createAccount(chainId, 1, password);
//        String response = CmdDispatcher.call("ac_getAddressList", new Object[]{chainId, 1, 10}, version);
//        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//        Page<String> resultPage = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), Page.class);
//        assertTrue(resultPage.getTotal() > 0);
//        resultPage.getList().forEach(System.out::println);
//
//        //test paging parameter pageNumber error
//        response = CmdDispatcher.call("ac_getAddressList", new Object[]{chainId, 0, 10}, version);
//        cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//        assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//        //Test paging parameter pageSize error
//        response = CmdDispatcher.call("ac_getAddressList", new Object[]{chainId, 1, -1}, version);
//        cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//        assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//    }
//
//    @Test
//    public void getAllPrivateKeyTest() {
//        try {
//            List<String> accountList = createAccount((short) 1, 1, password);
//            //query all accounts privateKey
//            String response = CmdDispatcher.call("ac_getAllPriKey", new Object[]{(short) 0, password}, version);
//            System.out.println(response);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            List<String> privateKeyAllList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
//            //query all accounts privateKey the specified chain
//            response = CmdDispatcher.call("ac_getAllPriKey", new Object[]{(short) 1, password}, version);
//            List<String> privateKeyList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
//            assertTrue(privateKeyList.size() >= accountList.size());
//            assertTrue(privateKeyAllList.size() >= privateKeyList.size());
//        } catch (NulsRuntimeException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void getPrivateKeyTest() {
//        try {
//            //create password accounts
//            List<String> accountList = createAccount(chainId, 1, password);
//            //query specified account private key
//            String response = CmdDispatcher.call("ac_getPriKeyByAddress", new Object[]{chainId, accountList.get(0), password}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            assertNotNull(result.get("priKey"));
//        } catch (NulsRuntimeException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void setRemarkTest() {
//        try {
//            String remark = "test remark";
//            //Create encrypted account
//            List<String> accountList = createAccount(chainId, 1, password);
//            //Set the correct remarks for the account
//            String response = CmdDispatcher.call("ac_setRemark", new Object[]{chainId, accountList.get(0), remark}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//        } catch (NulsRuntimeException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void importAccountByPriKeyTest() {
//        try {
//            //create encrypted account
//            List<String> accountList = createAccount(chainId, 1, password);
//            String priKey = getPriKeyByAddress(chainId, accountList.get(0), password);
//            assertNotNull(priKey);
//            //账户已存在则覆盖 If the account exists, it covers.
//            String response = CmdDispatcher.call("ac_importAccountByPriKey", new Object[]{chainId, priKey, password, true}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            String address = (String) result.get("address");
//            assertEquals(accountList.get(0), address);
//            //账户已存在，不覆盖，返回错误提示  If the account exists, it will not be covered,return error message.
//            response = CmdDispatcher.call("ac_importAccountByPriKey", new Object[]{chainId, priKey, password, false}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//
//            //移除账户，再导入 Remove the account and import it according to the private key.
//            CmdDispatcher.call("ac_removeAccount", new Object[]{chainId, accountList.get(0), password}, version);
//            //账户不存在则创建 If account does not exist, create
//            response = CmdDispatcher.call("ac_importAccountByPriKey", new Object[]{chainId, priKey, password, false}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//
//            //测试未加密账户
//            //create an unencrypted account for test
//            //由于getPriKeyByAddress只返回加密账户的私钥，所以无法得到未加密账户私钥，所以使用固定值测试
//            String addressx = "XfbZd1RYgTtQBb7xeP3bziAd2kmQL3930";
//            priKey = "00cf6b28b2885c550506006b72fab1ab85cbf7e1aafdc6c1661e2b82f7f0089185";
//            //账户已存在则覆盖 If the account exists, it covers.
//            response = CmdDispatcher.call("ac_importAccountByPriKey", new Object[]{chainId, priKey, null, true}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            address = (String) result.get("address");
//            assertEquals(addressx, address);
//
//        } catch (NulsRuntimeException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void importAccountByKeystoreTest() {
//        try {
//            //create encrypted account
//            List<String> accountList = createAccount(chainId, 1, password);
//            String priKey = getPriKeyByAddress(chainId, accountList.get(0), password);
//            assertNotNull(priKey);
//
//            //构造keystore对象
//            SimpleAccountDto account = getAccountByAddress(chainId, accountList.get(0));
//            AccountKeyStoreDto keyStoreDto = new AccountKeyStoreDto();
//            keyStoreDto.setAddress(account.getAddress());
//            keyStoreDto.setPubKey(account.getPubkeyHex());
//            keyStoreDto.setEncryptedPrivateKey(account.getEncryptedPrikeyHex());
//            //keyStoreDto.setPrikey(priKey);
//
//            //生成keystore HEX编码
//            String keyStoreHex = HexUtil.encode(JSONUtils.obj2json(keyStoreDto).getBytes());
//            //账户已存在则覆盖 If the account exists, it covers.
//            String response = CmdDispatcher.call("ac_importAccountByKeystore", new Object[]{chainId, keyStoreHex, password, true}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            String address = (String) result.get("address");
//            assertEquals(accountList.get(0), address);
//
//            //账户已存在，不覆盖，返回错误提示  If the account exists, it will not be covered,return error message.
//            response = CmdDispatcher.call("ac_importAccountByKeystore", new Object[]{chainId, keyStoreHex, password, false}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//
//            //移除账户，再导入 Remove the account and import it according to the private key.
//            response = CmdDispatcher.call("ac_removeAccount", new Object[]{chainId, accountList.get(0), password}, version);
//            //账户不存在则创建 If account does not exist, create
//            response = CmdDispatcher.call("ac_importAccountByKeystore", new Object[]{chainId, keyStoreHex, password, false}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//
//            //测试未加密账户
//            //Create an unencrypted account for test
//            //accountList = createAccount(chainId, 1, null);
//            //由于getPriKeyByAddress只返回加密账户的私钥，所以无法得到未加密账户私钥，所以使用固定值测试
//            String addressx = "XfbZd1RYgTtQBb7xeP3bziAd2kmQL3930";
//            priKey = "00cf6b28b2885c550506006b72fab1ab85cbf7e1aafdc6c1661e2b82f7f0089185";
//
//            //构造keystore对象
//            account = getAccountByAddress(chainId, addressx);
//            keyStoreDto = new AccountKeyStoreDto();
//            keyStoreDto.setAddress(account.getAddress());
//            keyStoreDto.setPubKey(account.getPubkeyHex());
//            //keyStoreDto.setEncryptedPrivateKey(account.getEncryptedPrikeyHex());
//            keyStoreDto.setPrikey(priKey);
//
//            //生成keystore HEX编码
//            keyStoreHex = HexUtil.encode(JSONUtils.obj2json(keyStoreDto).getBytes());
//            //账户已存在则覆盖 If the account exists, it covers.
//            response = CmdDispatcher.call("ac_importAccountByKeystore", new Object[]{chainId, keyStoreHex, null, true}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            address = (String) result.get("address");
//            assertEquals(addressx, address);
//        } catch (NulsRuntimeException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void exportAccountKeyStoreTest() {
//        try {
//            //create encrypted account
//            List<String> accountList = createAccount(chainId, 1, password);
//            String address = accountList.get(0);
//
//            //测试不指定备份路径
//            String pathDir = "";
//            //导出账户keystore路径  export account keyStore path
//            String response = CmdDispatcher.call("ac_exportAccountKeyStore", new Object[]{chainId, address, password, pathDir}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            String path = (String) result.get("path");
//            assertNotNull(path);
//
//            //测试指定非windows备份路径
//            pathDir = "测试1/back/up";
//            //导出账户keystore路径  export account keyStore path
//            response = CmdDispatcher.call("ac_exportAccountKeyStore", new Object[]{chainId, address, password, pathDir}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            path = (String) result.get("path");
//            assertNotNull(path);
//
//            //测试指定windows备份路径
//            pathDir = "D:\\workspace\\github\\nuls_2.0\\测试2\\back\\up";
//            //Create an unencrypted account for test
//            accountList = createAccount(chainId, 1, null);
//            address = accountList.get(0);
//            //导出账户keystore路径  export account keyStore path
//            response = CmdDispatcher.call("ac_exportAccountKeyStore", new Object[]{chainId, address, null, pathDir}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            path = (String) result.get("path");
//            assertNotNull(path);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void setPasswordTest() {
//        try {
//            //创建未加密账户 create unencrypted account
//            List<String> accountList = createAccount(chainId, 1, null);
//            String address = accountList.get(0);
//
//            //为账户设置密码 set password for account
//            String response = CmdDispatcher.call("ac_setPassword", new Object[]{chainId, address, password}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            Boolean value = (Boolean) result.get("value");
//            assertTrue(value);
//
//            //为账户重复设置密码 Repeat password for account
//            response = CmdDispatcher.call("ac_setPassword", new Object[]{chainId, address, password}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            //不能再次设置密码 Password cannot be set again.
//            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void updatePasswordTest() {
//        try {
//            //创建未加密账户 create unencrypted account
//            List<String> accountList = createAccount(chainId, 1, null);
//            String address = accountList.get(0);
//
//            //为未设置密码的账户修改密码 change password for account
//            String response = CmdDispatcher.call("ac_updatePassword", new Object[]{chainId, address, password, passwordNew}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            //未设置密码不能修改密码，必须先设置密码再修改 No password can be changed, password must be set first, then password should be changed.
//            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//
//            //为账户设置密码 set password for account
//            response = CmdDispatcher.call("ac_setPassword", new Object[]{chainId, address, password}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            Boolean value = (Boolean) result.get("value");
//            assertTrue(value);
//
//            //为账户修改密码 change password for account
//            response = CmdDispatcher.call("ac_updatePassword", new Object[]{chainId, address, password, passwordNew}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            value = (Boolean) result.get("value");
//            assertTrue(value);
//
//            //使用错误旧密码为账户修改密码 using old password to change password for account
//            response = CmdDispatcher.call("ac_updatePassword", new Object[]{chainId, address, "errorpwd123", passwordNew}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void setOfflineAccountPasswordTest() {
//        try {
//            //创建未加密离线账户 create unencrypted account
//            String response = CmdDispatcher.call("ac_createOfflineAccount", new Object[]{chainId, 1, null}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), AccountOfflineDto.class);
//            String address = accountList.get(0).getAddress();
//            String priKey = accountList.get(0).getPriKey();
//
//            //为账户设置密码 set password for account
//            response = CmdDispatcher.call("ac_setOfflineAccountPassword", new Object[]{chainId, address, priKey, password}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            String encryptedPriKey = (String) result.get("encryptedPriKey");
//            assertNotNull(encryptedPriKey);
//
//            //为离线账户重复设置密码 repeat password for account
//            response = CmdDispatcher.call("ac_setOfflineAccountPassword", new Object[]{chainId, address, encryptedPriKey, passwordNew}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            String encryptedPriKeyNew = (String) result.get("encryptedPriKey");
//            //可以为离线账户再次设置密码 Password cannot be set again.
//            assertNotEquals(encryptedPriKeyNew, encryptedPriKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void updateOfflineAccountPasswordTest() {
//        try {
//            //创建未加密离线账户 create unencrypted account
//            String response = CmdDispatcher.call("ac_createOfflineAccount", new Object[]{chainId, 1, null}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), AccountOfflineDto.class);
//            String address = accountList.get(0).getAddress();
//            String priKey = accountList.get(0).getPriKey();
//
//            //创建加密离线账户 create encrypted account
//            response = CmdDispatcher.call("ac_createOfflineAccount", new Object[]{chainId, 1, password}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), AccountOfflineDto.class);
//            String encryptedPriKey2 = accountList.get(0).getEncryptedPriKey();
//
//            //为账户设置密码 set password for account
//            response = CmdDispatcher.call("ac_setOfflineAccountPassword", new Object[]{chainId, address, priKey, password}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            String encryptedPriKey = (String) result.get("encryptedPriKey");
//            assertNotNull(encryptedPriKey);
//
//            //测试错误的地址 testing the wrong address
//            response = CmdDispatcher.call("ac_updateOfflineAccountPassword", new Object[]{chainId, address, encryptedPriKey2, password, passwordNew}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertEquals(AccountErrorCode.ADDRESS_ERROR.getCode(), cmdResp.getCode());
//
//            //测试错误的私钥 testing the wrong private key
//            response = CmdDispatcher.call("ac_updateOfflineAccountPassword", new Object[]{chainId, address, "86" + encryptedPriKey, password, passwordNew}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), cmdResp.getCode());//ADDRESS_ERROR
//
//            //测试错误的密码 testing the wrong password
//            response = CmdDispatcher.call("ac_updateOfflineAccountPassword", new Object[]{chainId, address, encryptedPriKey, password + "errorpwd123", passwordNew}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), cmdResp.getCode());
//
//            //为离线账户修改密码 modify password for offline account
//            response = CmdDispatcher.call("ac_updateOfflineAccountPassword", new Object[]{chainId, address, encryptedPriKey, password, passwordNew}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            String encryptedPriKeyNew = (String) result.get("encryptedPriKey");
//            assertNotEquals(encryptedPriKeyNew, encryptedPriKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void isEncryptedTest() {
//        try {
//            // create account
//            //创建未加密账户 create unencrypted account
//            List<String> accountList = createAccount(chainId, 1, null);
//            String address = accountList.get(0);
//
//            //验证账户是否加密 verify that the account is encrypted
//            String response = CmdDispatcher.call("ac_isEncrypted", new Object[]{chainId, address}, version);
//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            Boolean value = (Boolean) result.get("value");
//            assertFalse(value);
//
//            //为账户设置密码 set password for account
//            response = CmdDispatcher.call("ac_setPassword", new Object[]{chainId, address, password}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            value = (Boolean) result.get("value");
//            assertTrue(value);
//
//            //验证账户是否加密 verify that the account is encrypted
//            response = CmdDispatcher.call("ac_isEncrypted", new Object[]{chainId, address}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            value = (Boolean) result.get("value");
//            assertTrue(value);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    public void validationPasswordTest() {
        try {
            // create account
            //创建未加密账户 create unencrypted account
            List<String> accountList = createAccount(chainId, 1, null);
            String address = accountList.get(0);

            //验证账户是否正确 verify that the account password is correct
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            String response = CmdDispatcher.request("ac_validationPassword", params);
            Message cmdResp = JSONUtils.json2pojo(response, Message.class);
            Boolean value = (Boolean)JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getMessageData()), Response.class).getResponseData();
            assertFalse(value);

//            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            HashMap result = (HashMap) cmdResp.getResult();
//            Boolean value = (Boolean) result.get("value");
//            assertFalse(value);

            //为账户设置密码 set password for account
            response = CmdDispatcher.request("ac_setPassword", params);
            cmdResp = JSONUtils.json2pojo(response, Message.class);
            value = (Boolean)JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getMessageData()), Response.class).getResponseData();
            assertTrue(value);

//            response = CmdDispatcher.call("ac_setPassword", new Object[]{chainId, address, password}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            value = (Boolean) result.get("value");
//            assertTrue(value);

            //验证账户是否正确 verify that the account password is correct
            response = CmdDispatcher.request("ac_validationPassword", params);
            cmdResp = JSONUtils.json2pojo(response, Message.class);
            value = (Boolean)JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getMessageData()), Response.class).getResponseData();
            assertTrue(value);
//            response = CmdDispatcher.call("ac_validationPassword", new Object[]{chainId, address, password}, version);
//            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
//            result = (HashMap) cmdResp.getResult();
//            value = (Boolean) result.get("value");
//            assertTrue(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
