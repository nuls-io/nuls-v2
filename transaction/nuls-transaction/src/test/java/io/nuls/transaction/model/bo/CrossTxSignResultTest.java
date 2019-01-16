package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.transaction.TestConstant;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CrossTxSignResultTest {

    private CrossTxSignResult obj = null;
    private byte[] bytes;

    @Before
    public void setUp() throws Exception {
        obj = new CrossTxSignResult();
        obj.setPackingAddress(TestConstant.address1);
        obj.setSignature(TestConstant.getP2PHKSignature());
        obj.setNodeId("1");
    }

    @Test
    public void serializeToStream() {
        try {
            bytes = obj.serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void parse() {
        if (null == bytes) {
            serializeToStream();
        }
        CrossTxSignResult obj2 = new CrossTxSignResult();
        try {
            obj2.parse(new NulsByteBuffer(bytes));
            assertTrue(obj.getPackingAddress().equals(obj2.getPackingAddress()));
            assertTrue(obj.getNodeId().equals(obj2.getNodeId()));
            assertTrue(Arrays.equals(obj.getSignature().serialize(), obj2.getSignature().serialize()));
            assertEquals(obj.size(), obj2.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}