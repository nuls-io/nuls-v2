/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.block.rpc;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.*;

/**
 * 需要单节点运行起来,再执行这个测试类,测试的都是RPC接口
 */
public class BlockResourceTest {

    private int chainId = 2;

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    public void latestHeight() throws Exception {
        {
            Map<String,Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, chainId);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, LATEST_HEIGHT, params);
            Map responseData = (Map) cmdResp.getResponseData();
            Map map = (Map) responseData.get(LATEST_HEIGHT);
            Object value = map.get("value");
            Assert.assertTrue(value.toString().matches("\\d+"));
        }
        {
            Map<String,Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, 333333);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, LATEST_HEIGHT, params);
            Map responseData = (Map) cmdResp.getResponseData();
            Object o = responseData.get(LATEST_HEIGHT);
            Assert.assertNull(o);
        }
    }

    @Test
    public void latestBlockHeader() throws Exception {
        {
            Map<String,Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, chainId);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, LATEST_BLOCK_HEADER, params);
            Map data = (Map) cmdResp.getResponseData();
            String hex = (String) data.get(LATEST_BLOCK_HEADER);
            BlockHeader header = new BlockHeader();
            header.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            System.out.println(header.getHeight());
        }
        {
            Map<String,Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, 4242);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, LATEST_BLOCK_HEADER, params);
            Map data = (Map) cmdResp.getResponseData();
            String hex = (String) data.get(LATEST_BLOCK_HEADER);
            System.out.println(hex);
        }
    }

    @Test
    public void latestBlock() throws Exception {
        {
            Map<String,Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, chainId);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, LATEST_BLOCK, params);
            Map data = (Map) cmdResp.getResponseData();
            String hex = (String) data.get(LATEST_BLOCK);
            Block block = new Block();
            block.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            System.out.println(block.getHeader().getHeight());
        }
        {
            Map<String,Object> params = new HashMap<>();
            params.put(Constants.CHAIN_ID, 242);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, LATEST_BLOCK, params);
            Map data = (Map) cmdResp.getResponseData();
            String hex = (String) data.get(LATEST_BLOCK);
            System.out.println(hex);
        }
    }

    @Test
    public void getBlockHeaderByHeight() throws Exception {
        Map<String,Object> params = new HashMap<>();
        long height = 4L;
        params.put(Constants.CHAIN_ID, chainId);
        params.put("height", height);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_HEADER_BY_HEIGHT, params);
        Map data = (Map) cmdResp.getResponseData();
        String hex = (String) data.get(GET_BLOCK_HEADER_BY_HEIGHT);
        BlockHeader block100 = new BlockHeader();
        block100.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        Assert.assertEquals(height, block100.getHeight());

        params.put("height", height + 1);
        cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_HEADER_BY_HEIGHT, params);
        data = (Map) cmdResp.getResponseData();
        hex = (String) data.get(GET_BLOCK_HEADER_BY_HEIGHT);
        BlockHeader block101 = new BlockHeader();
        block101.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        Assert.assertEquals(height + 1, block101.getHeight());
        Assert.assertEquals(block100.getHash(), block101.getPreHash());
    }

    @Test
    public void getLatestBlockHeaders() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("size", 100);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_LATEST_BLOCK_HEADERS, params);
        Map data = (Map) cmdResp.getResponseData();
        List<String> hexList = (List) data.get(GET_LATEST_BLOCK_HEADERS);
        for (String hex : hexList) {
            BlockHeader block100 = new BlockHeader();
            block100.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        }
    }

    @Test
    public void getBlockByHeight() throws Exception {
        Map<String,Object> params = new HashMap<>();
        long blockHeight = 4L;
        params.put(Constants.CHAIN_ID, chainId);
        params.put("height", blockHeight);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_BY_HEIGHT, params);
        Map data = (Map) cmdResp.getResponseData();
        String hex = (String) data.get(GET_BLOCK_BY_HEIGHT);
        Block block100 = new Block();
        block100.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        Log.info("block: {}", JSONUtils.obj2PrettyJson(block100));
        Assert.assertEquals(blockHeight, block100.getHeader().getHeight());
    }

    @Test
    public void getBlockHeaderByHash() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("height", 1);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_HEADER_BY_HEIGHT, params);
        Map data = (Map) cmdResp.getResponseData();
        String hex = (String) data.get(GET_BLOCK_HEADER_BY_HEIGHT);
        BlockHeader block100 = new BlockHeader();
        block100.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        Assert.assertEquals(1, block100.getHeight());
        NulsHash hash = block100.getHash();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("hash", hash.getDigestHex());
        cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_HEADER_BY_HASH, params);
        data = (Map) cmdResp.getResponseData();
        hex = (String) data.get(GET_BLOCK_HEADER_BY_HASH);
        BlockHeader block100_ = new BlockHeader();
        block100_.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        Assert.assertEquals(hash, block100_.getHash());
        Assert.assertEquals(1, block100_.getHeight());
    }

    @Test
    public void getBlockByHash() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("height", 1);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_BY_HEIGHT, params);
        Map data = (Map) cmdResp.getResponseData();
        String hex = (String) data.get(GET_BLOCK_BY_HEIGHT);
        Block block100 = new Block();
        block100.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        Assert.assertEquals(1, block100.getHeader().getHeight());
        NulsHash hash = block100.getHeader().getHash();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("hash", hash.getDigestHex());
        cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_BY_HASH, params);
        data = (Map) cmdResp.getResponseData();
        hex = (String) data.get(GET_BLOCK_BY_HASH);
        Block block100_ = new Block();
        block100_.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        Assert.assertEquals(hash, block100_.getHeader().getHash());
        Assert.assertEquals(1, block100_.getHeader().getHeight());
    }
}