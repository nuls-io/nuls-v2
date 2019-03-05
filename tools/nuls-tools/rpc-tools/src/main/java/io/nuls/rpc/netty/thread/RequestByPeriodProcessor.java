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
package io.nuls.rpc.netty.thread;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.processor.RequestMessageProcessor;
import io.nuls.tools.log.Log;

/**
 * 订阅定时返回数据处理线程
 * Subscription event processing threads
 *
 * @author tag
 * 2019/2/25
 * */
public class RequestByPeriodProcessor implements Runnable {

    private ConnectData connectData;

    public  RequestByPeriodProcessor(ConnectData connectData){
        this.connectData = connectData;
    }

    /**
     * 轮流根据Period和EventCount定时推送消息
     * Push messages on a periodic and EventCount basis in turn
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (connectData.isConnected()) {
            try {
                if (!connectData.getRequestPeriodLoopQueue().isEmpty()) {
                    sendPeriodQueue();
                }
                Thread.sleep(Constants.PROCESSOR_INTERVAL_TIMEMILLIS);
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
    private void sendPeriodQueue() {
        /*
        获取队列中的第一个对象
        Get the first item of the queue
         */
        Object[] objects = connectData.getRequestPeriodLoopQueue().poll();
        Message message = (Message) objects[0];
        Request request = (Request) objects[1];

        /*
        需要继续发送，添加回队列
        Need to continue sending, add back to queue
         */
        boolean isContinue = RequestMessageProcessor.responseWithPeriod(connectData, message, request);
        if (isContinue) {
            connectData.getRequestPeriodLoopQueue().offer(objects);
        }
    }
}
