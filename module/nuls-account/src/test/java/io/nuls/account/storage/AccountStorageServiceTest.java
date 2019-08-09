package io.nuls.account.storage;

import io.nuls.account.AccountBootstrap;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.po.AccountPO;
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
        List<AccountPO> accountPOs = new ArrayList<>();
        int count = 10;
        String password = "a2678";
        for (int i = 0; i < count; i++) {
            //create account
            Account account = AccountTool.createAccount(chainId);
            if (StringUtils.isNotBlank(password)) {
                account.encrypt(password);
            }
            AccountPO po = new AccountPO(account);
            accountPOs.add(po);
        }
        //批量保存账户数据
        boolean result = accountStorageService.saveAccountList(accountPOs);
        assertTrue(result);
        assertEquals(accountPOs.size(), count);
        for (AccountPO account : accountPOs) {
            System.out.println(account.getAddress());
        }
    }

    @Test
    public void saveAccountTest() throws Exception {
        Account account = AccountTool.createAccount(chainId);
        boolean result = accountStorageService.saveAccount(new AccountPO(account));
        assertTrue(result);
    }

    @Test
    public void removeAccountTest() throws Exception {
        Account account = AccountTool.createAccount(chainId);
        boolean result = accountStorageService.saveAccount(new AccountPO(account));
        assertTrue(result);
        accountStorageService.removeAccount(account.getAddress());
        AccountPO accountPo = accountStorageService.getAccount(account.getAddress().getAddressBytes());
        assertNull(accountPo);
    }

    @Test
    public void updateAccountTest() throws Exception {
        Account account = AccountTool.createAccount(chainId);
        AccountPO accountPo = new AccountPO(account);
        boolean result = accountStorageService.saveAccount(accountPo);
        accountPo.setRemark("update remark");
        accountStorageService.updateAccount(accountPo);

        //check remark
        AccountPO newAccountPO = accountStorageService.getAccount(account.getAddress().getAddressBytes());
        assertEquals(accountPo.getRemark(), newAccountPO.getRemark());
        assertTrue(result);
    }

    @Test
    public void getAccountTest() throws Exception {
        Account account = AccountTool.createAccount(chainId);
        accountStorageService.saveAccount(new AccountPO(account));
        AccountPO accountPo = accountStorageService.getAccount(account.getAddress().getAddressBytes());
        assertNotNull(accountPo);
        assertEquals(account.getAddress().getBase58(), accountPo.getAddress());
    }

    @Test
    public void getAccountListTest() {
        List<AccountPO> accountList = accountStorageService.getAccountList();
        //sort by createTime desc
        Collections.sort(accountList, (AccountPO o1, AccountPO o2) -> (o2.getCreateTime().compareTo(o1.getCreateTime())));
        for (AccountPO account : accountList) {
            System.out.println(NulsDateUtils.timeStamp2DateStr(account.getCreateTime()) + "==" + account.getAddress());
        }
    }
}
