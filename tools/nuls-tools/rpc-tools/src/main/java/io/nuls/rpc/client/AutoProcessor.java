package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 处理客户端收到的消息
 * Processing messages received by clients
 *
 * @author tangyi
 * @date 2018/11/26
 * @description
 */
public class AutoProcessor implements Runnable {
    /**
     * 消费从服务端获取的消息
     * Consume the messages from servers
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        do {
            try {

                /*
                获取队列中的第一个对象，如果是空，舍弃
                Get the first item of the queue, If it is an empty object, discard
                 */
                Message message = ClientRuntime.firstMessageInResponseAutoQueue();
                if (message == null) {
                    Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
                    continue;
                }

                /*
                获取Response对象，这里得到的对象一定是需要自动调用本地方法
                Get Response object, The object you get here must automatically call the local method
                 */
                Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                String messageId = response.getRequestId();

                /*
                如果需要自动调用，则自动调用本地方法
                If need to invoke local method automatically, do it
                 */
                Object[] objects = ClientRuntime.INVOKE_MAP.get(messageId);
                Class clazz = (Class) objects[0];
                String invokeMethod = (String) objects[1];

                @SuppressWarnings("unchecked") Constructor constructor = clazz.getConstructor();
                @SuppressWarnings("unchecked") Method method = clazz.getDeclaredMethod(invokeMethod, Object.class);
                method.invoke(constructor.newInstance(), response);

                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            } catch (Exception e) {
                Log.error(e);
            }
        } while (true);
    }
}
