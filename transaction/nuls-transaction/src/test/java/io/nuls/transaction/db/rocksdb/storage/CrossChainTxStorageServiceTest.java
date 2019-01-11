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
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrossChainTxStorageServiceTest {

    protected static CrossChainTxStorageService crossChainTxStorageService;
    protected int chainId = 12345;
    protected String nodeId = "1001";

    @BeforeClass
    public static void beforeTest() throws Exception {
        //初始化数据库配置文件
        TransactionBootStrap.initDB();
        //初始化上下文
        SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        crossChainTxStorageService = SpringLiteContext.getBean(CrossChainTxStorageService.class);
        //启动链
        SpringLiteContext.getBean(ChainManager.class).runChain();
    }

    @Test
    public void putTx() throws Exception {
//        CrossChainTx ctx = new CrossChainTx();
//        Transaction tx = new Transaction(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
//        CrossTxData txData = new CrossTxData();
//        txData.setChainId(chainId);
//        //txData.setOriginalTxHash(originalTxHash);
//        tx.setRemark(StringUtils.bytes("ctx remark"));
//        tx.setTxData(txData.serialize());
//        //tx.setCoinData(coinData.serialize());
//        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
//        ctx.setTx(tx);
//        ctx.setSenderChainId(chainId);
//        ctx.setSenderNodeId(nodeId);
//        ctx.setState(TxConstant.CTX_UNPROCESSED_0);
        CrossChainTx ctx = TestConstant.createCrossChainTx();
        boolean result = crossChainTxStorageService.putTx(chainId, ctx);
        Assert.assertTrue(result);
    }

    @Test
    public void putTxs() throws Exception {
        List<CrossChainTx> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CrossChainTx ctx = TestConstant.createCrossChainTx();
            list.add(ctx);
        }
        boolean result = crossChainTxStorageService.putTxs(chainId, list);
        Assert.assertTrue(result);
    }

    @Test
    public void getTxList() {
        //test getTxList
        List<CrossChainTx> list = crossChainTxStorageService.getTxList(chainId);
        if (list != null) {
            NulsDigestData hash = list.get(0).getTx().getHash();
            //test getTx
            CrossChainTx tx = crossChainTxStorageService.getTx(chainId, hash);
            Assert.assertEquals(hash, tx.getTx().getHash());
            //test removeTx
            crossChainTxStorageService.removeTx(chainId, hash);
            tx = crossChainTxStorageService.getTx(chainId, hash);
            Assert.assertNull(tx);
        }
    }

}