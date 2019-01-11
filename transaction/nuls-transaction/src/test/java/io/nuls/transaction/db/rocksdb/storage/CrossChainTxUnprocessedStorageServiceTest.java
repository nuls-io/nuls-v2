package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.init.TransactionBootStrap;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CrossChainTxUnprocessedStorageServiceTest {

    protected static CrossChainTxUnprocessedStorageService crossChainTxUnprocessedStorageService;
    protected int chainId = 12345;
    protected String nodeId = "1001";

    @BeforeClass
    public static void beforeTest() throws Exception {
        //初始化数据库配置文件
        TransactionBootStrap.initDB();
        //初始化上下文
        SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        crossChainTxUnprocessedStorageService = SpringLiteContext.getBean(CrossChainTxUnprocessedStorageService.class);
        //启动链
        SpringLiteContext.getBean(ChainManager.class).runChain();
    }

    @Test
    public void putTx() throws Exception {
        CrossChainTx ctx = TestConstant.createCrossChainTx();
        ctx.setState(TxConstant.CTX_UNPROCESSED_0);
        boolean result = crossChainTxUnprocessedStorageService.putTx(chainId, ctx);
        Assert.assertTrue(result);
    }

    @Test
    public void getTxList() {
        //test getTxList
        List<CrossChainTx> list = crossChainTxUnprocessedStorageService.getTxList(chainId);
        System.out.println(list.size());
        if (list != null && list.size() > 0) {
            NulsDigestData hash = list.get(0).getTx().getHash();
            //test getTx
            CrossChainTx tx = crossChainTxUnprocessedStorageService.getTx(chainId, hash);
            Assert.assertEquals(hash, tx.getTx().getHash());
            //test removeTxList
            List<CrossChainTx> removeList = List.of(list.get(0));
            crossChainTxUnprocessedStorageService.removeTxList(chainId, removeList);
            tx = crossChainTxUnprocessedStorageService.getTx(chainId, hash);
            Assert.assertNull(tx);
        }
    }

}