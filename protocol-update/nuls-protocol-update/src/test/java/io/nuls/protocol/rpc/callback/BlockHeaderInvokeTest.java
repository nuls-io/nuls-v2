package io.nuls.protocol.rpc.callback;

import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.constant.Constant;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.utils.ConfigLoader;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Stack;

import static io.nuls.protocol.constant.Constant.*;
import static org.junit.Assert.assertEquals;

public class BlockHeaderInvokeTest {

    private static int chainId = 12345;

    @BeforeClass
    public static void beforeClass() {
        SpringLiteContext.init(DEFAULT_SCAN_PACKAGE);
        RocksDBService.init(DATA_PATH);
    }

    @Before
    public void setUp() throws Exception {
        RocksDBService.createTable(PROTOCOL_CONFIG);
        ConfigLoader.load();
    }

    @After
    public void tearDown() throws Exception {
        RocksDBService.destroyTable(PROTOCOL_CONFIG);
        RocksDBService.destroyTable(Constant.STATISTICS + chainId);
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
            data.setMainVersion((short) 1);
            data.setBlockVersion((short) 1);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
        //V1-->V2,并且V2持续运行一段时间
        for (int i = 1889; i <= 18888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            data.setMainVersion(version);
            data.setBlockVersion((short) 2);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
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
            data.setMainVersion(version);
            data.setBlockVersion((short) 3);
            data.setEffectiveRatio((byte) 90);
            data.setContinuousIntervalCount((short) 20);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 18900) {
                assertEquals(2, version);
                assertEquals(179, context.getLastValidStatistics().getCount());
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
    public void test2() throws IOException {
        ProtocolContext context = ContextManager.getContext(chainId);
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(chainId);
        //模拟V1持续运行一段时间
        for (int i = 1; i <= 1888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setMainVersion((short) 1);
            data.setBlockVersion((short) 1);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
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
                data.setMainVersion((short) 1);
                data.setBlockVersion((short) 1);
                data.setEffectiveRatio((byte) 80);
                data.setContinuousIntervalCount((short) 10);
            } else {
                data.setMainVersion(version);
                data.setBlockVersion((short) 2);
                data.setEffectiveRatio((byte) 80);
                data.setContinuousIntervalCount((short) 10);
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
            data.setMainVersion(version);
            data.setBlockVersion((short) 3);
            data.setEffectiveRatio((byte) 90);
            data.setContinuousIntervalCount((short) 20);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 18900) {
                assertEquals(2, version);
                assertEquals(177, context.getLastValidStatistics().getCount());
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
    public void test3() throws IOException {
        ProtocolContext context = ContextManager.getContext(chainId);
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(chainId);
        //模拟V1持续运行一段时间
        for (int i = 1; i <= 1888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setMainVersion((short) 1);
            data.setBlockVersion((short) 1);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
        //V1-->V2,V2连续确认数不足，V3介入，V3升级成功
        for (int i = 1889; i <= 2333; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            data.setMainVersion(version);
            data.setBlockVersion((short) 2);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
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
            if (i == 2300) {
                assertEquals(1, version);
                assertEquals(4, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
        }
        //V2-->V3,并且V3持续运行一段时间
        for (int i = 2334; i <= 28888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            data.setMainVersion(version);
            data.setBlockVersion((short) 3);
            data.setEffectiveRatio((byte) 90);
            data.setContinuousIntervalCount((short) 20);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 2400) {
                assertEquals(1, version);
                assertEquals(20, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2500) {
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
    public void test4() throws IOException {
        ProtocolContext context = ContextManager.getContext(chainId);
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(chainId);
        //模拟V1持续运行一段时间
        for (int i = 1; i <= 1888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setMainVersion((short) 1);
            data.setBlockVersion((short) 1);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
        //V1-->V2,V2统计有波动，并且V2持续运行一段时间
        for (int i = 1889; i <= 18888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            if (i >= 2282 && i <=2585) {
                data.setMainVersion((short) 1);
                data.setBlockVersion((short) 1);
                data.setEffectiveRatio((byte) 80);
                data.setContinuousIntervalCount((short) 10);
            } else {
                data.setMainVersion(version);
                data.setBlockVersion((short) 2);
                data.setEffectiveRatio((byte) 80);
                data.setContinuousIntervalCount((short) 10);
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
            if (i == 2300) {
                assertEquals(1, version);
                assertEquals(4, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 2400) {
                assertEquals(1, version);
                assertEquals(20, context.getLastValidStatistics().getCount());
                assertEquals(1, context.getLastValidStatistics().getProtocolVersion().getVersion());
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
            data.setMainVersion(version);
            data.setBlockVersion((short) 3);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 18900) {
                assertEquals(2, version);
                assertEquals(175, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 19000) {
                assertEquals(2, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(3, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
        }
        //V3-->V4,并且V4持续运行一段时间
        for (int i = 19223; i <= 28888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            data.setMainVersion(version);
            data.setBlockVersion((short) 4);
            data.setEffectiveRatio((byte) 90);
            data.setContinuousIntervalCount((short) 20);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 19300) {
                assertEquals(2, version);
                assertEquals(176, context.getLastValidStatistics().getCount());
                assertEquals(2, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 19400) {
                assertEquals(2, version);
                assertEquals(1, context.getLastValidStatistics().getCount());
                assertEquals(4, context.getLastValidStatistics().getProtocolVersion().getVersion());
            }
            if (i == 22000) {
                assertEquals(4, version);
            }
        }
    }

    /**
     * 测试连续升级后连续回滚降级(中途统计没有波动，没有跨版本升级)
     *
     * @throws IOException
     */
    @Test
    public void test5() throws IOException {
        Stack<BlockHeader> stack = new Stack<>();
        ProtocolContext context = ContextManager.getContext(chainId);
        BlockHeaderInvoke invoke = new BlockHeaderInvoke(chainId);
        //模拟V1持续运行一段时间
        for (int i = 1; i <= 1888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            data.setMainVersion((short) 1);
            data.setBlockVersion((short) 1);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            stack.push(blockHeader);
        }
        //V1-->V2,并且V2持续运行一段时间
        for (int i = 1889; i <= 18888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            data.setMainVersion(version);
            data.setBlockVersion((short) 2);
            data.setEffectiveRatio((byte) 80);
            data.setContinuousIntervalCount((short) 10);
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
            stack.add(blockHeader);
        }
        //V2-->V3,并且V3持续运行一段时间
        for (int i = 18889; i <= 28888; i++) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(i);
            BlockExtendsData data = new BlockExtendsData();
            short version = context.getCurrentProtocolVersion().getVersion();
            data.setMainVersion(version);
            data.setBlockVersion((short) 3);
            data.setEffectiveRatio((byte) 90);
            data.setContinuousIntervalCount((short) 20);
            blockHeader.setExtend(data.serialize());
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
            version = context.getCurrentProtocolVersion().getVersion();
            if (i == 18900) {
                assertEquals(2, version);
                assertEquals(179, context.getLastValidStatistics().getCount());
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
            stack.add(blockHeader);
        }

        //开始回滚
        for (int i = stack.size() - 1; i >= 0; i--) {
            BlockHeader blockHeader = stack.pop();
            invoke.callBack(response(HexUtil.encode(blockHeader.serialize())));
        }
    }

    public Response response(Object responseData) {
        Response response = MessageUtil.newResponse("", Constants.BOOLEAN_TRUE, "Congratulations! Processing completed！");
        response.setResponseData(responseData);
        return response;
    }
}