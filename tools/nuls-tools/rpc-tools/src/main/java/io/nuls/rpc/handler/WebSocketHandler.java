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

package io.nuls.rpc.handler;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.CallCmd;
import io.nuls.rpc.model.Rpc;
import io.nuls.rpc.model.RpcResult;
import io.nuls.tools.parse.JSONUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class WebSocketHandler {

    public static String callCmd(String formParamAsJson) throws Exception {

        Map<String, Object> jsonMap = JSONUtils.json2map(formParamAsJson);

        Rpc rpc = CallCmd.getLocalInvokeRpc((String) jsonMap.get("cmd"), (Double) jsonMap.get("minVersion"));
        if (rpc == null) {
            return "No cmd found: " + jsonMap.get("cmd") + "." + jsonMap.get("minVersion");
        }

        Class clz = Class.forName(rpc.getInvokeClass());

        @SuppressWarnings("unchecked") Method method = clz.getDeclaredMethod(rpc.getInvokeMethod(), List.class);
        @SuppressWarnings("unchecked") Constructor constructor = clz.getConstructor();
        BaseCmd cmd = (BaseCmd) constructor.newInstance();
        RpcResult rpcResult = (RpcResult) method.invoke(cmd, (List) jsonMap.get("params"));
        rpcResult.setId((Integer) jsonMap.get("id"));

        return JSONUtils.obj2json(rpcResult);
    }
}
