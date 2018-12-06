/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.rpc.server;

import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.client.thread.HeartbeatProcessor;
import io.nuls.rpc.client.thread.ResponseAutoProcessor;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.RegisterApi;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.rpc.server.thread.RequestLoopProcessor;
import io.nuls.rpc.server.thread.RequestSingleProcessor;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocketServer的实现类，同时拥有业务属性
 * WebSocketServer implementation class with business attributes
 *
 * @author tangyi
 * @date 2018/10/30
 */
public class WsServer extends WebSocketServer {
    public WsServer(int port) {
        super(new InetSocketAddress(port));
    }

    /**
     * 连接核心模块（Manager）
     * Connection Core Module (Manager)
     */
    public void connect(String kernelUrl) throws Exception {
        /*
        启动自身服务
        Start service
         */
        this.start();

        /*
        设置核心模块（Manager）连接地址
        Setting the Connection URL of Core Module(Manager)
         */
        Constants.kernelUrl = kernelUrl;

        /*
        与核心模块（Manager）握手
        Shake hands with the core module (Manager)
         */
        if (!CmdDispatcher.handshakeKernel()) {
            throw new Exception("Handshake kernel failed");
        } else {
            Log.info("Connect manager success." + ServerRuntime.local.getModuleName() + " ready!");
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake handshake) {
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket webSocket, String msg) {
        try {

            Message message = JSONUtils.json2pojo(msg, Message.class);
            switch (MessageType.valueOf(message.getMessageType())) {
                case NegotiateConnection:
                    /*
                    握手，直接响应
                     */
                    CmdHandler.negotiateConnectionResponse(webSocket);
                    break;
                case Unsubscribe:
                    /*
                    取消订阅，直接响应
                     */
                    Log.info("UnsubscribeFrom<" + webSocket.getRemoteSocketAddress().getHostString() + ":" + webSocket.getRemoteSocketAddress().getPort() + ">: " + msg);
                    CmdHandler.unsubscribe(webSocket, message);
                    break;
                case Request:
                    /*
                    Request，根据是否需要定时推送放入不同队列，等待处理
                    Request, put in different queues according to the response mode. Wait for processing
                     */
                    Log.info("RequestFrom<" + webSocket.getRemoteSocketAddress().getHostString() + ":" + webSocket.getRemoteSocketAddress().getPort() + ">: " + msg);
                    Request request = JSONUtils.map2pojo((Map) message.getMessageData(), Request.class);

                    if (!ClientRuntime.isPureDigital(request.getSubscriptionEventCounter())
                            && !ClientRuntime.isPureDigital(request.getSubscriptionPeriod())) {
                        ServerRuntime.REQUEST_SINGLE_QUEUE.offer(new Object[]{webSocket, msg});
                    }
                    if (ClientRuntime.isPureDigital(request.getSubscriptionPeriod())) {
                        ServerRuntime.REQUEST_PERIOD_LOOP_QUEUE.offer(new Object[]{webSocket, msg});
                    }
                    if (ClientRuntime.isPureDigital(request.getSubscriptionEventCounter())) {
                        ServerRuntime.REQUEST_EVENT_COUNT_LOOP_QUEUE.offer(new Object[]{webSocket, msg});
                    }


                    /*
                    如果需要一个Ack，则发送
                    Send Ack if needed
                     */
                    if (Constants.BOOLEAN_TRUE.equals(request.getRequestAck())) {
                        CmdHandler.ack(webSocket, message.getMessageId());
                    }
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        Log.error(ex);
    }


    @Override
    public void onStart() {
        Constants.THREAD_POOL.execute(new ResponseAutoProcessor());
        Constants.THREAD_POOL.execute(new RequestSingleProcessor());
        Constants.THREAD_POOL.execute(new RequestLoopProcessor());
        Constants.THREAD_POOL.execute(new HeartbeatProcessor());
        Log.info("Server<" + ServerRuntime.local.getConnectionInformation().get(Constants.KEY_IP) + ":" + ServerRuntime.local.getConnectionInformation().get(Constants.KEY_PORT) + ">-> started.");
    }

    /**
     * 根据预定义模块获得一个服务实例
     * Get a server instance with predefined module
     */
    public static WsServer getInstance(ModuleE moduleE) {
        return getInstance(moduleE.abbr, moduleE.name, moduleE.domain);
    }

    /**
     * 根据参数获得一个服务实例
     * Get a server instance with Abbreviation & Name
     */
    public static WsServer getInstance(String abbr, String name, String domain) {
        WsServer wsServer = new WsServer(HostInfo.randomPort());
        RegisterApi registerApi = new RegisterApi();
        registerApi.setModuleAbbreviation(abbr);
        registerApi.setModuleName(name);
        registerApi.setModuleDomain(domain);
        Map<String, String> connectionInformation = new HashMap<>(2);
        connectionInformation.put(Constants.KEY_IP, HostInfo.getLocalIP());
        connectionInformation.put(Constants.KEY_PORT, wsServer.getPort() + "");
        registerApi.setConnectionInformation(connectionInformation);
        registerApi.setApiMethods(new ArrayList<>());
        registerApi.setDependencies(new HashMap<>(16));
        registerApi.setModuleRoles(new HashMap<>(1));
        ServerRuntime.local = registerApi;

        return wsServer;
    }

    /**
     * 设置本模块的依赖角色
     * Setting Dependent Roles for this Module
     */
    public WsServer dependencies(String key, String value) {
        ServerRuntime.local.getDependencies().put(key, value);
        return this;
    }

    /**
     * 设置本模块的角色（角色名默认为模块编号）
     * Setting up the role of this module(Role name defaults to module code)
     */
    public WsServer moduleRoles(String[] value) {
        ServerRuntime.local.getModuleRoles().put(ServerRuntime.local.getModuleAbbreviation(), value);
        return this;
    }

    /**
     * 设置本模块的角色
     * Setting up the role of this module
     */
    public WsServer moduleRoles(String key, String[] value) {
        ServerRuntime.local.getModuleRoles().put(key, value);
        return this;
    }

    /**
     * 设置模块版本
     * Set module version
     */
    public WsServer moduleVersion(String moduleVersion) {
        ServerRuntime.local.setModuleVersion(moduleVersion);
        return this;
    }

    /**
     * 扫描指定路径，得到所有接口的详细信息
     * Scan the specified path for details of all interfaces
     */
    public WsServer scanPackage(String scanPackage) throws Exception {
        ServerRuntime.scanPackage(scanPackage);
        return this;
    }
}
