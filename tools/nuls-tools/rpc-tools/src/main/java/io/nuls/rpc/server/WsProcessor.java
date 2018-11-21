package io.nuls.rpc.server;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdDetail;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
public class WsProcessor implements Runnable {
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

        while (RuntimeInfo.REQUEST_QUEUE.size() > 0) {

            Object[] objects = null;
            synchronized (RuntimeInfo.REQUEST_QUEUE) {
                if (RuntimeInfo.REQUEST_QUEUE.size() > 0) {
                    objects = RuntimeInfo.REQUEST_QUEUE.get(0);
                    RuntimeInfo.REQUEST_QUEUE.remove(0);
                }
            }

            try {
                if (objects != null) {
                    WebSocket webSocket = (WebSocket) objects[0];
                    String msg = (String) objects[1];

                    Map<String, Object> message;
                    try {
                        message = JSONUtils.json2map(msg);
                    } catch (IOException e) {
                        Log.error(e);
                        Log.error("Message【" + msg + "】 doesn't match the rule, Discard!");
                        continue;
                    }

                    MessageType messageType = MessageType.valueOf(message.get("messageType").toString());
                    int messageId = (Integer) message.get("messageId");
                    Message rspMsg;
                    switch (messageType) {
                        case NegotiateConnection:
                            rspMsg = RuntimeInfo.buildMessage(RuntimeInfo.nextSequence(), MessageType.NegotiateConnectionResponse);
                            rspMsg.setMessageData(RuntimeInfo.defaultNegotiateConnectionResponse());
                            webSocket.send(JSONUtils.obj2json(rspMsg));
                            break;
                        case Request:
                            Map messageData = (Map) message.get("messageData");
                            Map requestMethods = (Map) messageData.get("requestMethods");
                            for (Object method : requestMethods.keySet()) {
                                Response response = RuntimeInfo.defaultResponse(messageId);

                                Map params = (Map) requestMethods.get(method);

                                CmdDetail cmdDetail = params == null || params.get(Constants.VERSION_KEY_STR) == null
                                        ? RuntimeInfo.getLocalInvokeCmd((String) method)
                                        : RuntimeInfo.getLocalInvokeCmd((String) method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));
                                Object responseData = buildResponse(cmdDetail.getInvokeClass(), cmdDetail.getInvokeMethod(), params);

                                response.setResponseProcessingTime(TimeService.currentTimeMillis() - response.getResponseProcessingTime());
                                response.setResponseData(responseData);

                                rspMsg = RuntimeInfo.buildMessage(RuntimeInfo.nextSequence(), MessageType.Response);
                                rspMsg.setMessageData(response);
                                webSocket.send(JSONUtils.obj2json(rspMsg));
                            }
                            break;
                        default:
                            break;

                    }
                }
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call local cmd.
     * 1. If the interface is injected via @Autowired, the injected object is used
     * 2. If the interface has no special annotations, construct a new object by reflection
     */
    private static Object buildResponse(String invokeClass, String invokeMethod, Map params) throws Exception {

        Class clz = Class.forName(invokeClass);
        @SuppressWarnings("unchecked") Method method = clz.getDeclaredMethod(invokeMethod, Map.class);

        BaseCmd cmd;
        if (SpringLiteContext.getBeanByClass(invokeClass) == null) {
            @SuppressWarnings("unchecked") Constructor constructor = clz.getConstructor();
            cmd = (BaseCmd) constructor.newInstance();
        } else {
            cmd = (BaseCmd) SpringLiteContext.getBeanByClass(invokeClass);
        }

        return method.invoke(cmd, params);
    }
}
