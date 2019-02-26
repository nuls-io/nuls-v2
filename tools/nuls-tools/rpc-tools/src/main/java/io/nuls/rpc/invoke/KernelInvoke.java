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
package io.nuls.rpc.invoke;

import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.log.Log;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/12/20
 * @description
 */
public class KernelInvoke extends BaseInvoke {
    /**
     * 自动回调的类需要重写的方法
     * A method that needs to be rewritten for a class that calls back automatically
     *
     * @param response 请求的响应信息，Response information to requests
     */
    @SuppressWarnings("unchecked")
    @Override
    public void callBack(Response response) {
        Map responseData = (Map) response.getResponseData();
        Map methodMap = (Map) responseData.get("registerAPI");
        Map dependMap = (Map) methodMap.get("Dependencies");
        StringBuilder logInfo = new StringBuilder("\n有模块信息改变，重新同步：\n");
        for (Object object : dependMap.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) object;
            logInfo.append("注入：[key=").append(entry.getKey()).append(",value=").append(entry.getValue()).append("]\n");
            ConnectManager.ROLE_MAP.put(entry.getKey(), entry.getValue());
        }
        ConnectManager.updateStatus();
        Log.info(logInfo.toString());
    }
}
