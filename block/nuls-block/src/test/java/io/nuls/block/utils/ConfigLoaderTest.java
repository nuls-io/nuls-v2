package io.nuls.block.utils;

import io.nuls.tools.io.IoUtils;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.protocol.Protocol;
import io.nuls.tools.protocol.ProtocolConfigJson;
import io.nuls.tools.protocol.ProtocolLoader;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.Constant.PROTOCOL_CONFIG_COMPARATOR;
import static io.nuls.block.constant.Constant.PROTOCOL_CONFIG_FILE;
import static org.junit.Assert.assertEquals;

public class ConfigLoaderTest {


    @Test
    public void name() throws Exception {
        String json = IoUtils.read(PROTOCOL_CONFIG_FILE);
        List<ProtocolConfigJson> protocolConfigs = JSONUtils.json2list(json, ProtocolConfigJson.class);
        protocolConfigs.sort(PROTOCOL_CONFIG_COMPARATOR);
        Map<Short, Protocol> protocolMap = ProtocolLoader.load(protocolConfigs);
        Protocol v1 = protocolMap.get(1);
        assertEquals(3, v1.getAllowMsg().size());
        assertEquals(3, protocolMap.get(2).getAllowMsg().size());
        assertEquals(2, protocolMap.get(3).getAllowMsg().size());
        assertEquals(3, v1.getAllowTx().size());
        assertEquals(3, protocolMap.get(2).getAllowTx().size());
        assertEquals(2, protocolMap.get(3).getAllowTx().size());
    }

}