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

import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 所有该端口的请求都发送到这里统一处理，在此类中封装JSON-RPC 2.0规范的框架
 *
 * @author Niels
 */
public class JsonRpcHandler extends HttpHandler {

    @Override
    public void service(Request request, Response response) throws Exception {

        response.setHeader("Access-control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (request.getMethod().equals(Method.OPTIONS)) {
            response.getWriter().write("ok");
            return;
        }
//        System.out.println("request::::::::::::::");
        if (!request.getMethod().equals(Method.POST)) {
            Log.warn("the request is not POST!");
            responseError(response, -32600, "", 0);
            return;
        }

        String content = null;
        try {
            content = getParam(request);
        } catch (IOException e) {
            Log.error(e);
        }
        if (StringUtils.isBlank(content)) {
            responseError(response, -32700, "", 0);
            return;
        }
        content = content.trim();

        if (content.startsWith("[")) {
            // 处理批量请求
            List<Map> paramList;
            try {
                paramList = JSONUtils.json2list(content, Map.class);
            } catch (Exception e) {
                Log.error(e);
                responseError(response, -32700, "the request is not a json-rpc 2.0 request", 0);
                return;
            }

            for (Map<String, Object> map : paramList) {
                doHandler(map, response);
            }
        } else {
            // 处理单个请求
            Map<String, Object> jsonRpcParam = null;
            try {
                jsonRpcParam = JSONUtils.json2map(content);
            } catch (Exception e) {
                Log.error(e);
                responseError(response, -32700, "the request is not a json-rpc 2.0 request", 0);
                return;
            }
            doHandler(jsonRpcParam, response);
        }
    }

    private void doHandler(Map<String, Object> jsonRpcParam, Response response) throws Exception {
        String method = (String) jsonRpcParam.get("method");
        int id = (int) jsonRpcParam.get("id");
        if (!"2.0".equals(jsonRpcParam.get("jsonrpc"))) {
            Log.warn("the request is not a json-rpc 2.0 request!");
            responseError(response, -32600, "the request is not a json-rpc 2.0 request", id);
            return;
        }
        RpcMethodInvoker invoker = JsonRpcContext.RPC_METHOD_INVOKER_MAP.get(method);

        if (null == invoker) {
            Log.warn("Can't find the method:{}", method);
            responseError(response, -32601, "Can't find the method", id);
            return;
        }

        RpcResult result = invoker.invoke((List<Object>) jsonRpcParam.get("params"));

        result.setId(id);
        try {
            response.getWriter().write(JSONUtils.obj2json(result));
        } catch (Exception e) {
            Log.error(e);
            responseError(response, -32603, "Internal error!", id);
            return;
        }
    }

    private String getParam(Request request) throws IOException {
        int contentLength = request.getContentLength();
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength; ) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return new String(buffer, "UTF-8");
    }


    private void responseError(Response response, int code, String message, long id) throws Exception {
        RpcResult result = new RpcResult();
        RpcResultError error = new RpcResultError();
        error.setCode(code);
        error.setMessage(message);
        result.setError(error);
        result.setId(id);
        response.getWriter().write(JSONUtils.obj2json(result));
    }
}
