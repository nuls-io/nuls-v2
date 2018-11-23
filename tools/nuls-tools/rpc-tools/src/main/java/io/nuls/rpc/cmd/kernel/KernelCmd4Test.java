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
import io.nuls.rpc.info.ClientRuntime;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.ModuleInfo;
import io.nuls.rpc.model.RegisterApi;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Map;

/**
 * this class is only used by testing.
 *
 * @author tangyi
 * @date 2018/10/17
 * @description
 */
public class KernelCmd4Test extends BaseCmd {


    @CmdAnnotation(cmd = "registerAPI", version = 1.0, scope = "private", minEvent = 1, minPeriod = 0,
            description = "Register API")
    public Response registerAPI(Map<String, Object> map) {
        try {
            RegisterApi registerApi = JSONUtils.map2pojo(map, RegisterApi.class);
            if (registerApi != null) {
                ModuleInfo moduleInfo = new ModuleInfo();
                String address = registerApi.getAddress();
                int port = registerApi.getPort();
                moduleInfo.setAddress(address);
                moduleInfo.setPort(port);
                moduleInfo.setAbbr(registerApi.getAbbr());
                moduleInfo.setName(registerApi.getName());
                moduleInfo.setRegisterApi(registerApi);
                ClientRuntime.remoteModuleMap.put(registerApi.getName(), moduleInfo);
            }
            System.out.println("Current APIMethods:" + JSONUtils.obj2json(ClientRuntime.remoteModuleMap));
            return success(ClientRuntime.remoteModuleMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }
}
