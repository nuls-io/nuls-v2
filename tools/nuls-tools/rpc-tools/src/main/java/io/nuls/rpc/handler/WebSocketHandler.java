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
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdDetail;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.ioc.SpringLiteContext;
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

        if (!RuntimeInfo.local.isAvailable()) {
            return "Module not available: " + RuntimeInfo.local.getName();
        }

        Map<String, Object> jsonMap = JSONUtils.json2map(formParamAsJson);
        CmdDetail cmdDetail = RuntimeInfo.getLocalInvokeCmd((String) jsonMap.get("cmd"), (Double) jsonMap.get("minVersion"));
        if (cmdDetail == null) {
            return "No cmd found: " + jsonMap.get("cmd") + "." + jsonMap.get("minVersion");
        }

        CmdResponse cmdResponse = buildResponse(cmdDetail.getInvokeClass(), cmdDetail.getInvokeMethod(), (List) jsonMap.get("params"));
        cmdResponse.setId((Integer) jsonMap.get("id"));

        return JSONUtils.obj2json(cmdResponse);
    }

    private static CmdResponse buildResponse(String invokeClass, String invokeMethod, List params) throws Exception {

        Class clz = Class.forName(invokeClass);
        @SuppressWarnings("unchecked") Method method = clz.getDeclaredMethod(invokeMethod, List.class);

        BaseCmd cmd;
        if (SpringLiteContext.getBeanByClass(invokeClass) == null) {
            @SuppressWarnings("unchecked") Constructor constructor = clz.getConstructor();
            cmd = (BaseCmd) constructor.newInstance();
        } else {
            cmd = (BaseCmd) SpringLiteContext.getBeanByClass(invokeClass);
        }

        return (CmdResponse) method.invoke(cmd, params);
    }

}
