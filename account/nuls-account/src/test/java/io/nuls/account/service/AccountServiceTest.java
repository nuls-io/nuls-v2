package io.nuls.account.service;

import io.nuls.account.constant.AccountParam;
import io.nuls.account.init.AccountBootstrap;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
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
        assertFalse(accountService.isEncrypted(chainId,account.getAddress().getBase58()));
        // set password
        String password = "abc12345890987";
        accountService.setPassword(chainId, account.getAddress().getBase58(), password);
        assertTrue(accountService.isEncrypted(chainId,account.getAddress().getBase58()));
    }

}
