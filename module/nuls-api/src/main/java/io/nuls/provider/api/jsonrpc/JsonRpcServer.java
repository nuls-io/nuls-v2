/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.provider.api.jsonrpc;

import io.nuls.core.core.annotation.Component;
import io.nuls.provider.model.jsonrpc.RpcForm;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.model.jsonrpc.RpcResultError;
import io.nuls.provider.utils.Log;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/jsonrpc")
@Component
public class JsonRpcServer {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Object dispatcher(Object form) {
        if(form == null) {
            return responseError("-32700", "", "0");
        }
        if(form instanceof List) {
            return dispatcherList((List<Map<String, Object>>) form);
        } else if(form instanceof Map) {
            return dispatcher((Map<String, Object>) form);
        } else {
            return responseError("-32700", "the request is not a json-rpc 2.0 request", "0");
        }
    }

    private Object dispatcherList(List<Map<String, Object>> forms) {
        List<RpcResult> list = new ArrayList<>();
        for(Map<String, Object> form : forms) {
            list.add(dispatcher(form));
        }
        if(list.size() == 1) {
            return list.get(0);
        }
        return list;
    }

    private RpcResult dispatcher(Map<String, Object> form) {
        String method = (String) form.get("method");
        String id = form.get("id") + "";
        String jsonrpc = (String) form.get("jsonrpc");
        if (!"2.0".equals(jsonrpc)) {
            Log.warn("the request is not a json-rpc 2.0 request!");
            return responseError("-32600", "the request is not a json-rpc 2.0 request", id);
        }
        RpcMethodInvoker invoker = JsonRpcContext.RPC_METHOD_INVOKER_MAP.get(method);
        if (null == invoker) {
            Log.warn("Can't find the method:{}", method);
            return responseError("-32601", "Can't find the method", id);
        }
        RpcResult result = invoker.invoke((List<Object>) form.get("params"));
        result.setId(id);
        return result;
    }

    private RpcResult responseError(String code, String message, String id) {
        RpcResult result = new RpcResult();
        RpcResultError error = new RpcResultError();
        error.setCode(code);
        error.setMessage(message);
        result.setError(error);
        result.setId(id);
        return result;
    }

    private static class JsonRpcForm {
        private List<RpcForm> forms;

        public List<RpcForm> getForms() {
            return forms;
        }

        public void setForms(List<RpcForm> forms) {
            this.forms = forms;
        }
    }
}
