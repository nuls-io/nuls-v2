package io.nuls.rpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

/**
 * 心跳检测线程
 * Heartbeat detection thread
 *
 * @author tangyi
 * @date 2018/12/4
 */
public class HeartbeatProcessor implements Runnable {

    /**
     * 心跳检测线程。如果握手不成功，则重新连接
     * Heartbeat detection threads. If the handshake is unsuccessful, reconnect
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            for (String url : ClientRuntime.wsClientMap.keySet()) {
                Message message = Constants.basicMessage(Constants.nextSequence(), MessageType.NegotiateConnection);
                message.setMessageData(Constants.defaultNegotiateConnection());
                String jsonMessage;
                try {
                    jsonMessage = JSONUtils.obj2json(message);
                } catch (JsonProcessingException e) {
                    Log.error(e);
                    continue;
                }

                try {
                    WsClient wsClient = ClientRuntime.wsClientMap.get(url);
                    wsClient.send(jsonMessage);
                    Log.info("Heartbeat NegotiateConnection:" + jsonMessage);
                    if (!CmdDispatcher.getNegotiateConnectionResponse()) {
                        ClientRuntime.wsClientMap.remove(url);
                        ClientRuntime.getWsClient(url);
                    }
                } catch (Exception e) {
                    Log.error(e);
                }
            }

            try {
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS * 100);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }
}
