package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.dto.AccountKeyStoreDTO;
import io.nuls.account.model.dto.AccountOfflineDTO;
import io.nuls.account.model.dto.SimpleAccountDTO;
import io.nuls.account.rpc.common.CommonRpcOperation;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsHash;
import io.nuls.core.basic.Page;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.parse.JSONUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class AccountCmdTest {

    //protected static AccountService accountService;

    protected int chainId = 2;
    protected String password = "nuls123456";
    protected String newPassword = "c12345678";
    protected String version = "1.0";
    protected String success = "1";

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    public SimpleAccountDTO getAccountByAddress(int chainId, String address) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", address);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountByAddress", params);

        SimpleAccountDTO accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("ac_getAccountByAddress")), SimpleAccountDTO.class);
        return accountDto;
    }

    /**
     * Query private key based on address
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
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
            priKey = (String) result.get("priKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return priKey;
    }

    @Test
    public void createAccountTest() throws Exception {
//       int count = 3;
//        //test to create an account that is not empty.
//        List<String> accountList = CommonRpcOperation.createAccount(count);
//        //checking the number of accounts returned
//        assertEquals(accountList.size(), count);
//        for (String address : accountList) {
//            System.out.println(address);
//        }

        //Test to create an empty password account
        List<String> accountList2 = CommonRpcOperation.createAccount(chainId, 2, password);
        //Checking the number of accounts returned
        assertEquals(accountList2.size(), 2);
        for (String address : accountList2) {
            System.out.println(address);
        }
/*

        //Test the largest number of generated accounts.
        List<String> accountList3 = CommonRpcOperation.createAccount(chainId, 101, null);
        assertNull(accountList3);

*/

    }

    @Test
    public void createOfflineAccountTest() throws Exception {
        int count = 10;
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("count", count);
        params.put("password", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createOfflineAccount", params);
        //List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(cmdResp.getResponseData()), AccountOfflineDto.class);
        List<AccountOfflineDTO> accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createOfflineAccount")).get("list")), AccountOfflineDTO.class);
        assertEquals(accountList.size(), count);
        for (AccountOfflineDTO account : accountList) {
            System.out.println(account.getAddress());
        }
    }

    @Test
    public void removeAccountTest() throws Exception {
        List<String> accountList = CommonRpcOperation.createAccount(chainId, 2, password);

        SimpleAccountDTO account = getAccountByAddress(chainId, accountList.get(0));
        assertNotNull(account);

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", accountList.get(0));
        params.put("password", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params);
        assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        account = getAccountByAddress(chainId, accountList.get(0));
        assertNull(account);
    }

    @Test
    public void getAccountByAddressTest() throws Exception {
        List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
        SimpleAccountDTO accountDto = getAccountByAddress(chainId, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG");
        assertEquals(accountList.get(0), accountDto.getAddress());
    }

    @Test
    public void getAccountListTest() throws Exception {
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAccountList", null);
        //List<SimpleAccountDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(cmdResp.getResponseData()), SimpleAccountDto.class);
        List<SimpleAccountDTO> accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAccountList")).get("list")), SimpleAccountDTO.class);
        accountList.forEach(account -> System.out.println(account.getAddress()));
    }

    @Test
    public void getAddressListTest() throws Exception {
        List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("pageNumber", 1);
        params.put("pageSize", 10);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAddressList", params);
        Page<String> resultPage = JSONUtils.json2pojo(JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("ac_getAddressList")), Page.class);
        assertTrue(resultPage.getTotal() > 0);
        resultPage.getList().forEach(System.out::println);

        //test paging parameter pageNumber error
        params.put("pageNumber", 0);
        cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAddressList", params);
        assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        //Test paging parameter pageSize error
        params.put("pageNumber", 1);
        params.put("pageSize", -1);
        cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAddressList", params);
        assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
    }

    @Test
    public void getAllPrivateKeyTest() {
        try {
            List<String> accountList = CommonRpcOperation.createAccount(1, 1, password);
            //query all accounts privateKey
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, 0);
            params.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAllPriKey", params);
            if (AccountConstant.SUCCESS_CODE.equals(cmdResp.getResponseStatus())) {
                List<String> privateKeyAllList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAllPriKey")).get("list");
                //query all accounts privateKey the specified chain
                params.put(Constants.CHAIN_ID, 1);
                cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAllPriKey", params);
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
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            //query specified account private key
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", accountList.get(0));
            params.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
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
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            //Set the correct remarks for the account
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", accountList.get(0));
            params.put("remark", remark);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setRemark", params);
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
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            String priKey = getPriKeyByAddress(chainId, accountList.get(0), password);
            assertNotNull(priKey);
            //Overwrite if account already exists If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("priKey", "5d3ee4ab8f9d5c03fb061ad14fe014c999a35c4a03d19a56d10cb4ad95d8463c");
            params.put("password", password);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
//            assertEquals(accountList.get(0), address);
            //Account already exists, not overwritten, returning error message  If the account exists, it will not be covered,return error message.
            params.put("overwrite", false);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //Remove account and import again Remove the account and import it according to the private key.
            Map<String, Object> params2 = new HashMap<>();
            params2.put(Constants.VERSION_KEY_STR, version);
            params2.put("chainId", chainId);
            params2.put("address", accountList.get(0));
            params2.put("password", password);
            ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params2);
            //Create if the account does not exist If account does not exist, create
            params.put("priKey", priKey);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //Test unencrypted accounts
            //create an unencrypted account for test
            //Due togetPriKeyByAddressOnly the private key of the encrypted account is returned, so it is not possible to obtain the private key of the unencrypted account, so a fixed value test is used
            String addressx = "KMNPqwARu77qAL4UCkd5Vwvj5PAtw3930";
            priKey = "00af59aa43536f6162a7166cdc1a389b32be0a06bc06f71a601a92e08fd2788dfe";
            //Overwrite if account already exists If the account exists, it covers.
            params.remove("password");
            params.put("priKey", priKey);
            params.put("overwrite", true);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            address = (String) result.get("address");
            assertEquals(addressx, address);
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
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            String priKey = getPriKeyByAddress(chainId, accountList.get(0), password);
            assertNotNull(priKey);

            //structurekeystoreobject
            SimpleAccountDTO account = getAccountByAddress(chainId, accountList.get(0));
            AccountKeyStoreDTO keyStoreDto = new AccountKeyStoreDTO();
            keyStoreDto.setAddress(account.getAddress());
            keyStoreDto.setPubKey(account.getPubkeyHex());
            keyStoreDto.setEncryptedPrivateKey(account.getEncryptedPrikeyHex());
            //keyStoreDto.setPrikey(priKey);

            //generatekeystore HEXcoding
            String keyStoreHex = HexUtil.encode(JSONUtils.obj2json(keyStoreDto).getBytes());
            //Overwrite if account already exists If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("keyStore", keyStoreHex);
            params.put("password", password);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByKeystore");
            String address = (String) result.get("address");
            assertEquals(accountList.get(0), address);

            //Account already exists, not overwritten, returning error message  If the account exists, it will not be covered,return error message.
            params.put("overwrite", false);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //Remove account and import again Remove the account and import it according to the private key.
            Map<String, Object> params2 = new HashMap<>();
            params2.put(Constants.VERSION_KEY_STR, version);
            params2.put("chainId", chainId);
            params2.put("address", accountList.get(0));
            params2.put("password", password);
            ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_removeAccount", params2);
            //Create if the account does not exist If account does not exist, create
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //Test unencrypted accounts
            //Create an unencrypted account for test
            //Due togetPriKeyByAddressOnly the private key of the encrypted account is returned, so it is not possible to obtain the private key of the unencrypted account, so a fixed value test is used
            String addressx = "XfbZd1RYgTtQBb7xeP3bziAd2kmQL3930";
            priKey = "00cf6b28b2885c550506006b72fab1ab85cbf7e1aafdc6c1661e2b82f7f0089185";

            //structurekeystoreobject
            account = getAccountByAddress(chainId, addressx);
            keyStoreDto = new AccountKeyStoreDTO();
            keyStoreDto.setAddress(account.getAddress());
            keyStoreDto.setPubKey(account.getPubkeyHex());
            //keyStoreDto.setEncryptedPrivateKey(account.getEncryptedPrikeyHex());
            keyStoreDto.setPrikey(priKey);

            //generatekeystore HEXcoding
            keyStoreHex = HexUtil.encode(JSONUtils.obj2json(keyStoreDto).getBytes());
            //Overwrite if account already exists If the account exists, it covers.
            params.put("keyStore", keyStoreHex);
            params.remove("password");
            params.put("overwrite", true);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByKeystore", params);
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
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            String address = accountList.get(0);

            //Test not specifying backup path
            String pathDir = "";
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            params.put("pathDir", pathDir);
            //Export Accountkeystorepath  export account keyStore path
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_exportAccountKeyStore", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_exportAccountKeyStore");
            String path = (String) result.get("path");
            assertNotNull(path);

            //Test specified nonwindowsBackup path
            pathDir = "test1/back/up";
            params.put("pathDir", pathDir);
            //Export Accountkeystorepath  export account keyStore path
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_exportAccountKeyStore", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_exportAccountKeyStore");
            path = (String) result.get("path");
            assertNotNull(path);

            //Test specifiedwindowsBackup path
            //Create an unencrypted account for test
            accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            pathDir = "D:\\workspace\\github\\nuls_2.0\\test2\\back\\up";
            params.put("address", accountList.get(0));
            params.remove("password");
            params.put("pathDir", pathDir);
            //Export Accountkeystorepath  export account keyStore path
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_exportAccountKeyStore", params);
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
            //Create an unencrypted account create unencrypted account
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            String address = accountList.get(0);

            //Set password for account set password for account
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setPassword");
            Boolean value = (Boolean) result.get("value");
            assertTrue(value);

            //Repeat password setting for account Repeat password for account
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            //Cannot reset password again Password cannot be set again.
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updatePasswordTest() {
        try {
            //Create an unencrypted account create unencrypted account
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            String address = accountList.get(0);

            //Change password for accounts without password set change password for account
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            params.put("newPassword", newPassword);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_updatePassword", params);
            //Password cannot be changed without setting it. You must first set the password before changing it No password can be changed, password must be set first, then password should be changed.
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());

            //Set password for account set password for account
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            Map result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setPassword");
            Boolean value = (Boolean) result.get("value");
            assertTrue(value);

            //Change password for account change password for account
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_updatePassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_updatePassword");
            value = (Boolean) result.get("value");
            assertTrue(value);

            //Change password for account using incorrect old password using old password to change password for account
            params.put("password", "errorpwd123");
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_updatePassword", params);
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
            params.put(Constants.CHAIN_ID, chainId);
            params.put("count", 1);
            params.put("password", password);
            //Create an unencrypted offline account create unencrypted account
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createOfflineAccount", params);
            //List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResponseData())).get("list")), AccountOfflineDto.class);
            List<AccountOfflineDTO> accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createOfflineAccount")).get("list")), AccountOfflineDTO.class);
            String address = accountList.get(0).getAddress();
            String priKey = accountList.get(0).getPriKey();

            params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("priKey", priKey);
            params.put("password", password);
            //Set password for account set password for account
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setOfflineAccountPassword", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setOfflineAccountPassword");
            String encryptedPriKey = (String) result.get("encryptedPriKey");
            assertNotNull(encryptedPriKey);

            params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("encryptedPriKey", encryptedPriKey);
            params.put("newPassword", newPassword);
            //Repeatedly setting passwords for offline accounts repeat password for account
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setOfflineAccountPassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setOfflineAccountPassword");
            String encryptedPriKeyNew = (String) result.get("encryptedPriKey");
            //Password can be reset for offline accounts Password cannot be set again.
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
            params.put(Constants.CHAIN_ID, chainId);
            params.put("count", 1);
            params.put("password", password);
            //Create an unencrypted offline account create unencrypted account
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createOfflineAccount", params);
            List<AccountOfflineDTO> accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createOfflineAccount")).get("list")), AccountOfflineDTO.class);
            String address = accountList.get(0).getAddress();
            String priKey = accountList.get(0).getPriKey();

            //Create encrypted offline account create encrypted account
            params.put("password", password);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createOfflineAccount", params);
            accountList = JSONUtils.json2list(JSONUtils.obj2json(((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createOfflineAccount")).get("list")), AccountOfflineDTO.class);
            String encryptedPriKey2 = accountList.get(0).getEncryptedPriKey();

            //Set password for account set password for account
            params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("priKey", priKey);
            params.put("password", password);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setOfflineAccountPassword", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setOfflineAccountPassword");
            String encryptedPriKey = (String) result.get("encryptedPriKey");
//            assertNotNull(encryptedPriKey);

            //Test incorrect address testing the wrong address
            params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("priKey", encryptedPriKey2);
            params.put("password", password);
            params.put("newPassword", newPassword);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_updateOfflineAccountPassword", params);
            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_updateOfflineAccountPassword"));
            assertEquals(AccountErrorCode.ADDRESS_ERROR.getCode(), result.get("code"));

            //Test incorrect private key testing the wrong private key
//            params.put("address", address);
//            params.put("priKey", "86" + encryptedPriKey);
//            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_updateOfflineAccountPassword", params);
//            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_updateOfflineAccountPassword"));
//            assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), result.get("code"));

            //Test incorrect password testing the wrong password
            params.put("priKey", encryptedPriKey);
            params.put("password", password + "errorpwd");
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_updateOfflineAccountPassword", params);
            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_updateOfflineAccountPassword"));
            assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), result.get("code"));

            //Change password for offline account modify password for offline account
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("priKey", encryptedPriKey);
            params.put("password", password);
            params.put("newPassword", newPassword);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_updateOfflineAccountPassword", params);
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
            //Create an unencrypted account create unencrypted account
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            String address = accountList.get(0);

            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            //Verify if the account is encrypted verify that the account is encrypted
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_isEncrypted", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_isEncrypted");
            Boolean value = (Boolean) result.get("value");
            assertFalse(value);

            //Set password for account set password for account
            params.put("password", password);
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setPassword");
            value = (Boolean) result.get("value");
            assertTrue(value);

            //Verify if the account is encrypted verify that the account is encrypted
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_isEncrypted", params);
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
            //Create an unencrypted account create unencrypted account
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            String address = accountList.get(0);

            //Verify if the account is correct verify that the account password is correct
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_validationPassword", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_validationPassword");
            Boolean value = (Boolean) result.get("value");
            assertFalse(value);

            //Set password for account set password for account
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_setPassword", params);
            result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_setPassword");
            value = (Boolean) result.get("value");
            assertTrue(value);

            //Verify if the account is correct verify that the account password is correct
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_validationPassword", params);
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
            //Create encrypted account create encrypted account
            List<String> accountList = CommonRpcOperation.createAccount(chainId, 1, password);
            String address = accountList.get(0);
            //byte[] addressBytes = accountList.get(0).getAddress().getAddressBytes();
            byte[] addressBytes = accountList.get(0).getBytes();

            //Create a transaction with alias settings
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(System.currentTimeMillis()/1000);
            Alias alias = new Alias(addressBytes, "alias");
            tx.setTxData(alias.serialize());

//            CoinDataResult coinDataResult = accountLedgerService.getCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size() , TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
//            if (!coinDataResult.isEnough()) {
//                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
//            }
            CoinData coinData = new CoinData();
            //coinData.setFrom(coinDataResult.getCoinList());
            CoinTo coin = new CoinTo();
            coin.setAddress(AddressTool.getAddress("tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn"));
            coin.setAmount(new BigInteger("1"));
            coin.setAssetsChainId(chainId);
            coin.setAssetsId(1);
            coinData.addTo(coin);

            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            String dataHex = HexUtil.encode(tx.getHash().getBytes());
            //Test password is correct
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, version);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            params.put("dataHex", dataHex);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", params);
            HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_signDigest"));
            String signatureHex = (String) result.get(RpcConstant.SIGNATURE);
            assertNotNull(signatureHex);

            //Incorrect test password
            params.put("password", password + "error");
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", params);
            result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_signDigest"));
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getResponseStatus());
            assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), result.get("code"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
