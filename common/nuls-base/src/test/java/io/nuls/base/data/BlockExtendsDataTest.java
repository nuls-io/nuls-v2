package io.nuls.base.data;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import org.junit.Test;

import java.io.IOException;

public class BlockExtendsDataTest {

    @Test
    public void test() throws NulsException, IOException {
        String string = "010000000100010000000100010001003c64002056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421";
        BlockExtendsData data = new BlockExtendsData();
        data.parse(new NulsByteBuffer(HexUtil.decode(string)));
//        System.out.println(data.getMainVersion());
        data.setConsensusMemberCount(1);
        data.setPackingIndexOfRound(1);
        data.setRoundIndex(1);
        data.setRoundStartTime(1L);
        data.setMainVersion((short)1);
        data.setBlockVersion((short) 1);
        data.setStateRoot(HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"));
        data.setContinuousIntervalCount((short) 100);
        data.setEffectiveRatio((byte) 60);
        System.out.println(HexUtil.encode(data.serialize()));
    }
}