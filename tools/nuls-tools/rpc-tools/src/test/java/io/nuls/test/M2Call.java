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

import io.nuls.rpc.client.RpcClient;
import io.nuls.rpc.info.RpcConstant;
import io.nuls.rpc.info.RpcInfo;
import io.nuls.rpc.model.Module;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Test;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/20
 * @description
 */
public class M2Call {
    @Test
    public void test() throws Exception {
        System.out.println("我可以调用的模块：" + RpcInfo.remoteModuleMap.size());
        String str = RpcClient.callFetchKernel("http://127.0.0.1:8091/" + RpcConstant.DEFAULT_PATH + "/" + RpcConstant.SINGLE);
        System.out.println(str);
        Map map = JSONUtils.json2map(str);
        for (Object key : map.keySet()) {
            System.out.println(key);
            System.out.println(map.get(key));
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(map.get(key)), Module.class);
            RpcInfo.remoteModuleMap.put((String) key, module);
        }
        System.out.println("我可以调用的模块：" + RpcInfo.remoteModuleMap.size());

        System.out.println("我开始调用其他模块了");

        System.out.println(RpcClient.callSingleRpc("cmd1", null, 3));

    }
}
