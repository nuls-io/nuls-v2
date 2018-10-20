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
import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RpcClient {

    public static String callJoinKernel(String kernelUri) throws Exception {
        RpcCmd rpcCmd = new RpcCmd("join", 1.0, new Object[]{RpcInfo.local});

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(RpcConstant.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

        return post(kernelUri, postParams);
    }

    public static String callFetchKernel(String kernelUri) throws Exception {
        RpcCmd rpcCmd = new RpcCmd("fetch", 1.0, new Object[]{RpcInfo.local});

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(RpcConstant.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);

        return post(kernelUri, postParams);
    }

    public static String callSingleRpc(String cmd, Object[] param, double minVersion) throws IOException {

        RpcCmd rpcCmd = new RpcCmd(cmd, minVersion, param);

        List<String> remoteUriList = RpcInfo.getRemoteUri(rpcCmd);
        if (remoteUriList.size() == 0) {
            return "No cmd found->" + cmd;
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
