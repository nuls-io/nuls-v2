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

package io.nuls.rpc;

import io.nuls.rpc.pojo.RpcCmd;
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

    private String remoteUri;
    private CloseableHttpClient httpClient;

    public RpcClient(String remoteIp, int remotePort) {
        this.remoteUri = "http://" + remoteIp + ":" + remotePort + "/";
        httpClient = HttpClients.createDefault();
    }

    public RpcClient(String remoteUri) {
        this.remoteUri = remoteUri;
        httpClient = HttpClients.createDefault();
    }

    /**
     * 默认获取最高版本的接口
     *
     * @param monitorPath:
     * @param cmd:
     * @param param:
     * @return String
     */
    public String callRpc(String monitorPath, String cmd, Object param) {
        return callRpc(monitorPath, cmd, param, 0, 1);
    }

    /**
     * 返回特定版本的接口
     *
     * @param monitorPath:
     * @param cmd:
     * @param param:
     * @param version:
     * @return String
     */
    public String callRpc(String monitorPath, String cmd, Object param, int version) {
        return callRpc(monitorPath, cmd, param, version, 0);
    }

    /**
     * 调用接口
     *
     * @param monitorPath:
     * @param cmd:
     * @param param:
     * @param version:
     * @param lowestVersion:
     * @return String
     */
    private String callRpc(String monitorPath, String cmd, Object param, int version, int lowestVersion) {

        RpcCmd rpcCmd = new RpcCmd();
        rpcCmd.setCmd(cmd);
        rpcCmd.setVersion(version);
        rpcCmd.setLowestVersion(lowestVersion);
        rpcCmd.setParam(param);

        HttpEntity postParams = null;
        List<NameValuePair> urlParameters = new ArrayList<>();
        try {
            urlParameters.add(new BasicNameValuePair(RpcInfo.FORM_PARAM_NAME, JSONUtils.obj2json(rpcCmd)));
            postParams = new UrlEncodedFormEntity(urlParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpPost httpPost = new HttpPost(remoteUri + monitorPath);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(postParams);

        try {
            return post(httpPost);
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private String post(HttpPost httpPost) throws IOException {
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

        return response.toString();
    }
}
