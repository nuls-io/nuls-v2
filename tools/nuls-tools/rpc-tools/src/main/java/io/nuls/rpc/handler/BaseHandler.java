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

import io.nuls.rpc.RpcInfo;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.pojo.Rpc;
import io.nuls.rpc.pojo.RpcCmd;
import io.nuls.tools.parse.JSONUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
@Path("nulsrpc")
public class BaseHandler {

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String doPost(@FormParam(RpcInfo.FORM_PARAM_NAME) String formParamAsJson, @Context HttpServletRequest request) {
        return cmd(formParamAsJson, request);
    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String doGet(@QueryParam(RpcInfo.FORM_PARAM_NAME) String formParamAsJson, @Context HttpServletRequest request) {
        return cmd(formParamAsJson, request);
    }

    private String cmd(String formParamAsJson, HttpServletRequest request) {
        System.out.println("BaseHandler-cmd start, formParamAsJson->" + formParamAsJson + "\n"
                + "request->" + request);

        RpcCmd rpcCmd = null;
        try {
            rpcCmd = JSONUtils.json2pojo(formParamAsJson, RpcCmd.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rpcCmd == null) {
            return "Bad cmd: " + formParamAsJson;
        }

        Rpc rpc = RpcInfo.getInvokeRpcByCmd(rpcCmd);
        if (rpc == null) {
            return "No cmd found: " + rpcCmd.toString();
        }

        try {
            Class clz = rpc.getInvokeClass();
            @SuppressWarnings("unchecked") Method execRpc = clz.getMethod("execRpc", Object.class);
            @SuppressWarnings("unchecked") Constructor constructor = clz.getConstructor();
            BaseCmd cmd = (BaseCmd) constructor.newInstance();
            return (String) execRpc.invoke(cmd, rpcCmd.getParam());
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
