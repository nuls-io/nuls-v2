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

package io.nuls.rpc.handler;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.RpcConstant;
import io.nuls.rpc.info.RpcInfo;
import io.nuls.rpc.model.Rpc;
import io.nuls.tools.parse.JSONUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
@Path(RpcConstant.DEFAULT_PATH)
public class BaseHandler {

    @POST
    @Path(RpcConstant.SINGLE)
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String singlePost(@FormParam(RpcConstant.FORM_PARAM_NAME) String formParamAsJson, @Context HttpServletRequest request) throws Exception {
        return cmd(formParamAsJson, request);
    }

    @GET
    @Path(RpcConstant.SINGLE)
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String singleGet(@QueryParam(RpcConstant.FORM_PARAM_NAME) String formParamAsJson, @Context HttpServletRequest request) throws Exception {
        return cmd(formParamAsJson, request);
    }

    private String cmd(String formParamAsJson, HttpServletRequest request) throws Exception {
        System.out.println("BaseHandler-cmd start, formParamAsJson->" + formParamAsJson + "\n"
                + "request->" + request.getRemoteAddr() + ":" + request.getRemotePort());

        Map<String, Object> jsonMap = JSONUtils.json2map(formParamAsJson);

        Rpc rpc = RpcInfo.getLocalInvokeRpc((String) jsonMap.get("cmd"), (Double) jsonMap.get("minVersion"));
        if (rpc == null) {
            return "No cmd found: " + jsonMap.get("cmd");
        }

        Class clz = Class.forName(rpc.getInvokeClass());

        @SuppressWarnings("unchecked") Method method = clz.getDeclaredMethod(rpc.getInvokeMethod(), List.class);
        @SuppressWarnings("unchecked") Constructor constructor = clz.getConstructor();
        BaseCmd cmd = (BaseCmd) constructor.newInstance();
        Object obj = method.invoke(cmd, (List) jsonMap.get("param"));
        System.out.println("Return String->" + JSONUtils.obj2json(obj));
        return JSONUtils.obj2json(obj);
    }
}
