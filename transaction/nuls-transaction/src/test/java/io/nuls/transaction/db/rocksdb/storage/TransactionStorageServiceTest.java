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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TransactionStorageServiceTest {

    protected static TransactionStorageService transactionStorageService;
    protected int chainId = 12345;
    protected long height = 100;

    @BeforeClass
    public static void beforeTest() throws Exception {
        //初始化数据库配置文件
        TransactionBootStrap.initDB();
        //初始化上下文
        SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        transactionStorageService = SpringLiteContext.getBean(TransactionStorageService.class);
        //启动链
        SpringLiteContext.getBean(ChainManager.class).runChain();
    }

    @Test
    public void saveTx() throws Exception {
        Transaction tx = TestConstant.getTransaction2();
        boolean result = transactionStorageService.saveTx(chainId, tx);
        Assert.assertTrue(result);
    }

    @Test
    public void saveTxList() throws Exception {
        List<Transaction> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Transaction tx = TestConstant.getTransaction2();
            tx.setRemark(StringUtils.bytes("tx remark" + i));
            list.add(tx);
        }
        boolean result = transactionStorageService.saveTxList(chainId, list);
        Assert.assertTrue(result);
    }

    /**
     * 测试交易批量保存、批量查询、批量删除、单个查询
     *
     * @throws Exception
     */
    @Test
    public void getTxList() throws Exception {
        //test saveTxList
        List<Transaction> list = new ArrayList<>();
        List<byte[]> hashList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Transaction tx = TestConstant.getTransaction2();
            tx.setRemark(StringUtils.bytes("tx remark" + i));
            list.add(tx);
            hashList.add(tx.getHash().serialize());
        }
        transactionStorageService.saveTxList(chainId, list);

        //test getTxList
        List<Transaction> txList = transactionStorageService.getTxList(chainId, hashList);
        Assert.assertEquals(hashList.size(), txList.size());

        NulsDigestData hash = list.get(0).getHash();
        //test getTx
        Transaction tx = transactionStorageService.getTx(chainId, hash);
        Assert.assertEquals(hash, tx.getHash());
        //test removeTxList
        List<byte[]> removeList = List.of(hashList.get(0));
        transactionStorageService.removeTxList(chainId, removeList);
        tx = transactionStorageService.getTx(chainId, hash);
        Assert.assertNull(tx);
    }

    @Test
    public void saveCrossTxEffectList() throws Exception {
        //test saveCrossTxEffectList
        List<CrossChainTx> list = new ArrayList<>();
        List<NulsDigestData> hashList = new ArrayList<>();
        List<byte[]> removeList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CrossChainTx ctx = TestConstant.createCrossChainTx();
            list.add(ctx);
            hashList.add(ctx.getTx().getHash());
            removeList.add(ctx.getTx().getHash().serialize());
        }
        boolean result = transactionStorageService.saveCrossTxEffectList(chainId, height, hashList);
        Assert.assertTrue(result);

        //test getCrossTxEffectList
        List<NulsDigestData> txList = transactionStorageService.getCrossTxEffectList(chainId, height);
        Assert.assertEquals(hashList.size(), txList.size());

        //test removeTxList
        result = transactionStorageService.removeCrossTxEffectList(chainId, height);
        Assert.assertTrue(result);
    }
}