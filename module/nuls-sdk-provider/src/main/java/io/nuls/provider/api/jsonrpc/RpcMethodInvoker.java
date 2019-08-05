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

package io.nuls.provider.api.jsonrpc;

import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.model.jsonrpc.RpcResultError;

import java.lang.reflect.Method;
import java.util.List;

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

    public RpcResult invoke(List<Object> jsonParams) {
        RpcResult result = null;
        try {
            result = (RpcResult) method.invoke(bean, jsonParams);
        } catch (Exception e) {

            Log.error("\n" + method.toString());
            if (e.getCause() instanceof JsonRpcException) {
                JsonRpcException jsonRpcException = (JsonRpcException) e.getCause();
                result = new RpcResult();
                result.setError(jsonRpcException.getError());

            } else if (e.getCause() instanceof NulsException) {
                NulsException nulsException = (NulsException) e.getCause();
                result = new RpcResult();

                result.setError(new RpcResultError(nulsException.getErrorCode()));
            } else {
                Log.error(e);
                result = new RpcResult();
                RpcResultError error = new RpcResultError();
                error.setMessage("system error");
                error.setCode("-32603");
                error.setData(e.getMessage());
                result.setError(error);
            }
        }
        return result;
    }

}
