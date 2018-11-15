package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.dto.AccountKeyStoreDto;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.util.AccountTool;
import io.nuls.base.data.Page;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
    protected double version = 1.0;

    @BeforeClass
    public static void start() throws Exception {
        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
    }

    private List<String> createAccount(short chainId, int count, String password) {
        List<String> accountList = null;
        try {
            String response = CmdDispatcher.call("ac_createAccount", new Object[]{chainId, count, password}, version);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);

            if (!AccountConstant.SUCCESS_CODE.equals(cmdResp.getCode()) || (count <= 0 || count > AccountTool.CREATE_MAX_SIZE)) {
                return null;
            }
            accountList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountList;
    }

    public SimpleAccountDto getAccountByAddress(short chainId, String address) throws Exception {
        String response = CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, address}, version);
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
            String response = CmdDispatcher.call("ac_getPriKeyByAddress", new Object[]{chainId, address, password}, version);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            HashMap result = (HashMap) cmdResp.getResult();
            priKey = (String) result.get("priKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return priKey;
    }

    @Ignore
    @Test
    public void createAccountTest() throws Exception {
        int count = 1;
        //Test to create an account that is not empty.
        List<String> accountList = createAccount(chainId, count, password);
        //Checking the number of accounts returned
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
        String response = CmdDispatcher.call("ac_createOfflineAccount", new Object[]{chainId, count, password}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        List<AccountOfflineDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), AccountOfflineDto.class);
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

        String response = CmdDispatcher.call("ac_removeAccount", new Object[]{chainId, accountList.get(0), password}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());

        account = getAccountByAddress(chainId, accountList.get(0));
        assertNull(account);
    }

    @Test
    public void getAccountByAddressTest() throws Exception {
        List<String> accountList = createAccount(chainId, 1, password);
        String response = CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, accountList.get(0)}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        SimpleAccountDto accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), SimpleAccountDto.class);
        assertEquals(accountList.get(0), accountDto.getAddress());
    }

    @Test
    public void getAccountListTest() throws Exception {
        String response = CmdDispatcher.call("ac_getAccountList", new Object[]{}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        List<SimpleAccountDto> accountList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), SimpleAccountDto.class);
        accountList.forEach(account -> System.out.println(account.getAddress()));
    }

    @Test
    public void getAddressListTest() throws Exception {
        List<String> accountList = createAccount(chainId, 1, password);
        String response = CmdDispatcher.call("ac_getAddressList", new Object[]{chainId, 1, 10}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        Page<String> resultPage = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), Page.class);
        assertTrue(resultPage.getTotal() > 0);
        resultPage.getList().forEach(System.out::println);

        //Test paging parameter pageNumber error
        response = CmdDispatcher.call("ac_getAddressList", new Object[]{chainId, 0, 10}, version);
        cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
        //Test paging parameter pageSize error
        response = CmdDispatcher.call("ac_getAddressList", new Object[]{chainId, 1, -1}, version);
        cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
    }

    @Test
    public void getAllPrivateKeyTest() {
        try {
            List<String> accountList = createAccount((short) 1, 1, password);
            //query all accounts privateKey
            String response = CmdDispatcher.call("ac_getAllPriKey", new Object[]{(short) 0, password}, version);
            System.out.println(response);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            List<String> privateKeyAllList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
            //query all accounts privateKey the specified chain
            response = CmdDispatcher.call("ac_getAllPriKey", new Object[]{(short) 1, password}, version);
            List<String> privateKeyList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
            assertTrue(privateKeyList.size() >= accountList.size());
            assertTrue(privateKeyAllList.size() >= privateKeyList.size());
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getPrivateKeyTest() {
        try {
            //Create password accounts
            List<String> accountList = createAccount(chainId, 1, password);
            //Query specified account private key
            String response = CmdDispatcher.call("ac_getPriKeyByAddress", new Object[]{chainId, accountList.get(0), password}, version);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            HashMap result = (HashMap) cmdResp.getResult();
            assertNotNull(result.get("priKey"));
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
            String response = CmdDispatcher.call("ac_setRemark", new Object[]{chainId, accountList.get(0), remark}, version);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void importAccountByPriKeyTest() {
        try {
            //Create encrypted account
            List<String> accountList = createAccount(chainId, 1, password);
            String priKey = getPriKeyByAddress(chainId, accountList.get(0), password);
            assertNotNull(priKey);
            //账户已存在则覆盖 If the account exists, it covers.
            String response = CmdDispatcher.call("ac_importAccountByPriKey", new Object[]{chainId, priKey, password, true}, version);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            HashMap result = (HashMap) cmdResp.getResult();
            String address = (String) result.get("address");
            assertEquals(accountList.get(0), address);
            //账户已存在，不覆盖，返回错误提示  If the account exists, it will not be covered,return error message.
            response = CmdDispatcher.call("ac_importAccountByPriKey", new Object[]{chainId, priKey, password, false}, version);
            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());

            //移除账户，再导入 Remove the account and import it according to the private key.
            CmdDispatcher.call("ac_removeAccount", new Object[]{chainId, accountList.get(0), password}, version);
            //账户不存在则创建 If account does not exist, create
            response = CmdDispatcher.call("ac_importAccountByPriKey", new Object[]{chainId, priKey, password, false}, version);
            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());

            //测试未加密账户
            //Create an unencrypted account for test
            //由于getPriKeyByAddress只返回加密账户的私钥，所以无法得到未加密账户私钥，所以使用固定值测试
            String addressx = "XfbZd1RYgTtQBb7xeP3bziAd2kmQL3930";
            priKey = "00cf6b28b2885c550506006b72fab1ab85cbf7e1aafdc6c1661e2b82f7f0089185";
            //账户已存在则覆盖 If the account exists, it covers.
            response = CmdDispatcher.call("ac_importAccountByPriKey", new Object[]{chainId, priKey, null, true}, version);
            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            result = (HashMap) cmdResp.getResult();
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
            //Create encrypted account
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
            String response = CmdDispatcher.call("ac_importAccountByKeystore", new Object[]{chainId, keyStoreHex, password, true}, version);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            HashMap result = (HashMap) cmdResp.getResult();
            String address = (String) result.get("address");
            assertEquals(accountList.get(0), address);

            //账户已存在，不覆盖，返回错误提示  If the account exists, it will not be covered,return error message.
            response = CmdDispatcher.call("ac_importAccountByKeystore", new Object[]{chainId, keyStoreHex, password, false}, version);
            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            assertNotEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());

            //移除账户，再导入 Remove the account and import it according to the private key.
            response = CmdDispatcher.call("ac_removeAccount", new Object[]{chainId, accountList.get(0), password}, version);
            //账户不存在则创建 If account does not exist, create
            response = CmdDispatcher.call("ac_importAccountByKeystore", new Object[]{chainId, keyStoreHex, password, false}, version);
            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());

            //测试未加密账户
            //Create an unencrypted account for test
            //accountList = createAccount(chainId, 1, null);
            //由于getPriKeyByAddress只返回加密账户的私钥，所以无法得到未加密账户私钥，所以使用固定值测试
            String addressx = "XfbZd1RYgTtQBb7xeP3bziAd2kmQL3930";
            priKey = "00cf6b28b2885c550506006b72fab1ab85cbf7e1aafdc6c1661e2b82f7f0089185";

            //构造keystore对象
            account = getAccountByAddress(chainId, addressx);
            keyStoreDto = new AccountKeyStoreDto();
            keyStoreDto.setAddress(account.getAddress());
            keyStoreDto.setPubKey(account.getPubkeyHex());
            keyStoreDto.setEncryptedPrivateKey(account.getEncryptedPrikeyHex());
            keyStoreDto.setPrikey(priKey);

            //生成keystore HEX编码
            keyStoreHex = HexUtil.encode(JSONUtils.obj2json(keyStoreDto).getBytes());
            //账户已存在则覆盖 If the account exists, it covers.
            response = CmdDispatcher.call("ac_importAccountByKeystore", new Object[]{chainId, keyStoreHex, null, true}, version);
            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            result = (HashMap) cmdResp.getResult();
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
            //Create encrypted account
            List<String> accountList = createAccount(chainId, 1, password);
            String address = accountList.get(0);

            //测试不指定备份路径
            String pathDir = "";
            //导出账户keystore路径  export account keyStore path
            String response = CmdDispatcher.call("ac_exportAccountKeyStore", new Object[]{chainId, address, password, pathDir}, version);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            HashMap result = (HashMap) cmdResp.getResult();
            String path = (String) result.get("path");
            assertNotNull(path);

            //测试指定非windows备份路径
            pathDir = "测试1/back/up";
            //导出账户keystore路径  export account keyStore path
            response = CmdDispatcher.call("ac_exportAccountKeyStore", new Object[]{chainId, address, password, pathDir}, version);
            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            result = (HashMap) cmdResp.getResult();
            path = (String) result.get("path");
            assertNotNull(path);

            //测试指定windows备份路径
            pathDir = "D:\\workspace\\github\\nuls_2.0\\测试2\\back\\up";
            //Create an unencrypted account for test
            accountList = createAccount(chainId, 1, null);
            address = accountList.get(0);
            //导出账户keystore路径  export account keyStore path
            response = CmdDispatcher.call("ac_exportAccountKeyStore", new Object[]{chainId, address, null, pathDir}, version);
            cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
            result = (HashMap) cmdResp.getResult();
            path = (String) result.get("path");
            assertNotNull(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
