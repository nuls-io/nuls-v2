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

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Map;

/**
 * 处理客户端收到的消息
 * Processing messages received by clients
 *
 * @author tangyi
 * @date 2018/11/26
 * @description
 */
public class ResponseAutoProcessor implements Runnable {
    /**
     * 消费从服务端获取的消息
     * Consume the messages from servers
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            try {

                /*
                获取队列中的第一个对象，如果是空，舍弃
                Get the first item of the queue, If it is an empty object, discard
                 */
                Message message = ClientRuntime.firstMessageInResponseAutoQueue();
                if (message == null) {
                    Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
                    continue;
                }

                /*
                获取Response对象，这里得到的对象一定是需要自动调用本地方法
                Get Response object, The object you get here must automatically call the local method
                 */
                Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                String messageId = response.getRequestId();

                /*
                如果需要自动调用，则自动调用本地方法
                If need to invoke local method automatically, do it
                 */
                BaseInvoke baseInvoke = ClientRuntime.INVOKE_MAP.get(messageId);
                baseInvoke.callBack(response);

                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}
