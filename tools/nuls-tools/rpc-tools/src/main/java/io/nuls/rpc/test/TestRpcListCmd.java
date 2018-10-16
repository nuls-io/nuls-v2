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

package io.nuls.rpc.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.nuls.rpc.RpcClient;
import io.nuls.rpc.RpcInfo;
import io.nuls.rpc.pojo.Rpc;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/16
 * @description
 */
public class TestRpcListCmd {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        System.out.println("启动Client");
        RpcClient rpcClient = new RpcClient("192.168.1.65", 8091);
        String response = rpcClient.callRpc(RpcInfo.DEFAULT_PATH, RpcInfo.CMD_LIST, 1, null);
        System.out.println(response);

        System.out.println("我获取的接口如下：");
        Map<String, JSONObject> rpcMap = JSON.parseObject(response, Map.class);
        for (String key : rpcMap.keySet()) {
            Rpc rpc = JSON.parseObject(JSON.toJSONString(rpcMap.get(key)), Rpc.class);
            System.out.println(rpc);
        }

    }
}
