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

package io.nuls.rpc.cmd;

import io.nuls.rpc.info.RpcInfo;
import io.nuls.rpc.model.Module;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/15
 * @description
 */
public abstract class BaseCmd {

    protected final String SUCCESS = "Success";

    /**
     * 从kernel接收所有模块信息
     * 请求参数为：
     * {
     * "cmd": "status",
     * "version": 1.0,
     * "params": [{
     * "service":["module_A","module_B"],
     * "available": true,
     * "modules": {
     * "module_A": {
     * "status" : "",
     * "rpcList":[],
     * "dependsModule":[],
     * "addr":"ip",
     * "port": 8080,
     * }
     * }
     * }]
     * }
     */
    protected Object status(List params) throws IOException {
        System.out.println("我收到来自kernel的推送了");
        Map<String, Object> map1 = JSONUtils.json2map(JSONUtils.obj2json(params.get(0)));

        RpcInfo.local.setAvailable((Boolean) map1.get("available"));

        Map<String, Object> moduleMap = JSONUtils.json2map(JSONUtils.obj2json(map1.get("modules")));
        for (Object key : moduleMap.keySet()) {
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(moduleMap.get(key)), Module.class);
            RpcInfo.remoteModuleMap.put((String) key, module);
        }

        System.out.println(JSONUtils.obj2json(RpcInfo.remoteModuleMap));

        return success(1.0);
    }

    protected Object success(double version) {
        return success(version, null);
    }

    /**
     * return success object
     */
    protected Object success(double version, Object result) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("code", 0);
        map.put("msg", SUCCESS);
        map.put("version", version);
        map.put("result", result);
        return map;
    }

    /**
     * return fail object
     */
    protected Object fail(String code, String msg, double version, Object result) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("code", code);
        map.put("msg", msg);
        map.put("version", version);
        map.put("result", result);
        return map;
    }
}
