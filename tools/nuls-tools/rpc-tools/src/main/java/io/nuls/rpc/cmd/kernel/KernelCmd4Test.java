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
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.RegisterApi;
import io.nuls.rpc.model.message.Response;
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


    @CmdAnnotation(cmd = "registerAPI", version = 1.0, scope = "private", minEvent = 1, minPeriod = 0,
            description = "Register API")
    public Response registerAPI(Map<String, Object> map) {
        try {
            RegisterApi registerApi = JSONUtils.map2pojo(map, RegisterApi.class);
            if (registerApi != null) {
                Map<String, Object> role = new HashMap<>(3);
                role.put(Constants.KEY_IP, registerApi.getConnectionInformation().get(Constants.KEY_IP));
                role.put(Constants.KEY_PORT, registerApi.getConnectionInformation().get(Constants.KEY_PORT));
                role.put(Constants.KEY_API_VERSION, registerApi.getSupportedAPIVersions());
                ClientRuntime.roleMap.put(registerApi.getModuleAbbreviation(), role);
            }
            Map<String, Object> methodMap = new HashMap<>(1);
            Map<String, Object> dependMap = new HashMap<>(1);
            dependMap.put("Dependencies", ClientRuntime.roleMap);
            methodMap.put("RegisterAPI", dependMap);
            return success(methodMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "method1", version = 1.0, scope = "private", minEvent = 1, minPeriod = 0,
            description = "Test method1")
    @Parameter(parameterName = "param1", parameterType = "string")
    public Response method1(Map<String, Object> map) {
        return success();
    }

}
