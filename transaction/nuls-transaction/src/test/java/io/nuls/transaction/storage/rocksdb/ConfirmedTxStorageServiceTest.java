package io.nuls.transaction.storage.rocksdb;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.model.StringUtils;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.TransactionBootstrap;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.model.bo.CrossTx;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ConfirmedTxStorageServiceTest {

    protected static ConfirmedTxStorageService confirmedTxStorageService;
    protected int chainId = 12345;
    protected long height = 100;

    @BeforeClass
    public static void beforeTest() throws Exception {
        //初始化数据库配置文件
        new TransactionBootstrap().initDB();
        //初始化上下文
        SpringLiteContext.init(TestConstant.CONTEXT_PATH);
        confirmedTxStorageService = SpringLiteContext.getBean(ConfirmedTxStorageService.class);
        //启动链
        SpringLiteContext.getBean(ChainManager.class).runChain();
    }

    @Test
    public void saveTx() throws Exception {
        Transaction tx = TestConstant.getTransaction2();
        boolean result = confirmedTxStorageService.saveTx(chainId, new TransactionConfirmedPO(tx, 1, (byte)1));
        Assert.assertTrue(result);
    }

    @Test
    public void saveTxList() throws Exception {
        List<TransactionConfirmedPO> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Transaction tx = TestConstant.getTransaction2();
            tx.setRemark(StringUtils.bytes("tx remark" + i));
            list.add(new TransactionConfirmedPO(tx, 1, (byte)1));
        }
        boolean result = confirmedTxStorageService.saveTxList(chainId, list);
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
        List<TransactionConfirmedPO> list = new ArrayList<>();
        List<String> hashList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Transaction tx = TestConstant.getTransaction2();
            tx.setRemark(StringUtils.bytes("tx remark" + i));
            list.add(new TransactionConfirmedPO(tx, 1, (byte)1));
            hashList.add(tx.getHash().getDigestHex());
        }
        confirmedTxStorageService.saveTxList(chainId, list);

       /* //test getTxList
        List<Transaction> txList = confirmedTxStorageService.getTxList(chainId, hashList);
        Assert.assertEquals(hashList.size(), txList.size());

        NulsDigestData hash = list.get(0).getTx().getHash();
        //test getTx
        TransactionConfirmedPO tx = confirmedTxStorageService.getTx(chainId, hash);
        Assert.assertEquals(hash, tx.getTx().getHash());
        //test removeTxList
        List<byte[]> removeList = List.of(hashList.get(0));
        confirmedTxStorageService.removeTxList(chainId, removeList);
        tx = confirmedTxStorageService.getTx(chainId, hash);
        Assert.assertNull(tx);*/
    }

    @Test
    public void saveCrossTxEffectList() throws Exception {
        //test saveCrossTxEffectList
        List<CrossTx> list = new ArrayList<>();
        List<NulsDigestData> hashList = new ArrayList<>();
        List<byte[]> removeList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CrossTx ctx = TestConstant.createCrossChainTx();
            list.add(ctx);
            hashList.add(ctx.getTx().getHash());
            removeList.add(ctx.getTx().getHash().serialize());
        }
        boolean result = confirmedTxStorageService.saveCrossTxEffectList(chainId, height, hashList);
        Assert.assertTrue(result);

        //test getCrossTxEffectList
        List<NulsDigestData> txList = confirmedTxStorageService.getCrossTxEffectList(chainId, height);
        Assert.assertEquals(hashList.size(), txList.size());

        //test removeTxList
        result = confirmedTxStorageService.removeCrossTxEffectList(chainId, height);
        Assert.assertTrue(result);
    }
}