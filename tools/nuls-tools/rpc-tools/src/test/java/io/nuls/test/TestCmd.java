/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.test;

import io.nuls.rpc.RpcClient;
import io.nuls.rpc.info.RpcInfo;
import io.nuls.rpc.pojo.Rpc;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Test;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/16
 * @description
 */
public class TestCmd {
    @SuppressWarnings("unchecked")
    @Test
    public void testList() throws Exception {

        System.out.println("启动Client");
        RpcClient rpcClient = new RpcClient("192.168.1.65", 8091);
        String response = rpcClient.callRpc(RpcInfo.DEFAULT_PATH, RpcInfo.CMD_LIST,null);
        System.out.println(response);

        System.out.println("我获取的接口如下：");
        Map<String, Rpc> rpcMap = JSONUtils.json2map(response, Rpc.class);
        for (String key : rpcMap.keySet()) {
            Rpc rpc = JSONUtils.json2pojo(JSONUtils.obj2json(rpcMap.get(key)), Rpc.class);
            System.out.println(rpc);
        }
    }

    @Test
    public void testVersion() {
        RpcClient rpcClient = new RpcClient("192.168.1.65", 8091);
        System.out.println(rpcClient.callRpc(RpcInfo.DEFAULT_PATH, "version", null));
        System.out.println(rpcClient.callRpc(RpcInfo.DEFAULT_PATH, "version", null,1));
    }
}
