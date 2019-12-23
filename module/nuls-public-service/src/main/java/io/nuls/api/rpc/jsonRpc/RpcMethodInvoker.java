/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.api.rpc.jsonRpc;

import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 */
public class RpcMethodInvoker {

    private Object bean;

    private Method method;

    public RpcMethodInvoker(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public RpcResult invoke(Object object) {
        RpcResult result = null;
        try {
            result = (RpcResult) method.invoke(bean, object);
        } catch (Exception e) {

            LoggerUtil.commonLog.error("\n" + method.toString());
            if (e.getCause() instanceof JsonRpcException) {
                JsonRpcException jsonRpcException = (JsonRpcException) e.getCause();
                result = new RpcResult();
                result.setError(jsonRpcException.getError());

            } else if (e.getCause() instanceof NulsException) {
                NulsException nulsException = (NulsException) e.getCause();
                result = new RpcResult();
                String error = null;
                String customMessage = nulsException.getCustomMessage();
                if (StringUtils.isNotBlank(customMessage) && customMessage.contains(";")) {
                    error = (customMessage.split(";", 2))[1];
                }
                RpcResultError rpcResultError = new RpcResultError(nulsException.getErrorCode());
                rpcResultError.setData(error);
                result.setError(rpcResultError);
            } else {
                LoggerUtil.commonLog.error(e.getCause());
                result = new RpcResult();
                RpcResultError error = new RpcResultError();
                error.setMessage("system error");
                error.setCode("-32603");
                error.setData(e.getCause().getMessage());
                result.setError(error);
            }
        }
        return result;
    }

}
