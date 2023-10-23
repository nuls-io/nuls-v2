package io.nuls.block.message.handler;

import io.nuls.base.RPCUtil;
import io.nuls.block.message.SmallBlockMessage;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SmallBlockHandlerTest {

    @Test
    public void name() throws IOException {
        String s = Files.readString(Path.of("C:\\Users\\alvin\\Desktop", "string.txt"));
        SmallBlockMessage message = RPCUtil.getInstanceRpcStr(s, SmallBlockMessage.class);
        System.out.println(message.size());
    }
}