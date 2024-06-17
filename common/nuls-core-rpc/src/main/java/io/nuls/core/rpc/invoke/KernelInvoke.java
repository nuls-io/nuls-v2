/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.core.rpc.invoke;

import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.log.Log;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/12/20
 * @description
 */
public class KernelInvoke extends BaseInvoke {
    /**
     * Methods that need to be rewritten for automatic callback classes
     * A method that needs to be rewritten for a class that calls back automatically
     *
     * @param response Request response information,Response information to requests
     */
    @SuppressWarnings("unchecked")
    @Override
    public void callBack(Response response) {
        Map responseData = (Map) response.getResponseData();
        Map methodMap = (Map) responseData.get("RegisterAPI");
        Map dependMap = (Map) methodMap.get("Dependencies");
        StringBuilder logInfo = new StringBuilder("\nModule information has changed, please resynchronize：\n");
        for (Object object : dependMap.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) object;
            logInfo.append("injection：[key=").append(entry.getKey()).append(",value=").append(entry.getValue()).append("]\n");
            ConnectManager.ROLE_MAP.put(entry.getKey(), entry.getValue());
        }
        ConnectManager.updateStatus();
        Log.info(logInfo.toString());
    }
}
