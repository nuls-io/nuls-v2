package io.nuls.core.rpc.netty.thread;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.processor.RequestMessageProcessor;
/**
 * 不需要回执的请求处理线程
 * Request processing threads that do not require a receipt
 *
 * @author tag
 * @date 2019/6/13
 */
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
                Log.info("-----------------------消息堆积数量：{}" , connectData.getRequestOnlyQueue().size());
                RequestMessageProcessor.callCommands(request.getRequestMethods());
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}
