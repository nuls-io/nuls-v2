package io.nuls.rpc.server.thread;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.server.handler.CmdHandler;
import io.nuls.rpc.server.runtime.WsData;
import io.nuls.tools.log.Log;

public class RequestByCountProcessor implements Runnable{
    private WsData wsData;

    public  RequestByCountProcessor(WsData wsData){
        this.wsData = wsData;
    }

    /**
     * 发送订阅的数据队列
     * Data queue for sending subscriptions
     * */
    @Override
    public void run() {
        while (wsData.isConnected()) {
            try {
                if(!wsData.getRequestEventResponseQueue().isEmpty()){
                    CmdHandler.responseWithEventCount(wsData.getWebSocket(),wsData.getRequestEventResponseQueue().poll());
                }
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}
