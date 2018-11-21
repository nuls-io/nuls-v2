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

package io.nuls.rpc.cmd.kernel;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.ModuleInfo;
import io.nuls.rpc.model.RegisterApi;
import io.nuls.rpc.model.message.Message;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * this class is only used by testing.
 *
 * @author tangyi
 * @date 2018/10/17
 * @description
 */
public class KernelCmd4Test extends BaseCmd {

    @CmdAnnotation(cmd = "register", version = 1.0, scope = "private", minEvent = 1, minPeriod = 0,
            description = "Register to manager")
    public Message version(Map<String, Object> map) {
        try {
            System.out.println("join之前的kernel remote模块数：" + RuntimeInfo.remoteModuleMap.size());
            ModuleInfo moduleInfo = JSONUtils.json2pojo(JSONUtils.obj2json(map.get("RegisterAPI")), ModuleInfo.class);
            if (moduleInfo != null) {
                System.out.println(moduleInfo.getAbbr() + " added");
                RuntimeInfo.remoteModuleMap.put(moduleInfo.getAbbr(), moduleInfo);
            }
            System.out.println("join之后的kernel remote模块数：" + RuntimeInfo.remoteModuleMap.size());

            Map<String, Object> result = new HashMap<>(16);
            result.put("service", new String[]{"a", "b", "c"});
            result.put("available", true);
            result.put("modules", RuntimeInfo.remoteModuleMap);

            return new Message();
        } catch (Exception e) {
            e.printStackTrace();
            return new Message();
        }
    }

    @CmdAnnotation(cmd = "negotiateConnection", version = 1.0, scope = "private", minEvent = 1, minPeriod = 0,
            description = "Negotiate connection")
    public Message negotiateConnection(Map<String, Object> map) {
        System.out.println("CompressionRate: " + map.get("CompressionRate"));
        System.out.println("CompressionAlgorithm: " + map.get("CompressionAlgorithm"));
        return new Message();
    }

    @CmdAnnotation(cmd = "registerAPI", version = 1.0, scope = "private", minEvent = 1, minPeriod = 0,
            description = "Register API")
    public Object registerAPI(Map<String, Object> map) {
        try {
            RegisterApi registerApi = JSONUtils.json2pojo(JSONUtils.obj2json(map), RegisterApi.class);
            if (registerApi != null) {
                ModuleInfo moduleInfo = new ModuleInfo();
                String address = registerApi.getAddress();
                int port = registerApi.getPort();
                moduleInfo.setAddress(address);
                moduleInfo.setPort(port);
                moduleInfo.setAbbr(registerApi.getAbbr());
                moduleInfo.setName(registerApi.getName());
                moduleInfo.setRegisterApi(registerApi);
                RuntimeInfo.remoteModuleMap.put(address + ":" + port, moduleInfo);
            }
            System.out.println("Current APIMethods:" + JSONUtils.obj2json(RuntimeInfo.remoteModuleMap));
            return RuntimeInfo.remoteModuleMap;
        } catch (Exception e) {
            Log.error(e);
            return e.getMessage();
        }
    }
}
