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
package io.nuls.rpc.server.thread;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.server.handler.CmdHandler;
import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.WebSocket;

import java.util.Map;

/**
 * 处理客户端消息的线程
 * Threads handling client messages
 *
 * @author tangyi
 * @date 2018/11/7
 */
public class RequestLoopProcessor implements Runnable {

    /**
     * 轮流根据Period和EventCount定时推送消息
     * Push messages on a periodic and EventCount basis in turn
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {

        while (true) {
            try {

                sendPeriodQueue();

                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }

    /**
     * 发送队列的第一个对象，然后放入队列尾
     * Send the first object of the queue and put it at the end of the queue
     *
     * @throws Exception 抛出任何异常 / Throw any exception
     */
    private void sendPeriodQueue() throws Exception {
        /*
        获取队列中的第一个对象
        Get the first item of the queue
         */
        Object[] objects = ServerRuntime.REQUEST_PERIOD_LOOP_QUEUE.take();

        WebSocket webSocket = (WebSocket) objects[0];
        String msg = (String) objects[1];

        Message message = JSONUtils.json2pojo(msg, Message.class);
        Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);

        /*
        需要继续发送，添加回队列
        Need to continue sending, add back to queue
         */
        boolean isContinue = CmdHandler.responseWithPeriod(webSocket, message.getMessageId(), request);
        if (isContinue) {
            ServerRuntime.REQUEST_PERIOD_LOOP_QUEUE.offer(new Object[]{webSocket, JSONUtils.obj2json(message)});
        }
    }
}
