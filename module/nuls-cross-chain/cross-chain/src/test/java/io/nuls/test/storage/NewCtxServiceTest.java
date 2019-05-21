package io.nuls.test.storage;

import io.nuls.base.data.Transaction;
import io.nuls.crosschain.nuls.NulsCrossChainBootStrap;
import io.nuls.crosschain.nuls.srorage.NewCtxService;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class NewCtxServiceTest {
    private static NewCtxService newCtxService;
    private int chainId = 2;
    @BeforeClass
    public static void beforeTest() {
        NulsCrossChainBootStrap.main(null);
        NulsCrossChainBootStrap accountBootstrap = SpringLiteContext.getBean(NulsCrossChainBootStrap.class);
        //初始化配置
        accountBootstrap.init();
        //启动时间同步线程
        newCtxService = SpringLiteContext.getBean(NewCtxService.class);
    }

    @Test
    public void saveTest(){
        for(int i=1;i<=5;i++){
            Transaction tx = new Transaction();
            tx.setTime(System.currentTimeMillis()/1000);
            tx.setType(i);
            tx.setRemark(HexUtil.decode("ABCDEFG"));
            NulsDigestData hash = tx.getHash();
            System.out.println(i+":"+hash.getDigestHex());
            newCtxService.save(hash, tx, chainId);
        }
    }

    @Test
    public void getTest()throws Exception{
        NulsDigestData hash = new NulsDigestData();
        hash.parse(HexUtil.decode("5f4fa928f35026128eb560b2537099fbe4b4ca2962e98958e232b2117bf19d1d"),0);
        Transaction tx = newCtxService.get(hash, chainId);
        System.out.println(tx.getType());
        System.out.println(HexUtil.encode(tx.getRemark()));
    }

    @Test
    public void delete(){
        NulsDigestData hash = new NulsDigestData();
        System.out.println(newCtxService.delete(hash, chainId));
    }

    @Test
    public void getList(){
        for (Transaction tx:newCtxService.getList(chainId)) {
            System.out.println(tx.getHash().getDigestHex());
            System.out.println(tx.getType());
            System.out.println(HexUtil.encode(tx.getRemark()));
        }
    }
}
