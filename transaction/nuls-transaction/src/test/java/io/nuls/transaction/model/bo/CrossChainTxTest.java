package io.nuls.transaction.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.tools.exception.NulsException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class CrossChainTxTest {

    private CrossChainTx cct ;
    byte[] bytes = null;

    @Before
    public void setUp() throws Exception {
        cct = new CrossChainTx();
        cct.setState(0);
    }

    @Test
    public void serializeToStream() {
        try {
            bytes = cct.serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void parse() {
        CrossChainTx cct2 = new CrossChainTx();
        try {
            cct2.parse(new NulsByteBuffer(bytes));
        } catch (NulsException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void size() {
    }
}