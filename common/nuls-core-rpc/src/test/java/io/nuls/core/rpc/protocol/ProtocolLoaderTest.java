package io.nuls.core.rpc.protocol;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

public class ProtocolLoaderTest {

    @Test
    public void load() throws Exception {
        ProtocolLoader.load(1, Files.readString(Paths.get("D:\\inchain\\IdeaProjects\\nuls_2.0\\module\\nuls-account\\src\\main\\resources\\protocol-config.json")));
        Collection<Protocol> protocols = ProtocolGroupManager.getProtocols(1);
        Protocol protocol = ProtocolGroupManager.getCurrentProtocol(1);
        System.out.println(protocol.getAllowMsg().get(0).getProcessors());
    }

}