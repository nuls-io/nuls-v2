package io.nuls.transaction.storage.rocksdb;

import io.nuls.base.data.NulsDigestData;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.TransactionBootStrap;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.model.bo.CrossTx;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class UnverifiedCtxStorageServiceTest {

    protected static UnverifiedCtxStorageService unverifiedCtxStorageService;
    protected int chainId = 12345;
    protected String nodeId = "1001";

    @BeforeClass
    public static void beforeTest() throws Exception {
        //初始化数据库配置文件
        TransactionBootStrap.initDB();
        //初始化上下文
        SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        unverifiedCtxStorageService = SpringLiteContext.getBean(UnverifiedCtxStorageService.class);
        //启动链
        SpringLiteContext.getBean(ChainManager.class).runChain();
    }

    @Test
    public void putTx() throws Exception {
        CrossTx ctx = TestConstant.createCrossChainTx();
        boolean result = unverifiedCtxStorageService.putTx(chainId, ctx);
        Assert.assertTrue(result);
    }

    @Test
    public void getTxList() {
        //test getTxList
        List<CrossTx> list = unverifiedCtxStorageService.getTxList(chainId);
        System.out.println(list.size());
        if (list != null && list.size() > 0) {
            NulsDigestData hash = list.get(0).getTx().getHash();
            //test getTx
            CrossTx tx = unverifiedCtxStorageService.getTx(chainId, hash);
            Assert.assertEquals(hash, tx.getTx().getHash());
            //test removeTxList
            List<CrossTx> removeList = List.of(list.get(0));
            unverifiedCtxStorageService.removeTxList(chainId, removeList);
            tx = unverifiedCtxStorageService.getTx(chainId, hash);
            Assert.assertNull(tx);
        }
    }

}