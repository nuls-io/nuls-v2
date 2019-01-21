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

package io.nuls.block.utils;

import io.nuls.block.model.Node;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.WsServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkUtilTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getAvailableNodes() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", 9861);
        params.put("state", 1);
        params.put("isCross", 0);
        params.put("startPage", 0);
        params.put("pageSize", 0);

        Response response = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_getNodes", params);
        Map responseData = (Map) response.getResponseData();
        List nw_getNodes = (List) responseData.get("nw_getNodes");
        List nodes = new ArrayList();
        for (Object nw_getNode : nw_getNodes) {
            Map map = (Map) nw_getNode;
            Node node = new Node();
            node.setId((String) map.get("nodeId"));
            node.setHeight(Long.parseLong(map.get("blockHeight").toString()));
//            node.setHash(NulsDigestData.fromDigestHex((String) map.get("blockHash")));
            nodes.add(node);
        }
        nodes.forEach(e -> System.out.println(e));
    }

    @Test
    public void resetNetwork() {
    }
}