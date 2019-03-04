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

import java.util.ArrayList;
import java.util.List;

public class CtxStorageServiceTest {

    protected static CtxStorageService ctxStorageService;
    protected int chainId = 12345;
    protected String nodeId = "1001";

    @BeforeClass
    public static void beforeTest() throws Exception {
        //初始化数据库配置文件
        TransactionBootStrap.initDB();
        //初始化上下文
        SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        ctxStorageService = SpringLiteContext.getBean(CtxStorageService.class);
        //启动链
        SpringLiteContext.getBean(ChainManager.class).runChain();
    }

    @Test
    public void putTx() throws Exception {
//        CrossTx ctx = new CrossTx();
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
        CrossTx ctx = TestConstant.createCrossChainTx();
        boolean result = ctxStorageService.putTx(chainId, ctx);
        Assert.assertTrue(result);
    }

    @Test
    public void putTxs() throws Exception {
        List<CrossTx> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CrossTx ctx = TestConstant.createCrossChainTx();
            list.add(ctx);
        }
        boolean result = ctxStorageService.putTxs(chainId, list);
        Assert.assertTrue(result);
    }

    @Test
    public void getTxList() {
        //test getTxList
        List<CrossTx> list = ctxStorageService.getTxList(chainId);
        if (list != null) {
            NulsDigestData hash = list.get(0).getTx().getHash();
            //test getTx
            CrossTx tx = ctxStorageService.getTx(chainId, hash);
            Assert.assertEquals(hash, tx.getTx().getHash());
            //test removeTx
            ctxStorageService.removeTx(chainId, hash);
            tx = ctxStorageService.getTx(chainId, hash);
            Assert.assertNull(tx);
        }
    }

}