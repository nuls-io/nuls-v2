package io.nuls.account.storage;

import io.nuls.account.AccountBootstrap;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.util.AccountTool;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.model.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/8
 */
public class AccountStorageServiceTest {

    protected static AccountStorageService accountStorageService;
    protected int chainId = 2;

    @BeforeClass
    public static void beforeTest() {
        SpringLiteContext.init("io.nuls.account", new ModularServiceMethodInterceptor());
        AccountBootstrap accountBootstrap = SpringLiteContext.getBean(AccountBootstrap.class);
        //初始化配置
        accountBootstrap.initCfg();
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(NulsConfig.DATA_PATH);
        //springLite容器初始化
        //启动时间同步线程
        //TimeService.getInstance().start();
        accountStorageService = SpringLiteContext.getBean(AccountStorageService.class);
    }

    @Test
    public void saveAccountListTest() throws Exception {
        List<AccountPo> accountPos = new ArrayList<>();
        int count = 10;
        String password = "a2678";
        for (int i = 0; i < count; i++) {
            //create account
            Account account = AccountTool.createAccount(chainId);
            if (StringUtils.isNotBlank(password)) {
                account.encrypt(password);
            }
            AccountPo po = new AccountPo(account);
            accountPos.add(po);
        }
        //批量保存账户数据
        boolean result = accountStorageService.saveAccountList(accountPos);
        assertTrue(result);
        assertEquals(accountPos.size(), count);
        for (AccountPo account : accountPos) {
            System.out.println(account.getAddress());
        }
    }

    @Test
    public void saveAccountTest() throws Exception {
        Account account = AccountTool.createAccount(chainId);
        boolean result = accountStorageService.saveAccount(new AccountPo(account));
        assertTrue(result);
    }

    @Test
    public void removeAccountTest() throws Exception {
        Account account = AccountTool.createAccount(chainId);
        boolean result = accountStorageService.saveAccount(new AccountPo(account));
        assertTrue(result);
        accountStorageService.removeAccount(account.getAddress());
        AccountPo accountPo = accountStorageService.getAccount(account.getAddress().getAddressBytes());
        assertNull(accountPo);
    }

    @Test
    public void updateAccountTest() throws Exception {
        Account account = AccountTool.createAccount(chainId);
        AccountPo accountPo = new AccountPo(account);
        boolean result = accountStorageService.saveAccount(accountPo);
        accountPo.setRemark("update remark");
        accountStorageService.updateAccount(accountPo);

        //check remark
        AccountPo newAccountPo = accountStorageService.getAccount(account.getAddress().getAddressBytes());
        assertEquals(accountPo.getRemark(), newAccountPo.getRemark());
        assertTrue(result);
    }

    @Test
    public void getAccountTest() throws Exception {
        Account account = AccountTool.createAccount(chainId);
        accountStorageService.saveAccount(new AccountPo(account));
        AccountPo accountPo = accountStorageService.getAccount(account.getAddress().getAddressBytes());
        assertNotNull(accountPo);
        assertEquals(account.getAddress().getBase58(), accountPo.getAddress());
    }

    @Test
    public void getAccountListTest() {
        List<AccountPo> accountList = accountStorageService.getAccountList();
        //sort by createTime desc
        Collections.sort(accountList, (AccountPo o1, AccountPo o2) -> (o2.getCreateTime().compareTo(o1.getCreateTime())));
        for (AccountPo account : accountList) {
            System.out.println(NulsDateUtils.timeStamp2DateStr(account.getCreateTime()) + "==" + account.getAddress());
        }
    }
}
