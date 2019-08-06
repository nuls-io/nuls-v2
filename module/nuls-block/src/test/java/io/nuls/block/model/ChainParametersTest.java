package io.nuls.block.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.exception.NulsException;
import org.junit.Test;

import java.io.IOException;

public class ChainParametersTest {

    @Test
    public void name() throws IOException, NulsException {
        ChainParameters parameters = new ChainParameters("nuls2", 2, 1, 3, 2, 3, 1, 1, 2, 1, 0, 10, 2, 10, 10, 9, "INFO", 10, 1, 2, "", 20971520);
        byte[] bytes = parameters.serialize();

        ChainParameters p = new ChainParameters();
        p.parse(new NulsByteBuffer(bytes));
        System.out.println(p.getGenesisBlockPath());
        System.out.println(p.getCachedBlockSizeLimit());
    }
}