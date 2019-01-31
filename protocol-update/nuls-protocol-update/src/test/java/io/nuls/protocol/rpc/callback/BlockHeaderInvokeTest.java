package io.nuls.protocol.rpc.callback;

import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.utils.ConfigLoader;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static io.nuls.protocol.constant.Constant.*;

public class BlockHeaderInvokeTest {

    private int chainId= 12345;

    @Before
    public void setUp() throws Exception {
        SpringLiteContext.init(DEFAULT_SCAN_PACKAGE);
        RocksDBService.init(DATA_PATH);
        RocksDBService.createTable(PROTOCOL_CONFIG);
        ConfigLoader.load();
    }

    /**
     * 测试连续升级(中途统计没有波动，没有跨版本升级)
     *
     * @throws IOException
     */
    @Test
    public void test1() throws IOException {
        ProtocolContext context = ContextManager.getContext(chainId);
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(chainId);
        //模拟V1持续运行一段时间
        for (int i = 1; i <= 1888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setUpgrade(false);
            data.setMainVersion((short) 1);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
        //V1-->V2,并且V2持续运行一段时间
        for (int i = 1889; i <= 18888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (version == 1) {
                data.setUpgrade(true);
                data.setMainVersion(version);
                data.setBlockVersion((short) 2);
                data.setInterval((short) 100);
                data.setEffectiveRatio((byte) 80);
                data.setContinuousIntervalCount((short) 10);
            } else {
                data.setUpgrade(false);
                data.setMainVersion(version);
            }
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 1900) {
                assertEquals(1, version);
                assertEquals(19, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2000) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 5000) {
                assertEquals(2, version);
            }
        }
        //V2-->V3,并且V3持续运行一段时间
        for (int i = 18889; i <= 28888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (version == 2) {
                data.setUpgrade(true);
                data.setMainVersion(version);
                data.setBlockVersion((short) 3);
                data.setInterval((short) 100);
                data.setEffectiveRatio((byte) 90);
                data.setContinuousIntervalCount((short) 20);
            } else {
                data.setUpgrade(false);
                data.setMainVersion(version);
            }
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 18900) {
                assertEquals(2, version);
                assertEquals(0, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 19000) {
                assertEquals(2, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(3, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 22000) {
                assertEquals(3, version);
            }
        }
    }

    /**
     * 测试连续升级(中途统计有波动，没有跨版本升级)
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void test2() throws IOException {
        ProtocolContext context = ContextManager.getContext(chainId);
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(chainId);
        //模拟V1持续运行一段时间
        for (int i = 1; i <= 1888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setUpgrade(false);
            data.setMainVersion((short) 1);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
        //V1-->V2,并且V2持续运行一段时间
        for (int i = 1889; i <= 18888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (i >= 2111 && i <=2256) {
                data.setUpgrade(false);
                data.setMainVersion((short) 1);
            } else {
                if (version == 1) {
                    data.setUpgrade(true);
                    data.setMainVersion(version);
                    data.setBlockVersion((short) 2);
                    data.setInterval((short) 100);
                    data.setEffectiveRatio((byte) 80);
                    data.setContinuousIntervalCount((short) 10);
                } else {
                    data.setUpgrade(false);
                    data.setMainVersion(version);
                }
            }

            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 1900) {
                assertEquals(1, version);
                assertEquals(19, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2000) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2200) {
                assertEquals(1, version);
                assertEquals(20, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2400) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 5000) {
                assertEquals(2, version);
            }
        }
        //V2-->V3,并且V3持续运行一段时间
        for (int i = 18889; i <= 28888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (version == 2) {
                data.setUpgrade(true);
                data.setMainVersion(version);
                data.setBlockVersion((short) 3);
                data.setInterval((short) 100);
                data.setEffectiveRatio((byte) 90);
                data.setContinuousIntervalCount((short) 20);
            } else {
                data.setUpgrade(false);
                data.setMainVersion(version);
            }
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 18900) {
                assertEquals(2, version);
                assertEquals(0, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 19000) {
                assertEquals(2, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(3, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 22000) {
                assertEquals(3, version);
            }
        }
    }

    /**
     * 测试连续升级(中途统计没有波动，有跨版本升级)
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void test3() throws IOException {
        ProtocolContext context = ContextManager.getContext(chainId);
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(chainId);
        //模拟V1持续运行一段时间
        for (int i = 1; i <= 1888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setUpgrade(false);
            data.setMainVersion((short) 1);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
        //V1-->V2,V2连续确认数不足，V3介入，V3升级成功
        for (int i = 1889; i <= 2333; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (version == 1) {
                data.setUpgrade(true);
                data.setMainVersion(version);
                data.setBlockVersion((short) 2);
                data.setInterval((short) 100);
                data.setEffectiveRatio((byte) 80);
                data.setContinuousIntervalCount((short) 10);
            } else {
                data.setUpgrade(false);
                data.setMainVersion(version);
            }
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 1900) {
                assertEquals(1, version);
                assertEquals(19, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2000) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
        }
        //V2-->V3,并且V3持续运行一段时间
        for (int i = 2334; i <= 28888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (version == 1) {
                data.setUpgrade(true);
                data.setMainVersion(version);
                data.setBlockVersion((short) 3);
                data.setInterval((short) 100);
                data.setEffectiveRatio((byte) 90);
                data.setContinuousIntervalCount((short) 20);
            } else {
                data.setUpgrade(false);
                data.setMainVersion(version);
            }
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 18900) {
                assertEquals(1, version);
                assertEquals(0, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 19000) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(3, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 22000) {
                assertEquals(3, version);
            }
        }
    }

    /**
     * 测试连续升级(中途统计有波动，有跨版本升级)
     *
     * @throws IOException
     */
    @Test
    @Ignore
    public void test4() throws IOException {
        ProtocolContext context = ContextManager.getContext(chainId);
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(chainId);
        //模拟V1持续运行一段时间
        for (int i = 1; i <= 1888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setUpgrade(false);
            data.setMainVersion((short) 1);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
        //V1-->V2,V2统计由波动，并且V2持续运行一段时间
        for (int i = 1889; i <= 18888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (i >= 2111 && i <=2256) {
                data.setUpgrade(false);
                data.setMainVersion((short) 1);
            } else {
                if (version == 1) {
                    data.setUpgrade(true);
                    data.setMainVersion(version);
                    data.setBlockVersion((short) 2);
                    data.setInterval((short) 100);
                    data.setEffectiveRatio((byte) 80);
                    data.setContinuousIntervalCount((short) 10);
                } else {
                    data.setUpgrade(false);
                    data.setMainVersion(version);
                }
            }

            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 1900) {
                assertEquals(1, version);
                assertEquals(19, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2000) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2200) {
                assertEquals(1, version);
                assertEquals(20, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2400) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 5000) {
                assertEquals(2, version);
            }
        }
        //V2-->V3,V3连续确认数不足，V4介入，V4升级成功
        for (int i = 18889; i <= 19222; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (version == 1) {
                data.setUpgrade(true);
                data.setMainVersion(version);
                data.setBlockVersion((short) 2);
                data.setInterval((short) 100);
                data.setEffectiveRatio((byte) 80);
                data.setContinuousIntervalCount((short) 10);
            } else {
                data.setUpgrade(false);
                data.setMainVersion(version);
            }
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 1900) {
                assertEquals(1, version);
                assertEquals(19, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2000) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
        }
        //V2-->V3,并且V3持续运行一段时间
        for (int i = 19223; i <= 28888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (version == 1) {
                data.setUpgrade(true);
                data.setMainVersion(version);
                data.setBlockVersion((short) 3);
                data.setInterval((short) 100);
                data.setEffectiveRatio((byte) 90);
                data.setContinuousIntervalCount((short) 20);
            } else {
                data.setUpgrade(false);
                data.setMainVersion(version);
            }
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 18900) {
                assertEquals(1, version);
                assertEquals(0, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 19000) {
                assertEquals(1, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(3, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 22000) {
                assertEquals(3, version);
            }
        }
    }



    public Response response(Object responseData) {
        Response response = MessageUtil.newResponse("", Constants.BOOLEAN_TRUE, "Congratulations! Processing completed！");
        response.setResponseData(responseData);
        return response;
    }
}