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

import io.nuls.rpc.info.RpcConstant;
import io.nuls.rpc.info.RpcInfo;
import io.nuls.rpc.model.Module;
import io.nuls.rpc.model.RpcCmd;
import io.nuls.tools.parse.JSONUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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

    public static String versionToKernel(String kernelUri) throws Exception {
        RpcCmd rpcCmd = new RpcCmd("version", 1.0, new Object[]{RpcInfo.local});

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(RpcConstant.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

        return post(kernelUri, postParams);
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
        RpcCmd rpcCmd = new RpcCmd("fetch", 1.0, new Object[]{RpcInfo.local});

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(RpcConstant.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

        String fetchString = post(kernelUri, postParams);

        return fetch2Map(fetchString);
    }

    private static Map<String, Object> fetch2Map(String fetchString) throws IOException {
        System.out.println(fetchString);

        Map<String, Object> map1 = JSONUtils.json2map(fetchString);

        Map<String, Object> result = JSONUtils.json2map(JSONUtils.obj2json(map1.get("result")));
        System.out.println("available" + JSONUtils.obj2json(result.get("available")));

        Map<String, Object> moduleMap = JSONUtils.json2map(JSONUtils.obj2json(result.get("modules")));
        for (Object key : moduleMap.keySet()) {
            System.out.println(key);
            System.out.println(moduleMap.get(key));
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(moduleMap.get(key)), Module.class);
            RpcInfo.remoteModuleMap.put((String) key, module);
        }
        result.put("available", result.get("available"));
        result.put("modules", RpcInfo.remoteModuleMap);

        Map<String, Object> fetchMap = new HashMap<>(16);
        fetchMap.put("code", map1.get("code"));
        fetchMap.put("msg", map1.get("msg"));
        fetchMap.put("result", result);

        return fetchMap;
    }

    public static String callSingleRpc(String cmd, Object[] params, double minVersion) throws IOException {

        RpcCmd rpcCmd = new RpcCmd(cmd, minVersion, params);

        List<String> remoteUriList = RpcInfo.getRemoteUri(rpcCmd);
        if (remoteUriList.size() == 0) {
            return "No cmd found->" + cmd + "." + minVersion;
        }
        if (remoteUriList.size() > 1) {
            return "Multiply cmd found->" + cmd;
        }

        String remoteUri = remoteUriList.get(0);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(RpcConstant.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

        String uri = remoteUri + "/" + RpcConstant.DEFAULT_PATH + "/" + RpcConstant.SINGLE;

        return post(uri, postParams);
    }

    private static String post(String uri, HttpEntity postParams) throws IOException {
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
}
