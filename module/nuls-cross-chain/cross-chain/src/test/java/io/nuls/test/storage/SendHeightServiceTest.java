package io.nuls.test.storage;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.crosschain.nuls.CrossChainBootStrap;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPO;
import io.nuls.crosschain.nuls.srorage.SendHeightService;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendHeightServiceTest {
    private static SendHeightService sendHeightService;
    private int chainId = 2;
    private int height = 10;

    @BeforeClass
    public static void beforeTest() {
        CrossChainBootStrap.main(null);
        CrossChainBootStrap accountBootstrap = SpringLiteContext.getBean(CrossChainBootStrap.class);
        //初始化配置
        accountBootstrap.init();
        //启动时间同步线程
        sendHeightService = SpringLiteContext.getBean(SendHeightService.class);
    }

    @Test
    public void saveTest(){
        SendCtxHashPO po = new SendCtxHashPO();
        List<NulsHash> hashList = new ArrayList<>();
        for(int i=1;i<=5;i++){
            Transaction tx = new Transaction();
            tx.setTime(System.currentTimeMillis()/1000);
            tx.setType(i);
            tx.setRemark(HexUtil.decode("ABCDEFG"));
            NulsHash hash = tx.getHash();
            System.out.println(i+":"+hash.toHex());
            hashList.add(tx.getHash());
        }
        po.setHashList(hashList);
        System.out.println(sendHeightService.save(height, po, chainId));
    }

    @Test
    public void getTest(){
        SendCtxHashPO po = sendHeightService.get(height, chainId);
        for (NulsHash hash:po.getHashList()) {
            System.out.println(hash.toHex());
        }
    }

    @Test
    public void delete(){
        System.out.println(sendHeightService.delete(height, chainId));
    }

    @Test
    public void getList(){
        Map<Long , SendCtxHashPO> map = sendHeightService.getList(chainId);
        for (Map.Entry<Long , SendCtxHashPO> value:map.entrySet()) {
            for (NulsHash hash:value.getValue().getHashList()) {
                System.out.println(value.getKey()+":"+hash.toHex());
            }
        }
    }
}
