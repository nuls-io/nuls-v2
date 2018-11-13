package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.util.AccountTool;
import io.nuls.base.data.Page;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.model.CmdResponse;
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

    private List<String> createAccount(short chainId, int count, String password) throws Exception {
        String response = CmdDispatcher.call("ac_createAccount", new Object[]{chainId, count, password}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);

        if (!AccountConstant.SUCCESS_CODE.equals(cmdResp.getCode()) || (count <= 0 || count > AccountTool.CREATE_MAX_SIZE)) {
            return null;
        }
        List<String> accoutList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
        return accoutList;
    }

    public SimpleAccountDto getAccountByAddress(short chainId, String address) throws Exception {
        String response = CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, address}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        SimpleAccountDto accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), SimpleAccountDto.class);
        return accountDto;
    }

    @Ignore
    @Test
    public void createAccountTest() throws Exception {
        int count = 1;
        //Test to create an account that is not empty.
        List<String> accoutList = createAccount(chainId, count, password);
        //Checking the number of accounts returned
        assertEquals(accoutList.size(), count);
        for (String address : accoutList) {
            System.out.println(address);
        }

        //Test to create an empty password account
        accoutList = createAccount(chainId, count, null);
        //Checking the number of accounts returned
        assertEquals(accoutList.size(), count);

        //Test the largest number of generated accounts.
        accoutList = createAccount(chainId, 101, null);
        assertNull(accoutList);


    }

    @Test
    public void createOfflineAccountTest() throws Exception {
        int count = 10;
        String response = CmdDispatcher.call("ac_createOfflineAccount", new Object[]{chainId, count, password}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        List<AccountOfflineDto> accoutList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), AccountOfflineDto.class);
        assertEquals(accoutList.size(), count);
        for (AccountOfflineDto account : accoutList) {
            System.out.println(account.getAddress());
        }
    }

    @Test
    public void removeAccountTest() throws Exception {
        List<String> accoutList = createAccount(chainId, 2, password);

        SimpleAccountDto account = getAccountByAddress(chainId, accoutList.get(0));
        assertNotNull(account);

        String response = CmdDispatcher.call("ac_removeAccount", new Object[]{chainId, accoutList.get(0), password}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        assertEquals(AccountConstant.SUCCESS_CODE, cmdResp.getCode());

        account = getAccountByAddress(chainId, accoutList.get(0));
        assertNull(account);
    }

    @Test
    public void getAccountByAddressTest() throws Exception {
        List<String> accoutList = createAccount(chainId, 1, password);
        String response = CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, accoutList.get(0)}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        SimpleAccountDto accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), SimpleAccountDto.class);
        assertEquals(accoutList.get(0), accountDto.getAddress());
    }

    @Test
    public void getAccountListTest() throws Exception {
        String response = CmdDispatcher.call("ac_getAccountList", new Object[]{}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        List<SimpleAccountDto> accoutList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), SimpleAccountDto.class);
        accoutList.forEach(account -> System.out.println(account.getAddress()));
    }

    @Test
    public void getAddressListTest() throws Exception {
        List<String> accoutList = createAccount(chainId, 1, password);
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
            //String errorRemark = "test error remark test error remark test error remark test error remark";
            //Create password accounts
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
}
