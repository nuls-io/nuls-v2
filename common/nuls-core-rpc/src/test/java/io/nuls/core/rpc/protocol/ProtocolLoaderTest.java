package io.nuls.core.rpc.protocol;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ProtocolLoaderTest {

    @Test
    public void load() throws Exception {
        ProtocolLoader.load(1, Files.readString(Paths.get("C:\\Users\\alvin\\Desktop\\sample.json")));
        Protocol protocol = ProtocolGroupManager.getCurrentProtocol(1);
        System.out.println(protocol.getAllowMsg().get(0).getProcessors());
    }

}