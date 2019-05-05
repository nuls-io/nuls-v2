/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2018 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.core.rpc.cmd.kernel;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.RegisterApi;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * this class is only used by testing.
 *
 * @author tangyi
 * @date 2018/10/17
 * @description
 */
@Component
public class KernelCmd4Test extends BaseCmd {


    @CmdAnnotation(cmd = "RegisterAPI", version = 1.0, minEvent = 1,
            description = "Register API")
    public Response registerAPI(Map<String, Object> map) {
        try {
            RegisterApi registerApi = JSONUtils.map2pojo(map, RegisterApi.class);
            Log.info("注册的方法：" + JSONUtils.obj2json(registerApi));
            if (registerApi != null) {
                Map<String, Object> role = new HashMap<>(3);
                role.put(Constants.KEY_IP, registerApi.getConnectionInformation().get(Constants.KEY_IP));
                role.put(Constants.KEY_PORT, registerApi.getConnectionInformation().get(Constants.KEY_PORT));
                ConnectManager.ROLE_MAP.put(registerApi.getAbbreviation(), role);
                ConnectData connectData = ConnectManager.getConnectDataByRole(registerApi.getAbbreviation());
                if(connectData != null){
                    connectData.addCloseEvent(() -> {
                        if (!ConnectManager.ROLE_CHANNEL_MAP.containsKey(registerApi.getAbbreviation())) {
                            Log.warn("RMB:{}模块触发连接断开事件", registerApi.getAbbreviation());
                            ConnectManager.ROLE_MAP.remove(registerApi.getAbbreviation());
                        }
                    });
                }
            }
            Map<String, Object> dependMap = new HashMap<>(1);
            dependMap.put("Dependencies", ConnectManager.ROLE_MAP);
            return success(dependMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

}
