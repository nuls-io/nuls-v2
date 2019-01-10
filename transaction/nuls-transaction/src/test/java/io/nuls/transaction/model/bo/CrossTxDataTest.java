package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.constant.TxConstant;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class CrossTxDataTest {

    private CrossTxData ctd;
    private byte[] bytes;
    @Before
    public void setUp() throws Exception {
        ctd = new CrossTxData();
        ctd.setOriginalTxHash(TestConstant.getHashA().getDigestBytes());
        ctd.setChainId(TxConstant.NULS_CHAINID);
//        ctd.setOriginalTxHash(TestConstant.getHashA().getDigestBytes());
//        ctd.setChainId(0);
    }

    @Test
    public void serializeToStream() {
        try {
            bytes = ctd.serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void parse() {
        if(null == bytes){
            serializeToStream();
        }
        CrossTxData ctd2 = new CrossTxData();
        try {
            ctd2.parse(new NulsByteBuffer(bytes));
        } catch (NulsException e) {
            e.printStackTrace();
        }
        assertEquals(ctd.getChainId(), ctd2.getChainId());
        assertTrue(Arrays.equals(ctd.getOriginalTxHash(), ctd2.getOriginalTxHash()));
        assertEquals(ctd.size(), ctd2.size());
    }
}