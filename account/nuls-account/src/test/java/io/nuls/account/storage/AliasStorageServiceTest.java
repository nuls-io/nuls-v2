package io.nuls.account.storage;

import io.nuls.account.constant.AccountParam;
import io.nuls.account.init.AccountBootstrap;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.service.AccountService;
import io.nuls.base.basic.AddressTool;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.thread.TimeService;
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
    protected static int chainId = 12345;

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
        AliasPo aliasPo = createAlias();
        ////Fist:save the alias to DB
        boolean result = aliasStorageService.saveAlias(aliasPo);
        assertTrue(result);
        ////Second:get the aliasPO by alias from DB
        AliasPo savedAliasPo = aliasStorageService.getAlias(chainId,aliasPo.getAlias());
        //Third:check the saved alias is right
        assertNotNull(savedAliasPo);
        assertTrue(Arrays.equals(aliasPo.getAddress(),savedAliasPo.getAddress()));
        assertEquals(aliasPo.getAlias(), savedAliasPo.getAlias());
        //Forth:remove the alias
        result = aliasStorageService.removeAlias(chainId,aliasPo.getAlias());
        assertTrue(result);
        //Fifth:get the alias from db and check
        AliasPo aliasPoAfterRemove = aliasStorageService.getAlias(chainId,aliasPo.getAlias());
        assertNull(aliasPoAfterRemove);
    }

    /**
     *
     * test get the alias list from db
     *
     *
     * @throws Exception
     */
    @Test
    public void getAliasListTest() throws Exception {
        AliasPo aliasPo1 = createAlias();
        boolean result = aliasStorageService.saveAlias(aliasPo1);
        assertTrue(result);
        AliasPo aliasPo2 = createAlias();
        result = aliasStorageService.saveAlias(aliasPo2);
        assertTrue(result);
        List<AliasPo> aliasPoList = aliasStorageService.getAliasList(chainId);
        assertTrue(aliasPoList != null && aliasPoList.size() >= 2);
    }

    /**
     *
     * test get the alias by address from db
     *
     *
     * @throws Exception
     */
    @Test
    public void getAliasByAddressTest() throws Exception {
        AliasPo aliasPo = createAlias();
        boolean result = aliasStorageService.saveAlias(aliasPo);
        assertTrue(result);
        AliasPo aliasPoAfterGet = aliasStorageService.getAliasByAddress(chainId, AddressTool.getStringAddressByBytes(aliasPo.getAddress()));
        assertNotNull(aliasPoAfterGet);
        assertEquals(aliasPo.getAlias(),aliasPoAfterGet.getAlias());
    }

    /**
     * create an AliasPo for test
     *
     * */
    public static AliasPo createAlias(){
        if (accountService == null) {
            accountService = SpringLiteContext.getBean(AccountService.class);
        }
        List<Account> accounts = accountService.createAccount(chainId,1,null);
        String alias = "Hi,我的别名是" + System.currentTimeMillis();
        AliasPo aliasPo = new AliasPo();
        aliasPo.setChainId(chainId);
        aliasPo.setAddress(accounts.get(0).getAddress().getAddressBytes());
        aliasPo.setAlias(alias);
        return aliasPo;
    }
}
