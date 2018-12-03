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

package io.nuls.rpc.server;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.RegisterApi;
import io.nuls.tools.log.Log;
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
 * @description
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
            Log.info("Handshake success." + ServerRuntime.local.getModuleName() + " ready!");
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
            /*
            收到的所有消息都放入队列，等待其他线程处理
            All messages received are queued, waiting for other threads to process
             */
            Log.info("ServerMsgFrom<" + webSocket.getRemoteSocketAddress().getHostString() + ":" + webSocket.getRemoteSocketAddress().getPort() + ">: " + msg);
            ServerRuntime.CLIENT_MESSAGE_QUEUE.add(new Object[]{webSocket, msg});
            ServerRuntime.serverThreadPool.execute(new ServerProcessor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        Log.error(ex);
    }

    @Override
    public void onStart() {
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
