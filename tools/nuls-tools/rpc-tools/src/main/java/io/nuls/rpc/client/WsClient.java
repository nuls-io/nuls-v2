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

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.log.Log;
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
        try {
            /*
             * add to response queue, Waiting for thread pool processing
             */
            RuntimeInfo.RESPONSE_QUEUE.add(JSONUtils.json2map(paramString));
        } catch (IOException e) {
            Log.error("WsClient.onMessage-> " + e.getMessage() + ":" + paramString);
        }
    }

    @Override
    public void onClose(int paramInt, String paramString, boolean paramBoolean) {
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }

    /**
     * get response by id
     */
    public Map getResponse(int id) throws InterruptedException, IOException {
        long timeMillis = System.currentTimeMillis();
        do {
            for (Map map : RuntimeInfo.RESPONSE_QUEUE) {
                if ((Integer) map.get("id") == id) {
                    RuntimeInfo.RESPONSE_QUEUE.remove(map);
                    return map;
                }
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        } while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS);

        return buildCmdResponseMap(Constants.RESPONSE_TIMEOUT);
    }

    private static Map buildCmdResponseMap(String msg) throws IOException {
        CmdResponse cmdResponse = new CmdResponse();
        cmdResponse.setCode(Constants.FAILED_CODE);
        cmdResponse.setMsg(msg);
        return JSONUtils.json2map(JSONUtils.obj2json(cmdResponse));
    }
}