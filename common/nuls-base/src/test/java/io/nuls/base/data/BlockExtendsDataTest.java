package io.nuls.base.data;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import org.junit.Test;

import java.io.IOException;

public class BlockExtendsDataTest {

    @Test
    public void test() throws NulsException, IOException {
        String string = "010000000100010000000000010001000100500a002056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421";
        BlockExtendsData data = new BlockExtendsData();
        data.parse(new NulsByteBuffer(HexUtil.decode(string)));
        System.out.println(data.getMainVersion());
        System.out.println(data.getBlockVersion());
        System.out.println(data.getContinuousIntervalCount());
        System.out.println(data.getEffectiveRatio());
        data.setContinuousIntervalCount((short) 100);
        data.setEffectiveRatio((byte) 60);
        System.out.println(HexUtil.encode(data.serialize()));
    }
}