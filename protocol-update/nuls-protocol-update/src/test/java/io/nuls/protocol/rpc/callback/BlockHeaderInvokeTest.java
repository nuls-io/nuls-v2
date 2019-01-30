package io.nuls.protocol.rpc.callback;

import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.utils.ConfigLoader;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.Before;
import org.junit.Test;

import static io.nuls.protocol.constant.Constant.*;

public class BlockHeaderInvokeTest {

    @Before
    public void setUp() throws Exception {
        SpringLiteContext.init(DEFAULT_SCAN_PACKAGE);
        RocksDBService.init(DATA_PATH);
        RocksDBService.createTable(PROTOCOL_CONFIG);
        ConfigLoader.load();
    }

    @Test
    public void callBack() {
        BlockHeaderInvoke invoke = new BlockHeaderInvoke();
        for (int i = 0; i < 1000; i++) {
            invoke.callBack(new Response());
        }
    }
}