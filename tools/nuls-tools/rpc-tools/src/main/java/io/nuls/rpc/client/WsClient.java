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

import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * WebSocketClient的实现类
 * Implementation Class of WebSocketClient
 *
 * @author tangyi
 * @date 2018/10/30
 */
public class WsClient extends WebSocketClient {


    WsClient(String url) throws URISyntaxException {
        super(new URI(url));
    }

    @Override
    public void onOpen(ServerHandshake shake) {
    }

    @Override
    public void onMessage(String msg) {
        try {
            /*
            收到的所有消息都放入队列，等待其他线程处理
            All messages received are queued, waiting for other threads to process
             */
            Message message = JSONUtils.json2pojo(msg, Message.class);
            switch (MessageType.valueOf(message.getMessageType())) {
                case NegotiateConnectionResponse:
                    ClientRuntime.NEGOTIATE_RESPONSE_QUEUE.offer(message);
                    break;
                case Ack:
                    ClientRuntime.ACK_QUEUE.offer(message);
                    break;
                case Response:
                    Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                    if (ClientRuntime.INVOKE_MAP.containsKey(response.getRequestId())) {
                        ClientRuntime.RESPONSE_AUTO_QUEUE.offer(message);
                    } else {
                        ClientRuntime.RESPONSE_MANUAL_QUEUE.offer(message);
                    }
                    break;
                default:
                    break;
            }
            Log.info("ClientMsgFrom<" + this.getRemoteSocketAddress().getHostString() + ":" + this.getRemoteSocketAddress().getPort() + ">: " + msg);
        } catch (IOException e) {
            Log.error(e);
        }
    }

    @Override
    public void onClose(int paramInt, String paramString, boolean paramBoolean) {
    }

    @Override
    public void onError(Exception e) {
        Log.error(e);
    }


    // TODO 增加一个重连机制
}