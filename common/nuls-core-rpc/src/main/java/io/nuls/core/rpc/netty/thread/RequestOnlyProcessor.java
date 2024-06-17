package io.nuls.core.rpc.netty.thread;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.model.RequestOnly;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.processor.RequestMessageProcessor;
/**
 * Request processing thread that does not require a receipt
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
     * Consumption of messages obtained from the server
     * Consume the messages from servers
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (connectData.isConnected()) {
            try {
                /*
                Get the first object in the queue
                Get the first item of the queue
                 */
                RequestOnly requestOnly = connectData.getRequestOnlyQueue().take();
                connectData.subRequestOnlyQueueMemSize(requestOnly.getMessageSize());
                RequestMessageProcessor.callCommands(requestOnly.getRequest().getRequestMethods());
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }
}
