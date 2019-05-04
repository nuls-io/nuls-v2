package io.nuls.core.rpc.netty.thread;

import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.processor.RequestMessageProcessor;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.log.Log;

/**
 * 订阅事件处理线程
 * Subscription event processing threads
 *
 * @author tag
 * 2019/2/25
 */
public class RequestByCountProcessor implements Runnable {
    private ConnectData connectData;

    public RequestByCountProcessor(ConnectData connectData) {
        this.connectData = connectData;
    }

    /**
     * 发送订阅的数据队列
     * Data queue for sending subscriptions
     */
    @Override
    public void run() {
        while (connectData.isConnected()) {
            try {
                Response response = connectData.getRequestEventResponseQueue().take();
                if (response.getRequestID() == null) {
                    continue;
                }
                RequestMessageProcessor.responseWithEventCount(connectData.getChannel(), response);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}
