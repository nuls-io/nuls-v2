package io.nuls.tools.protocol;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ProtocolLoaderTest {

    @Test
    public void load() throws Exception {
        ProtocolLoader.load(1, Files.readString(Paths.get("C:\\Users\\alvin\\Desktop\\sample.json")));
    }
}