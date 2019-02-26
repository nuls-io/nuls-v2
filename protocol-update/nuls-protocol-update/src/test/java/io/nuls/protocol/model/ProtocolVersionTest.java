package io.nuls.protocol.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ProtocolVersionTest {

    @Test
    public void testSerialize() throws IOException, NulsException {
        ProtocolVersion version1 = new ProtocolVersion();
        version1.setVersion((byte) 1);
        version1.setEffectiveRatio((byte) 85);
        version1.setContinuousIntervalCount((short) 260);
        String hex = HexUtil.encode(version1.serialize());
        ProtocolVersion version2 = new ProtocolVersion();
        version2.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        assertEquals(version1.getVersion(), version2.getVersion());
        assertEquals(version1.getEffectiveRatio(), version2.getEffectiveRatio());
        assertEquals(version1.getContinuousIntervalCount(), version2.getContinuousIntervalCount());
        assertEquals(version1, version2);
    }

    @Test
    public void testEquals() {
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

        assertNotEquals(version1.getVersion(), version2.getVersion());
        assertEquals(version1.getEffectiveRatio(), version2.getEffectiveRatio());
        assertEquals(version1.getContinuousIntervalCount(), version2.getContinuousIntervalCount());
        assertNotEquals(version1, version2);
        assertNotEquals(version2, version3);
    }
}