package io.nuls.protocol.rpc.callback;

import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.utils.ConfigLoader;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    public void callBack() throws IOException {
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(12345);
        for (int i = 1; i <= 1000; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setUpgrade(false);
            data.setMainVersion((short) 1);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
        for (int i = 1001; i <= 10000; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setUpgrade(true);
            data.setMainVersion((short) 1);
            data.setBlockVersion((short) 2);
            data.setInterval((short) 100);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
    }

    public Response response(Object responseData) {
        Response response = MessageUtil.newResponse("", Constants.BOOLEAN_TRUE, "Congratulations! Processing completed！");
        response.setResponseData(responseData);
        return response;
    }
}