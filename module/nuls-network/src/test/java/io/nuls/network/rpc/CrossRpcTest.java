/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.network.rpc;

import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.network.utils.LoggerUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/01/21
 **/
public class CrossRpcTest {
    @Before
    public void before() throws Exception {
        NoUse.mockModule();
    }

    @Test
    public void addCrossChain() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", 54);
            map.put("magicNumber", 35354343);
            map.put("maxOut", "");
            map.put("maxIn", "");
            map.put("minAvailableCount", 20);
            map.put("seedIps", "");
            map.put("isCrossGroup", "true");
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_createNodeGroup", map);
            LoggerUtil.COMMON_LOG.info("response={}", response);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
        }

    }

    @Test
    public void activeCrossChain() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", 100);
            map.put("maxOut", "100");
            map.put("maxIn", "50");
            map.put("seedIps", "192.168.1.122:28112");
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_activeCross", map);
            LoggerUtil.COMMON_LOG.info("response={}", JSONUtils.obj2json(response));
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
        }

    }

    @Test
    public void delCrossChain() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("chainId", 54);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_delNodeGroup", map);
            LoggerUtil.COMMON_LOG.info("response={}", response);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
        }

    }

}
