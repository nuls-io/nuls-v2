package io.nuls.base.protocol;

import io.nuls.core.io.IoUtils;
import org.junit.Test;

public class ProtocolLoaderTest {

    @Test
    public void name() throws Exception {
        String read = IoUtils.read("protocol-config.json");
        ProtocolLoader.load(2, read);
    }
}