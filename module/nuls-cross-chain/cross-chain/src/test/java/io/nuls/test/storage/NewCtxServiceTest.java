package io.nuls.test.storage;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.crosschain.nuls.CrossChainBootStrap;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class NewCtxServiceTest {
    private static NewCtxService newCtxService;
    private int chainId = 2;
    @BeforeClass
    public static void beforeTest() {
        CrossChainBootStrap.main(null);
        CrossChainBootStrap accountBootstrap = SpringLiteContext.getBean(CrossChainBootStrap.class);
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
            NulsHash hash = tx.getHash();
            System.out.println(i+":"+hash.toHex());
            newCtxService.save(hash, tx, chainId);
        }
    }

    @Test
    public void getTest()throws Exception{
        NulsHash hash = NulsHash.fromHex("5f4fa928f35026128eb560b2537099fbe4b4ca2962e98958e232b2117bf19d1d");
        Transaction tx = newCtxService.get(hash, chainId);
        System.out.println(tx.getType());
        System.out.println(HexUtil.encode(tx.getRemark()));
    }

    @Test
    public void delete(){
        NulsHash hash = new NulsHash();
        System.out.println(newCtxService.delete(hash, chainId));
    }

    @Test
    public void getList(){
        for (Transaction tx:newCtxService.getList(chainId)) {
            System.out.println(tx.getHash().toHex());
            System.out.println(tx.getType());
            System.out.println(HexUtil.encode(tx.getRemark()));
        }
    }
}
