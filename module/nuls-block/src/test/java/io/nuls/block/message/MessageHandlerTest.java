/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.message;

import io.nuls.base.data.NulsHash;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.GET_BLOCKS_BY_HEIGHT_MESSAGE;
import static io.nuls.block.constant.CommandConstant.GET_BLOCK_MESSAGE;

/**
 * 需要单节点运行起来,再执行这个测试类,测试的是消息收发逻辑
 */
public class MessageHandlerTest {

    @Test
    public void getBlock() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, 1);
        params.put("nodes", "192.168.1.191:8003");
        HashMessage message = new HashMessage();
        message.setRequestHash(NulsHash.fromDigestHex("00208d10744a059e403b100866f65d96ce33aedbcf498d1faa7d9f2eff041195d5aa"));
        params.put("messageBody", HexUtil.encode(message.serialize()));
        params.put("command", GET_BLOCK_MESSAGE);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCK_MESSAGE, params);
        System.out.println(response);
    }

    @Test
    public void getBlocks() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, 1);
        params.put("nodes", "192.168.1.191:8003");
        HeightRangeMessage message = new HeightRangeMessage();
        message.setStartHeight(1000);
        message.setEndHeight(1010);
        params.put("messageBody", HexUtil.encode(message.serialize()));
        params.put("command", GET_BLOCKS_BY_HEIGHT_MESSAGE);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, GET_BLOCKS_BY_HEIGHT_MESSAGE, params);
        System.out.println(response);
    }
}