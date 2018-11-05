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

import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.rpc.model.ConfigItem;
import io.nuls.rpc.model.Module;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/15
 * @description
 */
public abstract class BaseCmd {

    protected final int SUCCESS_CODE = 0;

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
    protected CmdResponse status(List params) throws IOException {
        System.out.println("我收到来自kernel的推送了");
        Map<String, Object> map1 = JSONUtils.json2map(JSONUtils.obj2json(params.get(0)));

        RuntimeInfo.local.setAvailable((Boolean) map1.get("available"));

        Map<String, Object> moduleMap = JSONUtils.json2map(JSONUtils.obj2json(map1.get("modules")));
        for (String key : moduleMap.keySet()) {
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(moduleMap.get(key)), Module.class);
            RuntimeInfo.remoteModuleMap.put(key, module);
        }

        System.out.println(JSONUtils.obj2json(RuntimeInfo.remoteModuleMap));

        return result(1.0);
    }

    protected void addConfigItem(String key, Object value, boolean readOnly) {
        ConfigItem configItem = new ConfigItem(key, value, readOnly);
        RuntimeInfo.configItemList.add(configItem);
    }

    protected CmdResponse result(double version) {
        return result(0, version, null, null);
    }

    protected CmdResponse result(int code, double version, String msg, Object result) {
        CmdResponse cmdResponse = new CmdResponse();
        cmdResponse.setCode(code);
        cmdResponse.setVersion(version);
        cmdResponse.setMsg(msg);
        cmdResponse.setResult(result);
        return cmdResponse;
    }

}
