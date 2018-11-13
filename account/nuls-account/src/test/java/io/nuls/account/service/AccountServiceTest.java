package io.nuls.account.service;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountParam;
import io.nuls.account.init.AccountBootstrap;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/8
 */
public class AccountServiceTest {

    protected static AccountService accountService;
    protected short chainId = 12345;
    protected String password = "a12345678";

    @BeforeClass
    public static void beforeTest() {
        //初始化配置
        AccountBootstrap.initCfg();
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(AccountParam.getInstance().getDataPath());
        //springLite容器初始化
        SpringLiteContext.init("io.nuls.account", new ModularServiceMethodInterceptor());
        //启动时间同步线程
        TimeService.getInstance().start();
        accountService = SpringLiteContext.getBean(AccountService.class);
    }

    /**
     * set password test
     * <p>
     * Nov.10th 2018
     *
     * @auther EdwardChan
     */
    @Test
    public void setPasswordTest() {
        // create account
        List<Account> result = accountService.createAccount(chainId, 1, null);
        assertTrue(result != null && result.size() == 1);
        Account account = result.get(0);
        assertFalse(account.isEncrypted());
        // set password
        String password = "abc12345890987";
        accountService.setPassword(chainId, account.getAddress().getBase58(), password);
        //get account
        Account accountAfterSetPassword = accountService.getAccount(chainId, account.getAddress().getBase58());
        //check if the account is encrypted
        assertNotNull(accountAfterSetPassword);
        assertTrue(accountAfterSetPassword.isEncrypted());
        assertTrue(account.validatePassword(password));
    }

    /**
     * the account is encrypted test
     * <p>
     * Nov.10th 2018
     *
     * @auther EdwardChan
     */
    @Test
    public void isEncryptedTest() {
        // create account
        List<Account> result = accountService.createAccount(chainId, 1, null);
        assertTrue(result != null && result.size() == 1);
        Account account = result.get(0);
        assertFalse(accountService.isEncrypted(chainId, account.getAddress().getBase58()));
        // set password
        String password = "abc12345890987";
        accountService.setPassword(chainId, account.getAddress().getBase58(), password);
        assertTrue(accountService.isEncrypted(chainId, account.getAddress().getBase58()));
    }

    /**
     * Create a specified number of accounts
     */
    @Test
    public void createAccountTest() throws Exception {
        int count = 1;
        //Test to create an account that is not empty.
        List<Account> accoutList = accountService.createAccount(chainId, count, password);
        //Checking the number of accounts returned
        assertEquals(accoutList.size(), count);

        //Test to create an empty password account
        accoutList = accountService.createAccount(chainId, count, null);
        //Checking the number of accounts returned
        assertEquals(accoutList.size(), count);

        try {
            //Test the largest number of generated accounts.
            accoutList = accountService.createAccount(chainId, 101, password);
            assertNull(accoutList);
        } catch (NulsRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Remove specified account test
     */
    @Test
    public void removeAccounTest() {
        //create account
        List<Account> accoutList = accountService.createAccount(chainId, 2, password);
        //query account
        Account account = accountService.getAccount(chainId, accoutList.get(0).getAddress().getBase58());
        assertNotNull(account);
        //remove specified account
        boolean result = accountService.removeAccount(chainId, accoutList.get(0).getAddress().getBase58(), password);
        assertTrue(result);
        //once again verify that accounts exist.
        account = accountService.getAccount(chainId, accoutList.get(0).getAddress().getBase58());
        assertNull(account);
    }

    @Test
    public void getAccountTest() throws Exception {
        //create account
        List<Account> accoutList = accountService.createAccount(chainId, 1, password);
        //query account
        Account account = accountService.getAccount(chainId, accoutList.get(0).getAddress().getBase58());
        assertNotNull(account);
        assertEquals(accoutList.get(0).getAddress().getBase58(), account.getAddress().getBase58());
    }

    @Test
    public void getAccountListTest() throws Exception {
        //query all accounts
        List<Account> accoutList = accountService.getAccountList();
        int oldSize = accoutList.size();
        //create account
        List<Account> accouts = accountService.createAccount(chainId, 1, password);
        //check whether the accounts are equal in number.
        accoutList = accountService.getAccountList();
        int newSize = accoutList.size();
        assertEquals(newSize, oldSize + accouts.size());
    }

}
