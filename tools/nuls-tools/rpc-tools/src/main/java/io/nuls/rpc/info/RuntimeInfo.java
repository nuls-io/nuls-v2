/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.rpc.info;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.handler.WebSocketHandler;
import io.nuls.rpc.model.*;
import io.nuls.rpc.model.message.*;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.data.DateUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.TimeService;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RuntimeInfo {


    /**
     * local module(io.nuls.rpc.Module) information
     */
    public static ModuleInfo local = new ModuleInfo();

    /**
     * remote module information
     * key: module name/code
     * value: moduleInfo(io.nuls.rpc.ModuleInfo)
     */
    public static ConcurrentMap<String, ModuleInfo> remoteModuleMap = new ConcurrentHashMap<>();

    /**
     * local Config item information
     */
    public static Map<String, ConfigItem> configItemMap = new ConcurrentHashMap<>();

    /**
     * cmd sequence
     */
    public static AtomicInteger sequence = new AtomicInteger(0);

    /**
     * Kernel URL
     */
    public static String kernelUrl;


    /**
     * The pending request command received through RPC
     * Array [0] is the Websocket object for communication
     * Array [1] is the content of the communication
     */
    public static final List<Object[]> REQUEST_QUEUE = Collections.synchronizedList(new ArrayList<>());

    /**
     * The thread pool object that handles the request
     */
    public static ExecutorService fixedThreadPool = ThreadUtils.createThreadPool(5, 500, new NulsThreadFactory("handRequest"));
    //    public static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    /**
     * The response of the cmd invoked through RPC
     */
    public static final List<Map> RESPONSE_QUEUE = Collections.synchronizedList(new ArrayList<>());

    /**
     * WsClient object that communicates with other modules
     * key: uri(ex: ws://127.0.0.1:8887)
     * value: WsClient
     */
    private static ConcurrentMap<String, WsClient> wsClientMap = new ConcurrentHashMap<>();

    /**
     * Get the WsClient object through the url
     */
    public static WsClient getWsClient(String uri) throws Exception {

        if (!wsClientMap.containsKey(uri)) {
            WsClient wsClient = new WsClient(uri);
            wsClient.connect();
            Thread.sleep(1000);
            if (wsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
                wsClientMap.put(uri, wsClient);
            } else {
                Log.info("Failed to connect " + uri);
            }
        }
        return wsClientMap.get(uri);
    }

    /**
     * get the next call counter(unique identifier)
     */
    public static int nextSequence() {
        return sequence.incrementAndGet();
    }


    /**
     * Get the url of the module that provides the cmd through the CmdRequest object
     * The resulting url may not be unique, returning all found
     */
    public static String getRemoteUri(String cmd) {
        for (ModuleInfo moduleInfo : RuntimeInfo.remoteModuleMap.values()) {
            for (CmdDetail cmdDetail : moduleInfo.getRegisterApi().getApiMethods()) {
                if (cmdDetail.getMethodName().equals(cmd)) {
                    return "ws://" + moduleInfo.getRegisterApi().getAddress() + ":" + moduleInfo.getRegisterApi().getPort();
                }
            }
        }
        return null;
    }

    /**
     * Get local command
     * Sort by version number
     * 1. Not less than the incoming version number
     * 2. Forward compatible
     * 3. The highest version that meet conditions 1 and 2 at the same time
     */
    public static CmdDetail getLocalInvokeCmd(String cmd, double minVersion) {

        RuntimeInfo.local.getRegisterApi().getApiMethods().sort(Comparator.comparingDouble(CmdDetail::getVersion));

        CmdDetail find = null;
        for (CmdDetail cmdDetail : RuntimeInfo.local.getRegisterApi().getApiMethods()) {
            if (!cmdDetail.getMethodName().equals(cmd)) {
                continue;
            }
            if ((int) minVersion != (int) cmdDetail.getVersion()) {
                continue;
            }
            if (find == null) {
                find = cmdDetail;
                continue;
            }

            if (cmdDetail.getVersion() > find.getVersion()) {
                find = cmdDetail;
            }
        }
        return find;
    }

    /**
     * Get local command
     * Sort by version number
     * The highest version
     */
    public static CmdDetail getLocalInvokeCmd(String cmd) {

        RuntimeInfo.local.getRegisterApi().getApiMethods().sort(Comparator.comparingDouble(CmdDetail::getVersion));

        CmdDetail find = null;
        for (CmdDetail cmdDetail : RuntimeInfo.local.getRegisterApi().getApiMethods()) {
            if (!cmdDetail.getMethodName().equals(cmd)) {
                continue;
            }

            if (find == null) {
                find = cmdDetail;
                continue;
            }

            if (cmdDetail.getVersion() > find.getVersion()) {
                find = cmdDetail;
            }
        }
        return find;
    }

    /**
     * Scan the provided package
     * Analysis annotation, register cmd
     */
    public static void scanPackage(String packageName) throws Exception {
        if (packageName == null || packageName.length() == 0) {
            return;
        }
        List<Class> classList = ScanUtil.scan(packageName);
        for (Class clz : classList) {
            Method[] methods = clz.getMethods();
            for (Method method : methods) {
                CmdDetail cmdDetail = annotation2CmdDetail(method);
                if (cmdDetail == null) {
                    continue;
                }

                if (!isRegister(cmdDetail)) {
                    RuntimeInfo.local.getRegisterApi().getApiMethods().add(cmdDetail);
                } else {
                    throw new Exception(Constants.CMD_DUPLICATE + ":" + cmdDetail.getMethodName() + "-" + cmdDetail.getVersion());
                }
            }
        }
    }

    /**
     * Get annotation of methods
     * If the annotation is CmdAnnotation, it means that the cmd needs to be registered
     */
    private static CmdDetail annotation2CmdDetail(Method method) {
        CmdDetail cmdDetail = null;
        List<CmdParameter> cmdParameters = new ArrayList<>();
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (CmdAnnotation.class.getName().equals(annotation.annotationType().getName())) {
                CmdAnnotation cmdAnnotation = (CmdAnnotation) annotation;
                cmdDetail = new CmdDetail();
                cmdDetail.setMethodName(cmdAnnotation.cmd());
                cmdDetail.setMethodDescription(cmdAnnotation.description());
                cmdDetail.setMethodMinEvent(cmdAnnotation.minEvent());
                cmdDetail.setMethodMinPeriod(cmdAnnotation.minPeriod());
                cmdDetail.setMethodScope(cmdAnnotation.scope());
                cmdDetail.setVersion(cmdAnnotation.version());
                cmdDetail.setInvokeClass(method.getDeclaringClass().getName());
                cmdDetail.setInvokeMethod(method.getName());
            }
            if (Parameters.class.getName().equals(annotation.annotationType().getName())) {
                Parameters parameters = (Parameters) annotation;
                for (Parameter parameter : parameters.value()) {
                    CmdParameter cmdParameter = new CmdParameter(parameter.parameterName(), parameter.parameterType(), parameter.parameterValidRange(), parameter.parameterValidRegExp());
                    cmdParameters.add(cmdParameter);
                }
            }
        }
        if (cmdDetail == null) {
            return null;
        }
        cmdDetail.setParameters(cmdParameters);

        return cmdDetail;
    }

    /**
     * Determine if the cmd has been registered
     * 1. The same cmd
     * 2. The same version
     */
    private static boolean isRegister(CmdDetail sourceCmdDetail) {
        boolean exist = false;
        for (CmdDetail cmdDetail : RuntimeInfo.local.getRegisterApi().getApiMethods()) {
            if (cmdDetail.getMethodName().equals(sourceCmdDetail.getMethodName()) && cmdDetail.getVersion() == sourceCmdDetail.getVersion()) {
                exist = true;
                break;
            }
        }

        return exist;
    }

    public static Map buildCmdResponseMap(int id, String msg) throws IOException {
        CmdResponse cmdResponse = new CmdResponse();
        cmdResponse.setId(id);
        cmdResponse.setCode(Constants.FAILED_CODE);
        cmdResponse.setMsg(msg);
        return JSONUtils.json2map(JSONUtils.obj2json(cmdResponse));
    }

    public static Message buildMessage(int messageId, MessageType messageType) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setMessageType(messageType.name());
        message.setTimestamp(TimeService.currentTimeMillis());
        message.setTimezone(DateUtils.getTimeZone());
        return message;
    }

    public static NegotiateConnection defaultNegotiateConnection() {
        NegotiateConnection negotiateConnection = new NegotiateConnection();
        negotiateConnection.setCompressionAlgorithm("zlib");
        negotiateConnection.setCompressionRate(0);
        return negotiateConnection;
    }

    public static void negotiateConnectionResponse(WebSocket webSocket) throws JsonProcessingException {
        NegotiateConnectionResponse negotiateConnectionResponse = new NegotiateConnectionResponse();
        negotiateConnectionResponse.setNegotiationStatus(0);
        negotiateConnectionResponse.setNegotiationComment("Incompatible protocol version");

        Message rspMsg = RuntimeInfo.buildMessage(RuntimeInfo.nextSequence(), MessageType.NegotiateConnectionResponse);
        rspMsg.setMessageData(negotiateConnectionResponse);
        webSocket.send(JSONUtils.obj2json(rspMsg));
    }

    public static void response(WebSocket webSocket, Map<String, Object> messageMap) throws Exception {
        int messageId = (Integer) messageMap.get("messageId");
        Map messageData = (Map) messageMap.get("messageData");
        Map requestMethods = (Map) messageData.get("requestMethods");
        for (Object method : requestMethods.keySet()) {
            Response response = defaultResponse(messageId);

            Map params = (Map) requestMethods.get(method);

            CmdDetail cmdDetail = params == null || params.get(Constants.VERSION_KEY_STR) == null
                    ? RuntimeInfo.getLocalInvokeCmd((String) method)
                    : RuntimeInfo.getLocalInvokeCmd((String) method, Double.parseDouble(params.get(Constants.VERSION_KEY_STR).toString()));
            Message message;
            if (cmdDetail == null) {
                response.setResponseProcessingTime(TimeService.currentTimeMillis() - response.getResponseProcessingTime());
                response.setResponseData("No such version: " + method + "," + params.get(Constants.VERSION_KEY_STR));
                message = RuntimeInfo.buildMessage(RuntimeInfo.nextSequence(), MessageType.Response);
                message.setMessageData(response);
                webSocket.send(JSONUtils.obj2json(message));
                continue;
            }

            Object responseData = WebSocketHandler.invoke(cmdDetail.getInvokeClass(), cmdDetail.getInvokeMethod(), params);

            response.setResponseProcessingTime(TimeService.currentTimeMillis() - response.getResponseProcessingTime());
            response.setResponseData(responseData);

            message = RuntimeInfo.buildMessage(RuntimeInfo.nextSequence(), MessageType.Response);
            message.setMessageData(response);
            webSocket.send(JSONUtils.obj2json(message));
        }
    }

    public static void unsubscribe(){

    }

    public static Request defaultRequest() {
        Request request = new Request();
        request.setRequestAck(0);
        request.setSubscriptionEventCounter(0);
        request.setSubscriptionPeriod(0);
        request.setSubscriptionRange("");
        request.setResponseMaxSize(0);
        request.setRequestMethods(new HashMap<>(16));
        return request;
    }

    private static Response defaultResponse(int requestId) {
        Response response = new Response();
        response.setRequestId(requestId);
        response.setResponseProcessingTime(TimeService.currentTimeMillis());
        response.setResponseStatus(1);
        response.setResponseComment("Congratulations! Processing completed！");
        response.setResponseMaxSize(0);
        return response;
    }


}
