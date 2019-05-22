package io.nuls.test.storage;

import io.nuls.base.data.Transaction;
import io.nuls.core.parse.HashUtil;
import io.nuls.crosschain.nuls.CrossChainBootStrap;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPo;
import io.nuls.crosschain.nuls.srorage.SendHeightService;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.print.attribute.HashAttributeSet;
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
    public void saveTest() {
        SendCtxHashPo po = new SendCtxHashPo();
        List<byte[]> hashList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Transaction tx = new Transaction();
            tx.setTime(System.currentTimeMillis() / 1000);
            tx.setType(i);
            tx.setRemark(HexUtil.decode("ABCDEFG"));
            byte[] hash = tx.getHash();
            System.out.println(i + ":" + HashUtil.toHex(hash));
            hashList.add(tx.getHash());
        }
        po.setHashList(hashList);
        System.out.println(sendHeightService.save(height, po, chainId));
    }

    @Test
    public void getTest() {
        SendCtxHashPo po = sendHeightService.get(height, chainId);
        for (byte[] hash : po.getHashList()) {
            System.out.println(HashUtil.toHex(hash));
        }
    }

    @Test
    public void delete() {
        System.out.println(sendHeightService.delete(height, chainId));
    }

    @Test
    public void getList() {
        Map<Long, SendCtxHashPo> map = sendHeightService.getList(chainId);
        for (Map.Entry<Long, SendCtxHashPo> value : map.entrySet()) {
            for (byte[] hash : value.getValue().getHashList()) {
                System.out.println(value.getKey() + ":" + HashUtil.toHex(hash));
            }
        }
    }
}
