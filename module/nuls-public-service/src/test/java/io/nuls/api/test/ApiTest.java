package io.nuls.api.test;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.ContractInfo;
import io.nuls.api.model.po.ContractResultInfo;
import io.nuls.api.model.po.CurrentRound;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ApiTest {

//    protected Chain chain;
//    protected static int chainId = 2;
//    protected static int assetId = 1;

    @Before
    public void before() throws Exception {
//        NoUse.mockModule();
//        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
//        chain = new Chain();
//        chain.setConfig(new ConfigBean(chainId, assetId, 100000000L));
    }

    @Test
    public void testCmdCall() {
//
//        for (int i = 0; i < 10000; i++) {
//            BlockInfo block = WalletRpcHandler.getBlockInfo(2, i);
//            for (TransactionInfo tx : block.getTxList()) {
//                if (tx.getType() == 1) {
//
//                }
//            }
//        }
    }

    @Before
    public void initApiCache() {
        ApiCache apiCache = new ApiCache();
        CurrentRound currentRound = new CurrentRound();
        currentRound.setStartHeight(111);
        currentRound.setEndHeight(222);
        apiCache.setCurrentRound(currentRound);

        CacheManager.addApiCache(2, apiCache);
    }


    @Test
    public void updateCurrentRound() {
        ApiCache apiCache = CacheManager.getCache(2);
        CurrentRound currentRound = apiCache.getCurrentRound();
        System.out.println(currentRound.getStartHeight() + "----" + currentRound.getEndHeight());
        CurrentRound beforeRound = new CurrentRound();
        beforeRound.setStartHeight(3333);
        beforeRound.setEndHeight(4444);

        apiCache.setCurrentRound(beforeRound);
        System.out.println(apiCache.getCurrentRound().getStartHeight() + "----" + apiCache.getCurrentRound().getEndHeight());

        testUpdateCurrentRound(currentRound);

        System.out.println(currentRound.getStartHeight() + "----" + currentRound.getEndHeight());
    }

    private void testUpdateCurrentRound(CurrentRound currentRound) {
        currentRound.setStartHeight(7777);
        currentRound.setEndHeight(8888);
    }


    @Test
    public void testContract() {
        try {
            ContractInfo contractInfo = new ContractInfo();
            contractInfo.setCreateTxHash("0020f0b5b43fb165413938030266ebdcfb780b7a213ebddc2db8665cbfcb6a936cb5");
            contractInfo.setContractAddress("tNULSeBaNAsdgUuiYL5WCtVqb8r9gCkpw8QH86");
            Result<ContractInfo> result = WalletRpcHandler.getContractInfo(2, contractInfo);
            contractInfo = result.getData();
            Document document = contractInfo.toDocument();
            System.out.println(1);
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testContractResult() {
        try {
            Result<ContractResultInfo> result = WalletRpcHandler.getContractResultInfo(2, "0020cc10c27160b1e0c7dd8590baa16fedbb91661654b48df45ad370c47ce27cefa6");
            ContractResultInfo resultInfo = result.getData();
            Document document = resultInfo.toDocument();
            System.out.println(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test() {
        Set<String> set = new HashSet<>();
        set.add("aaaa1");
        set.add("aaaa11");
        set.add("aaaa112");
        set.add("aaaa13");
        set.add("aaaa14");
        set.add("aaaa15");
        set.add("aaaa16");
        set.add("aaaa17");
        set.add("aaaa18");
        set.add("aaaa19");
        set.add("aaaa10");

        int i = 0;
        for (String key : set) {
            System.out.println(key);
            i++;
            if (i == 6) {
                break;
            }
        }

    }
}
