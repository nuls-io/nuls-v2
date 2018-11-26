package io.nuls.rpc.client;

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
        synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
            Map removeMap = null;
            for (Map map : ClientRuntime.CALLED_VALUE_QUEUE) {
                Message message = JSONUtils.map2pojo(map, Message.class);
                if (!MessageType.Response.name().equals(message.getMessageType())) {
                    continue;
                }

                Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                String messageId = response.getRequestId();
                if (ClientRuntime.INVOKE_MAP.containsKey(messageId)) {
                    Object[] objects = ClientRuntime.INVOKE_MAP.get(messageId);
                    Class clazz = (Class) objects[0];
                    String invokeMethod = (String) objects[1];
                    String cmd = (String) objects[2];
                    try {
                        Constructor constructor = clazz.getConstructor();
                        Method method = clazz.getDeclaredMethod(invokeMethod, Object.class);
                        Map responseData = (Map) response.getResponseData();
                        method.invoke(constructor.newInstance(), responseData.get(cmd));
                        removeMap = map;
                        break;
                    } catch (Exception e) {
                        Log.error(e);
                    }
                }
            }
            if (removeMap != null) {
                ClientRuntime.CALLED_VALUE_QUEUE.remove(removeMap);
            }
        }
    }
}
