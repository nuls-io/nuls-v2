package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/26
 * @description
 */
public class ClientProcessor implements Runnable {
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            while (ClientRuntime.SERVER_RESPONSE_QUEUE.size() > 0) {
                /*
                Get the first item of the queue
                If it is an empty object, discard
                 */
                Map map = ClientRuntime.firstItemInServerResponseQueue();
                if (map == null) {
                    continue;
                }

                /*
                Message type should be "Response"
                If not Response, add back to the queue and wait for other threads to process
                 */
                Message message = JSONUtils.map2pojo(map, Message.class);
                if (!MessageType.Response.name().equals(message.getMessageType())) {
                    ClientRuntime.SERVER_RESPONSE_QUEUE.add(map);
                    continue;
                }

                Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                String messageId = response.getRequestId();
                if (ClientRuntime.INVOKE_MAP.containsKey(messageId)) {
                    /*
                    Automatic invoke method
                     */
                    Object[] objects = ClientRuntime.INVOKE_MAP.get(messageId);
                    Class clazz = (Class) objects[0];
                    String invokeMethod = (String) objects[1];

                    @SuppressWarnings("unchecked") Constructor constructor = clazz.getConstructor();
                    @SuppressWarnings("unchecked") Method method = clazz.getDeclaredMethod(invokeMethod, Object.class);
                    method.invoke(constructor.newInstance(), response);

                } else {
                    /*
                    If no need to invoke, add back to the queue and wait for other threads to process
                     */
                    ClientRuntime.SERVER_RESPONSE_QUEUE.add(map);
                }
            }

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
