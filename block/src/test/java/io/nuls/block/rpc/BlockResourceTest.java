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
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.*;
import static org.junit.Assert.*;

public class BlockResourceTest {

    private int chainId = 1;

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    public void latestHeight() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.BL.abbr, LATEST_HEIGHT, params);
        System.out.println(cmdResp.getResponseData());
    }

    @Test
    public void latestBlockHeader() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.BL.abbr, LATEST_BLOCK_HEADER, params);
        Map data = (Map) cmdResp.getResponseData();
        String hex = (String) data.get(LATEST_BLOCK_HEADER);
        BlockHeader header = new BlockHeader();
        header.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        System.out.println(header.getHeight());
    }

    @Test
    public void latestBlock() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.BL.abbr, LATEST_BLOCK, params);
        Map data = (Map) cmdResp.getResponseData();
        String hex = (String) data.get(LATEST_BLOCK);
        Block block = new Block();
        block.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        System.out.println(block.getHeader().getHeight());
    }

    @Test
    public void getBlockHeaderByHeight() throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("height", 100);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_HEADER_BY_HEIGHT, params);
        Map data = (Map) cmdResp.getResponseData();
        String hex = (String) data.get(GET_BLOCK_HEADER_BY_HEIGHT);
        BlockHeader block100 = new BlockHeader();
        block100.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        System.out.println(block100.getHeight());

        params.put("height", 101);
        cmdResp = CmdDispatcher.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_HEADER_BY_HEIGHT, params);
        data = (Map) cmdResp.getResponseData();
        hex = (String) data.get(GET_BLOCK_HEADER_BY_HEIGHT);
        BlockHeader block101 = new BlockHeader();
        block101.parse(new NulsByteBuffer(HexUtil.decode(hex)));
        System.out.println(block101.getHeight());
    }

    @Test
    public void getLatestBlockHeaders() {
    }

    @Test
    public void getBlockByHeight() {
    }

    @Test
    public void getBlockHeaderByHash() {
    }

    @Test
    public void getBlockByHash() {
    }
}