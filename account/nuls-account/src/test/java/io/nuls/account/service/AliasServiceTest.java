package io.nuls.account.service;

import io.nuls.account.constant.AccountParam;
import io.nuls.account.AccountBootstrap;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.storage.AliasStorageServiceTest;
import io.nuls.base.basic.AddressTool;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.thread.TimeService;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/8
 */
public class AliasServiceTest {

    protected static AccountService accountService;
    protected static AliasService aliasService;
    protected int chainId = 12345;
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
        aliasService = SpringLiteContext.getBean(AliasService.class);
    }

    /**
     * save the alias
     * <p>
     * Nov.10th 2018
     *
     * @auther EdwardChan
     */
    @Test
    public void saveAliasTest() throws Exception {
        // create account
        Alias alias = AliasStorageServiceTest.createAlias();
        boolean result = aliasService.aliasTxCommit(chainId,alias);
        assertTrue(result);
        Account account = accountService.getAccount(chainId, AddressTool.getStringAddressByBytes(alias.getAddress()));
        assertNotNull(account);
        assertEquals(account.getAlias(),alias.getAlias());
    }
}
