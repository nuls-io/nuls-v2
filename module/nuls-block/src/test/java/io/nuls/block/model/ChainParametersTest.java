package io.nuls.block.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.exception.NulsException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ChainParametersTest {

    @Test
    public void name() throws IOException, NulsException {
        ChainParameters parameters = new ChainParameters("nuls2", 2, 1, 5242880, 18000, 3, 1000, 1000, 1000, 1000, 60, 0, 10, 1024, 60000, 10, 10, "INFO", 15000, 60000, 10, 1000, 5000, 2, "", 20971520);
        byte[] bytes = parameters.serialize();

        ChainParameters p = new ChainParameters();
        p.parse(new NulsByteBuffer(bytes));
        System.out.println(p.getGenesisBlockPath());
        System.out.println(p.getCachedBlockSizeLimit());
    }
}