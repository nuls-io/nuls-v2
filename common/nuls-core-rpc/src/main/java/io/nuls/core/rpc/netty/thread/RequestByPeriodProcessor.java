/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.core.rpc.netty.thread;

import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.processor.RequestMessageProcessor;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.Message;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.log.Log;

/**
 * Subscription scheduled return data processing thread
 * Subscription event processing threads
 *
 * @author tag
 * 2019/2/25
 */
public class RequestByPeriodProcessor implements Runnable {

    private ConnectData connectData;

    public RequestByPeriodProcessor(ConnectData connectData) {
        this.connectData = connectData;
    }

    /**
     * Take turns according toPeriodandEventCountTimed push messages
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
     * Send the first object in the queue and put it at the end of the queue
     * Send the first object of the queue and put it at the end of the queue
     */
    private void sendPeriodQueue() {
        /*
        Get the first object in the queue
        Get the first item of the queue
         */
        Object[] objects = connectData.getRequestPeriodLoopQueue().poll();
        Message message = (Message) objects[0];
        Request request = (Request) objects[1];

        /*
        Need to continue sending and add back to the queue
        Need to continue sending, add back to queue
         */
        boolean isContinue = RequestMessageProcessor.responseWithPeriod(connectData, message, request);
        if (isContinue) {
            connectData.getRequestPeriodLoopQueue().offer(objects);
        }
    }
}
