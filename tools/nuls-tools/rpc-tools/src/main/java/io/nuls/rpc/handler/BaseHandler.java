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
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdDetail;
import io.nuls.rpc.model.CmdRequest;
import io.nuls.tools.parse.JSONUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
@Path(Constants.DEFAULT_PATH)
public class BaseHandler {

    @POST
    @Path(Constants.JSON)
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String jsonPost(@FormParam(Constants.FORM_PARAM_NAME) String formParamAsJson, @Context HttpServletRequest request) throws Exception {
        return jsonGo(formParamAsJson, request);
    }

    @GET
    @Path(Constants.JSON)
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String jsonGet(@QueryParam(Constants.FORM_PARAM_NAME) String formParamAsJson, @Context HttpServletRequest request) throws Exception {
        return jsonGo(formParamAsJson, request);
    }

    /**
     * format of formParamAsJson:
     * {
     * "cmd":"cmd1",
     * "minVersion":1,
     * "params":[
     * "wangkun",// note: it should be any types. include str, int, float, boolean, object etc.
     * "handsome",
     * true
     * ]
     * }
     * <p>
     * format of return string:
     * {
     * "msg":"Success",
     * "result":{},
     * "code":0,
     * "version":1
     * }
     */
    private String jsonGo(String formParamAsJson, HttpServletRequest request) throws Exception {
        System.out.println("jsonGo: BaseHandler-cmd start, formParamAsJson->" + formParamAsJson + "\n"
                + "request->" + request.getRemoteAddr() + ":" + request.getRemotePort());

        Map<String, Object> jsonMap = JSONUtils.json2map(formParamAsJson);

        CmdDetail cmdDetail = RuntimeInfo.getLocalInvokeCmd((String) jsonMap.get("cmd"), (Double) jsonMap.get("minVersion"));
        if (cmdDetail == null) {
            return "No cmd found: " + jsonMap.get("cmd") + "." + jsonMap.get("minVersion");
        }

        Class clz = Class.forName(cmdDetail.getInvokeClass());

        @SuppressWarnings("unchecked") Method method = clz.getDeclaredMethod(cmdDetail.getInvokeMethod(), List.class);
        @SuppressWarnings("unchecked") Constructor constructor = clz.getConstructor();
        BaseCmd cmd = (BaseCmd) constructor.newInstance();
        Object obj = method.invoke(cmd, (List) jsonMap.get("params"));
        System.out.println("jsonGo Return String->" + JSONUtils.obj2json(obj));
        return JSONUtils.obj2json(obj);
    }


    @POST
    @Path(Constants.BYTE)
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String bytePost(@Context HttpServletRequest request) throws Exception {
        return byteGo(request);
    }

    private String byteGo(HttpServletRequest request) throws Exception {
        System.out.println("postGo: BaseHandler-cmd start, request->" + request.getRemoteAddr() + ":" + request.getRemotePort());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(request.getInputStream().readAllBytes());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        CmdRequest cmdRequest = (CmdRequest) objectInputStream.readObject();

        CmdDetail cmdDetail = RuntimeInfo.getLocalInvokeCmd(cmdRequest.getCmd(), cmdRequest.getMinVersion());

        Class clz = Class.forName(cmdDetail.getInvokeClass());

        @SuppressWarnings("unchecked") Method method = clz.getDeclaredMethod(cmdDetail.getInvokeMethod(), List.class);
        @SuppressWarnings("unchecked") Constructor constructor = clz.getConstructor();
        BaseCmd cmd = (BaseCmd) constructor.newInstance();
        Object obj = method.invoke(cmd, Arrays.asList(cmdRequest.getParams()));
        System.out.println("byteGo Return String->" + obj);
        return JSONUtils.obj2json(obj);
    }
}
