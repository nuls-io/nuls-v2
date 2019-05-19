package io.nuls.protocol.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.ProtocolVersion;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StatisticsInfoTest {

    @Test
    public void testSerialize() throws IOException, NulsException {
        StatisticsInfo statisticsInfo1 = new StatisticsInfo();
        statisticsInfo1.setCount((short) 88);
        statisticsInfo1.setHeight(888);
        statisticsInfo1.setLastHeight(777);

        ProtocolVersion version1 = new ProtocolVersion();
        version1.setVersion((byte) 1);
        version1.setEffectiveRatio((byte) 85);
        version1.setContinuousIntervalCount((short) 260);

        ProtocolVersion version2 = new ProtocolVersion();
        version2.setVersion((byte) 2);
        version2.setEffectiveRatio((byte) 85);
        version2.setContinuousIntervalCount((short) 260);

        ProtocolVersion version3 = new ProtocolVersion();
        version3.setVersion((byte) 2);
        version3.setEffectiveRatio((byte) 85);
        version3.setContinuousIntervalCount((short) 222);

        Map<ProtocolVersion, Integer> map = new HashMap<>();
        map.put(version1, 888);
        map.put(version2, 111);
        map.put(version3, 1);
        statisticsInfo1.setProtocolVersion(version1);
        statisticsInfo1.setProtocolVersionMap(map);
        String hex = HexUtil.encode(statisticsInfo1.serialize());
        StatisticsInfo statisticsInfo2 = new StatisticsInfo();
        statisticsInfo2.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        assertNotEquals(statisticsInfo2, statisticsInfo1);
        assertEquals(statisticsInfo2.getProtocolVersion(), statisticsInfo1.getProtocolVersion());
        assertEquals(statisticsInfo2.getProtocolVersionMap().get(version2), statisticsInfo1.getProtocolVersionMap().get(version2));
        assertEquals(111, statisticsInfo2.getProtocolVersionMap().get(version2).intValue());
        assertEquals(888, statisticsInfo2.getProtocolVersionMap().get(version1).intValue());
        assertEquals(1, statisticsInfo2.getProtocolVersionMap().get(version3).intValue());
    }

}