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

package io.nuls.rpc.client;

import io.nuls.rpc.info.CallCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeParam;
import io.nuls.rpc.model.Module;
import io.nuls.rpc.model.RpcCmd;
import io.nuls.tools.parse.JSONUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RpcClient {

    /**
     * the format of json string :
     * {
     * "cmd":"version",
     * "minVersion":1,
     * "params":[
     * {
     * "name":"moduleABC",
     * "status":"READY",
     * "available":false,
     * "addr":"127.0.0.1",
     * "port":18819,
     * "rpcList":[
     * {
     * "cmd":"shutdown",
     * "version":1
     * },
     * {
     * "cmd":"cmd1",
     * "version":1
     * },
     * {
     * "cmd":"status",
     * "version":1
     * }
     * ],
     * "dependsModule":[
     * "m2",
     * "m3"
     * ]
     * }
     * ]
     * }
     */
    public static String versionToKernel(String kernelUri) throws Exception {
        RpcCmd rpcCmd = new RpcCmd(RuntimeParam.sequence.incrementAndGet(), "version", 1.0, new Object[]{RuntimeParam.local});

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(Constants.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

        return jsonPost(kernelUri, postParams);
    }

    /**
     * 从核心模块获取当前所有模块信息
     * 核心模块的返回数据结构为：
     * {
     * "msg":"Success",
     * "result":{
     * "available":[
     * "moduleABC"
     * ],
     * "modules":{
     * "moduleABC":{
     * "name":"moduleABC",
     * "status":"",
     * "addr":"127.0.0.1",
     * "port":"17792",
     * "rpcList":[
     * {
     * "cmd":"cmd1",
     * "version":1,
     * "invokeClass":"io.nuls.rpc.cmd.cmd1.SomeCmd",
     * "invokeMethod":"methodName",
     * "preCompatible":true
     * },
     * {
     * "cmd":"cmd1",
     * "version":2.2,
     * "invokeClass":"io.nuls.rpc.cmd.cmd1.SomeCmd",
     * "invokeMethod":"cmd11111",
     * "preCompatible":true
     * }
     * ],
     * "dependsModule":[
     * "m2",
     * "m3"
     * ]
     * }
     * }
     * },
     * "code":0
     * }
     */
    public static Map<String, Object> callFetchKernel(String kernelUri) throws Exception {
        RpcCmd rpcCmd = new RpcCmd(RuntimeParam.sequence.incrementAndGet(), "fetch", 1.0, new Object[]{RuntimeParam.local});

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(Constants.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

        String fetchString = jsonPost(kernelUri, postParams);

        return fetch2Map(fetchString);
    }

    private static Map<String, Object> fetch2Map(String fetchString) throws IOException {
        System.out.println(fetchString);

        Map<String, Object> map1 = JSONUtils.json2map(fetchString);

        Map<String, Object> result = JSONUtils.json2map(JSONUtils.obj2json(map1.get("result")));
        System.out.println("available" + JSONUtils.obj2json(result.get("available")));

        Map<String, Object> moduleMap = JSONUtils.json2map(JSONUtils.obj2json(result.get("modules")));
        for (String key : moduleMap.keySet()) {
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(moduleMap.get(key)), Module.class);
            RuntimeParam.remoteModuleMap.put(key, module);
        }
        result.put("available", result.get("available"));
        result.put("modules", RuntimeParam.remoteModuleMap);

        Map<String, Object> fetchMap = new HashMap<>(16);
        fetchMap.put("code", map1.get("code"));
        fetchMap.put("msg", map1.get("msg"));
        fetchMap.put("result", result);

        return fetchMap;
    }

    /**
     * call rpc : one cmd, one action
     */
    public static String jsonSingleRpc(String cmd, Object[] params, double minVersion) throws IOException {

        RpcCmd rpcCmd = new RpcCmd(RuntimeParam.sequence.incrementAndGet(), cmd, minVersion, params);

        List<String> remoteUriList = CallCmd.getRemoteUri(rpcCmd);
        if (remoteUriList.size() == 0) {
            return "No cmd found->" + cmd + "." + minVersion;
        }
        if (remoteUriList.size() > 1) {
            return "Multiply cmd found->" + cmd;
        }

        System.out.println("请求参数：->" + JSONUtils.obj2json(rpcCmd));
        String remoteUri = remoteUriList.get(0);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(Constants.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

        String uri = remoteUri + "/" + Constants.DEFAULT_PATH + "/" + Constants.JSON;

        return jsonPost(uri, postParams);
    }

    /**
     * call rpc: one cmd, multiply actions
     */
    public static String jsonMultiplyRpc(String cmd, Object[] params, double minVersion) throws IOException {

        RpcCmd rpcCmd = new RpcCmd(RuntimeParam.sequence.incrementAndGet(), cmd, minVersion, params);

        List<String> remoteUriList = CallCmd.getRemoteUri(rpcCmd);
        if (remoteUriList.size() == 0) {
            return "No cmd found->" + cmd + "." + minVersion;
        }

        Map<String, Object> resultMap = new HashMap<>(16);
        for (String remoteUri : remoteUriList) {
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair(Constants.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

            HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

            String uri = remoteUri + "/" + Constants.DEFAULT_PATH + "/" + Constants.JSON;

            resultMap.put(remoteUri, JSONUtils.json2map(jsonPost(uri, postParams)));
        }

        return JSONUtils.obj2json(resultMap);
    }

    /**
     * call rpc with post
     */
    private static String jsonPost(String uri, HttpEntity postParams) throws IOException {
        System.out.println("调用：" + uri);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(postParams);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        System.out.println("POST Response Status:: " + httpResponse.getStatusLine().getStatusCode());
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }

        reader.close();
        httpResponse.close();
        httpClient.close();

        return response.toString();
    }


    /**
     * call rpc : one cmd, one action
     */
    public static byte[] byteSingleRpc(String cmd, Object[] params, double minVersion) throws Exception {

        RpcCmd rpcCmd = new RpcCmd(RuntimeParam.sequence.incrementAndGet(), cmd, minVersion, params);

        List<String> remoteUriList = CallCmd.getRemoteUri(rpcCmd);
        if (remoteUriList.size() == 0) {
            throw new Exception("No cmd found" + cmd);
        }
        if (remoteUriList.size() > 1) {
            throw new Exception("Multiply cmd found->" + cmd);
        }

        HttpPost httpPost = new HttpPost(remoteUriList.get(0) + "/" + Constants.DEFAULT_PATH + "/" + Constants.BYTE);
        httpPost.setEntity(new ByteArrayEntity(CallCmd.obj2Bytes(rpcCmd)));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            HttpEntity entityResponse = httpResponse.getEntity();
            int contentLength = (int) entityResponse.getContentLength();
            if (contentLength <= 0) {
                throw new IOException("No response");
            }
            byte[] respBuffer = new byte[contentLength];
            if (entityResponse.getContent().read(respBuffer) != respBuffer.length) {
                throw new IOException("Read response buffer error");
            }
            return respBuffer;
        }
    }
}
