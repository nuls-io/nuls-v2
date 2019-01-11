package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.Transaction;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.init.TransactionBootStrap;
import io.nuls.transaction.manager.ChainManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TxVerifiedStorageServiceTest {

    protected static TxVerifiedStorageService txVerifiedStorageService;
    protected int chainId = 12345;

    @BeforeClass
    public static void beforeTest() throws Exception {
        //初始化数据库配置文件
        TransactionBootStrap.initDB();
        //初始化上下文
        SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        txVerifiedStorageService = SpringLiteContext.getBean(TxVerifiedStorageService.class);
        //启动链
        SpringLiteContext.getBean(ChainManager.class).runChain();
    }

    @Test
    public void putTx() throws Exception {
        Transaction tx = TestConstant.getTransaction2();
        boolean result = txVerifiedStorageService.putTx(chainId, tx);
        Assert.assertTrue(result);
    }

    @Test
    public void removeTx() throws Exception {
        Transaction tx = TestConstant.getTransaction2();
        boolean result = txVerifiedStorageService.putTx(chainId, tx);
        Assert.assertTrue(result);

        Transaction txResult = txVerifiedStorageService.getTx(chainId, tx.getHash());
        Assert.assertEquals(tx.getHash(), txResult.getHash());

        txVerifiedStorageService.removeTx(chainId, txResult.getHash());

        txResult = txVerifiedStorageService.getTx(chainId, tx.getHash());
        Assert.assertNull(txResult);
    }

    @Test
    public void removeListTx() throws Exception {
        Transaction tx = TestConstant.getTransaction2();
        boolean result = txVerifiedStorageService.putTx(chainId, tx);
        Assert.assertTrue(result);

        //test getTxList
        List<byte[]> hashList = List.of(tx.getHash().serialize());
        List<Transaction> txList = txVerifiedStorageService.getTxList(chainId, hashList);
        Assert.assertEquals(hashList.size(), txList.size());

        result = txVerifiedStorageService.removeTxList(chainId, hashList);
        Assert.assertTrue(result);

        txList = txVerifiedStorageService.getTxList(chainId, hashList);
        Assert.assertTrue(txList.size() == 0);
    }

}