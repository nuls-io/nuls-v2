package io.nuls.block.message;

import io.nuls.base.data.NulsHash;
import io.nuls.core.crypto.HexUtil;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class BlockMessageTest {

    @Test
    public void name() throws IOException {
        BlockMessage message = new BlockMessage();
        message.setBlock(null);
        message.setSyn(false);
        message.setRequestHash(NulsHash.calcHash("123".getBytes()));

        System.out.println(HexUtil.encode(message.serialize()));
    }
}