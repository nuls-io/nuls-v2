package io.nuls.block.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.exception.NulsException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ChainParametersTest {

    @Test
    public void name() throws IOException, NulsException {
        ChainParameters parameters = new ChainParameters(2, 1, 5242880, 1800000, (byte) 3, 1000, 1000, 1000, (byte) 60, (byte) 1, (byte) 10, 1024, 60000, (byte) 6, (byte) 10, "INFO", 15000, 5000, "", 20971520);
        byte[] bytes = parameters.serialize();

        ChainParameters p = new ChainParameters();
        p.parse(new NulsByteBuffer(bytes));

        Assert.assertEquals(parameters, p);
    }
}