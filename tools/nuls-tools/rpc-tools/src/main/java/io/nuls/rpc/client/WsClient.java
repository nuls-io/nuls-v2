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

import io.nuls.rpc.info.RuntimeParam;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/30
 * @description
 */

public class WsClient extends WebSocketClient {


    public WsClient(String url) throws URISyntaxException {
        super(new URI(url));
    }

    @Override
    public void onOpen(ServerHandshake shake) {

    }

    @Override
    public void onMessage(String paramString) {
        System.out.println("ws client-> receive：" + paramString);
        try {
            Map map = JSONUtils.json2map(paramString);

            RuntimeParam.resultQueue.add(map);
            System.out.println("ws client-> add to map, id=" + map.get("id") + ",size=" + RuntimeParam.resultQueue.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int paramInt, String paramString, boolean paramBoolean) {
        System.out.println("关闭...");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("异常" + e);
    }

    public Map wsResponse(int id) throws InterruptedException {
        while (true) {
            for (int i = 0; i < RuntimeParam.resultQueue.size(); i++) {
                Map map = RuntimeParam.resultQueue.get(i);
                if ((Integer) map.get("id") == id) {
                    RuntimeParam.resultQueue.remove(map);
                    System.out.println("ws client-> get response,id=" + id + ", size=" + RuntimeParam.resultQueue.size());
                    return map;
                }
            }
            Thread.sleep(100);
        }
    }
}