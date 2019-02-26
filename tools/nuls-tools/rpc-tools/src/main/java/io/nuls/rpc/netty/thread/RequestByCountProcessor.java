package io.nuls.rpc.netty.thread;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.processor.RequestMessageProcessor;
import io.nuls.tools.log.Log;

/**
 * 订阅事件处理线程
 * Subscription event processing threads
 *
 * @author tag
 * 2019/2/25
 * */
public class RequestByCountProcessor implements Runnable{
    private ConnectData connectData;

    public  RequestByCountProcessor(ConnectData connectData){
        this.connectData = connectData;
    }

    /**
     * 发送订阅的数据队列
     * Data queue for sending subscriptions
     * */
    @Override
    public void run() {
        while (connectData.isConnected()) {
            try {
                if(!connectData.getRequestEventResponseQueue().isEmpty()){
                    RequestMessageProcessor.responseWithEventCount(connectData.getChannel(),connectData.getRequestEventResponseQueue().poll());
                }
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}
