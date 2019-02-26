package io.nuls.protocol.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StatisticsTest {

    @Test
    public void testSerialize() throws IOException, NulsException {
        Statistics statistics1 = new Statistics();
        statistics1.setCount((short) 88);
        statistics1.setHeight(888);
        statistics1.setLastHeight(777);

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
        statistics1.setProtocolVersion(version1);
        statistics1.setProtocolVersionMap(map);
        String hex = HexUtil.encode(statistics1.serialize());
        Statistics statistics2 = new Statistics();
        statistics2.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        assertEquals(statistics2, statistics1);
        assertEquals(statistics2.getProtocolVersion(), statistics1.getProtocolVersion());
        assertEquals(statistics2.getProtocolVersionMap().get(version2), statistics1.getProtocolVersionMap().get(version2));
        assertEquals(111, statistics2.getProtocolVersionMap().get(version2).intValue());
    }

}