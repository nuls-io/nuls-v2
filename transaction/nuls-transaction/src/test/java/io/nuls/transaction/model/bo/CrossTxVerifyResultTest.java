package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.constant.TxConstant;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CrossTxVerifyResultTest {

    private CrossTxVerifyResult obj = null;
    private byte[] bytes;

    @Before
    public void setUp() throws Exception {
        obj = new CrossTxVerifyResult();
        obj.setChainId(TxConstant.NULS_CHAINID);
        obj.setHeight(34567890);
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
        CrossTxVerifyResult obj2 = new CrossTxVerifyResult();
        try {
            obj2.parse(new NulsByteBuffer(bytes));
            assertEquals(obj.size(), obj2.size());
            assertEquals(obj.getChainId(), obj2.getChainId());
            assertEquals(obj.getHeight(), obj2.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}