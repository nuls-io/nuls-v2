package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.init.TransactionBootStrap;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxData;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.rpc.call.AccountCall;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CrossChainTxStorageServiceTest {

    protected static CrossChainTxStorageService crossChainTxStorageService;
    protected int chainId = 12345;
    protected int assetsId = 1;
    protected String nodeId = "1001";

    @BeforeClass
    public static void beforeTest() {
        //初始化数据库配置文件
        TransactionBootStrap.initDB();
        //初始化上下文
        SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        crossChainTxStorageService = SpringLiteContext.getBean(CrossChainTxStorageService.class);
    }

    @Test
    public void putTx() throws IOException {
        CrossChainTx ctx = new CrossChainTx();
        Transaction tx = new Transaction(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
        CrossTxData txData = new CrossTxData();
        txData.setChainId(chainId);
        //txData.setOriginalTxHash(originalTxHash);
        tx.setRemark(StringUtils.bytes("ctx remark"));
        //tx.setTxData(txData.serialize());
        //tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        ctx.setTx(tx);
        ctx.setSenderChainId(chainId);
        ctx.setSenderNodeId(nodeId);
        ctx.setState(TxConstant.CTX_UNPROCESSED_0);
        boolean result = crossChainTxStorageService.putTx(chainId, ctx);
        Assert.assertTrue(result);
    }

}