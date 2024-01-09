package io.nuls.account.service;

import io.nuls.account.AccountBootstrap;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.common.ConfigBean;
import io.nuls.core.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rockdb.service.RocksDBService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/20
 */
public class AccountKeyStoreServiceTest {

    protected static AccountService accountService;
    protected static AccountKeyStoreService accountKeyStoreService;
    protected int chainId = 2;
    protected int assetId = 1;
    protected String password = "a2678";

    @BeforeClass
    public static void beforeTest() {
        //Initialize configuration
        SpringLiteContext.init("io.nuls.account", new ModularServiceMethodInterceptor());
        AccountBootstrap accountBootstrap = SpringLiteContext.getBean(AccountBootstrap.class);
        //Initialize configuration
        accountBootstrap.initCfg();
        //Read the configuration file, store the data in the root directory, initialize and open all table connections in that directory, and place them in the cache
        RocksDBService.init(NulsConfig.DATA_PATH);
        //Start time synchronization thread
//        TimeService.getInstance().start();
        accountService = SpringLiteContext.getBean(AccountService.class);
        accountKeyStoreService = SpringLiteContext.getBean(AccountKeyStoreService.class);
    }

    @Test
    public void backupAccountToKeyStoreTest() {
        //Create password accounts
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        configBean.setMaxViewGas(100000000L);
        chain.setConfig(configBean);
        List<Account> accountList = accountService.createAccount(chain, 1, password);
        String address = accountList.get(0).getAddress().getBase58();
        //Test not specifying backup path
        String pathDir = "";
        //Backup accountkeystore  backup account keyStore
        String path = accountKeyStoreService.backupAccountToKeyStore(pathDir, chainId, address, password);
        assertNotNull(path);

        //Test specified nonwindowsBackup path
        pathDir = "test1/back/up";
        //Backup accountkeystore  backup account keyStore
        path = accountKeyStoreService.backupAccountToKeyStore(pathDir, chainId, address, password);
        assertNotNull(path);

        //Test specifiedwindowsBackup path
        pathDir = "D:\\workspace\\github\\nuls_2.0\\test2\\back\\up";
        //Create an unencrypted account for test
        accountList = accountService.createAccount(chain, 1, null);
        address = accountList.get(0).getAddress().getBase58();
        //Backup accountkeystore  backup account keyStore
        path = accountKeyStoreService.backupAccountToKeyStore(pathDir, chainId, address, password);
        assertNotNull(path);

    }

}
