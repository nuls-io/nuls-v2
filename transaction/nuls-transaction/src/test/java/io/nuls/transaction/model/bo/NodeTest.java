package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.constant.TxConstant;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class NodeTest {

    private Node obj = null;
    private byte[] bytes;

    @Before
    public void setUp() throws Exception {
        obj = new Node();
        obj.setId("192.168.1.110:8090");
        obj.setHeight(34567890);
        obj.setHash(TestConstant.getHashA());
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
        Node obj2 = new Node();
        try {
            obj2.parse(new NulsByteBuffer(bytes));
            assertEquals(obj.size(), obj2.size());
            assertTrue(obj.getId().equals(obj2.getId()));
            assertTrue(obj.getHash().equals(obj2.getHash()));
            assertEquals(obj.getHeight(), obj2.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}