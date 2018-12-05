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
package io.nuls.rpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

/**
 * 心跳检测线程
 * Heartbeat detection thread
 *
 * @author tangyi
 * @date 2018/12/4
 */
public class HeartbeatProcessor implements Runnable {

    /**
     * 心跳检测线程。如果握手不成功，则重新连接
     * Heartbeat detection threads. If the handshake is unsuccessful, reconnect
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            for (String url : ClientRuntime.wsClientMap.keySet()) {
                Message message = MessageUtil.basicMessage(MessageType.NegotiateConnection);
                message.setMessageData(MessageUtil.defaultNegotiateConnection());
                String jsonMessage;
                try {
                    jsonMessage = JSONUtils.obj2json(message);
                } catch (JsonProcessingException e) {
                    Log.error(e);
                    continue;
                }

                try {
                    WsClient wsClient = ClientRuntime.wsClientMap.get(url);
                    wsClient.send(jsonMessage);
                    if (!CmdDispatcher.receiveNegotiateConnectionResponse()) {
                        ClientRuntime.wsClientMap.remove(url);
                        ClientRuntime.getWsClient(url);
                    }
                } catch (Exception e) {
                    Log.error(e);
                }
            }

            try {
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS * 100);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }
}
