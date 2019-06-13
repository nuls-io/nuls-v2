package io.nuls.core.rpc.netty.thread;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.processor.RequestMessageProcessor;

public class RequestOnlyProcessor implements Runnable{
    private ConnectData connectData;

    public RequestOnlyProcessor(ConnectData connectData) {
        this.connectData = connectData;
    }

    /**
     * 消费从服务端获取的消息
     * Consume the messages from servers
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (connectData.isConnected()) {
            try {
                /*
                获取队列中的第一个对象
                Get the first item of the queue
                 */
                Request request = connectData.getRequestOnlyQueue().take();
                RequestMessageProcessor.callCommands(request.getRequestMethods());
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}
