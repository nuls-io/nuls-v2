package io.nuls.transaction.storage;

import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.TransactionBootstrap;
import io.nuls.transaction.manager.ChainManager;
import org.junit.Before;

public class UnverifiedTxStorageServiceTest {

    protected static UnverifiedTxStorageService unverifiedTxStorageService;
    protected static ChainManager chainManager;
    protected int chainId = 2;

    @Before
    public void beforeTest() throws Exception {
        //初始化数据库配置文件
        new TransactionBootstrap().initDB();
        //初始化上下文
        SpringLiteContext.init(TestConstant.CONTEXT_PATH);
        unverifiedTxStorageService = SpringLiteContext.getBean(UnverifiedTxStorageService.class);
        chainManager = SpringLiteContext.getBean(ChainManager.class);
        //启动链
        SpringLiteContext.getBean(ChainManager.class).runChain();
    }
//
//    @Test
//    public void putTx() throws Exception {
//        Transaction tx = TestConstant.getTransaction2();
//        Chain chain = chainManager.getChain(chainId);
//        boolean result = unverifiedTxStorageService.putTx(chain, new TransactionNetPO(tx, "1"));
//        Assert.assertTrue(result);
//    }
//
//    @Test
//    public void pollTx() {
//        Chain chain = chainManager.getChain(chainId);
//        TransactionNetPO tx = unverifiedTxStorageService.pollTx(chain);
//        Assert.assertNotNull(tx);
//    }
}