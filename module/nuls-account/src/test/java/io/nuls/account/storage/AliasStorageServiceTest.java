package io.nuls.account.storage;

import io.nuls.account.AccountBootstrap;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.po.AliasPO;
import io.nuls.account.service.AccountService;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rockdb.service.RocksDBService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author EdwardChan
 * @description the test case of alias storage
 * @date Nov.9th 2018
 */
public class AliasStorageServiceTest {

    protected static AliasStorageService aliasStorageService;

    protected static AccountService accountService;
    protected static int chainId = 2;
    protected static int assetId = 1;

    @BeforeClass
    public static void beforeTest() {
        //初始化配置
        SpringLiteContext.init("io.nuls.account", new ModularServiceMethodInterceptor());
        AccountBootstrap accountBootstrap = SpringLiteContext.getBean(AccountBootstrap.class);
        //初始化配置
        accountBootstrap.initCfg();        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(NulsConfig.DATA_PATH);
        //springLite容器初始化
        //启动时间同步线程
        //TimeService.getInstance().start();
        aliasStorageService = SpringLiteContext.getBean(AliasStorageService.class);
        accountService = SpringLiteContext.getBean(AccountService.class);
    }

    /**
     *
     * test remove the alias
     *
     * The test cast contain saveAlia、getAlias and remove alias
     *
     *
     * @throws Exception
     */
    @Test
    public void removeAliasTest() throws Exception {
        Alias alias = createAlias();
        ////Fist:save the alias to DB
        boolean result = aliasStorageService.saveAlias(chainId,alias);
        assertTrue(result);
        ////Second:get the aliasPO by alias from DB
        AliasPO savedAliasPO = aliasStorageService.getAlias(chainId,alias.getAlias());
        //Third:check the saved alias is right
        assertNotNull(savedAliasPO);
        assertTrue(Arrays.equals(alias.getAddress(), savedAliasPO.getAddress()));
        assertEquals(alias.getAlias(), savedAliasPO.getAlias());
        //Forth:remove the alias
        result = aliasStorageService.removeAlias(chainId,alias.getAlias());
        assertTrue(result);
        //Fifth:get the alias from storage and check
        AliasPO aliasPOAfterRemove = aliasStorageService.getAlias(chainId,alias.getAlias());
        assertNull(aliasPOAfterRemove);
    }

    /**
     *
     * test get the alias list from storage
     *
     *
     * @throws Exception
     */
    //@Test
    public void getAliasListTest() throws Exception {
        Alias alias1 = createAlias();
        boolean result = aliasStorageService.saveAlias(chainId,alias1);
        assertTrue(result);
        Alias alias2 = createAlias();
        result = aliasStorageService.saveAlias(chainId,alias2);
        assertTrue(result);
    }

    /**
     *
     * test get the alias by address from storage
     *
     *
     * @throws Exception
     */
    @Test
    public void getAliasByAddressTest() throws Exception {
        Alias alias = createAlias();
        boolean result = aliasStorageService.saveAlias(chainId,alias);
        assertTrue(result);
        AliasPO aliasPOAfterGet = aliasStorageService.getAliasByAddress(chainId, AddressTool.getStringAddressByBytes(alias.getAddress()));
        assertNotNull(aliasPOAfterGet);
        assertEquals(alias.getAlias(), aliasPOAfterGet.getAlias());
    }

    /**
     * create an AliasPo for test
     *
     * */
    public static Alias createAlias(){
        if (accountService == null) {
            accountService = SpringLiteContext.getBean(AccountService.class);
        }
        Chain chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId));
        List<Account> accounts = accountService.createAccount(chain,1,null);
        String aliasStr = "Hi,我的别名是" + System.currentTimeMillis();
        Alias alias = new Alias();
        alias.setAddress(accounts.get(0).getAddress().getAddressBytes());
        alias.setAlias(aliasStr);
        return alias;
    }
}
