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

import io.nuls.rpc.handler.WebSocketHandler;
import io.nuls.rpc.info.CallCmd;
import io.nuls.rpc.info.IpPortInfo;
import io.nuls.rpc.info.RuntimeParam;
import io.nuls.rpc.info.WsPool;
import io.nuls.rpc.model.Module;
import io.nuls.rpc.model.ModuleStatus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class WsServer extends WebSocketServer {
    public WsServer(int port) {
        super(new InetSocketAddress(port));
    }

    public void init(String moduleName, List<String> depends, String scanPackage) throws Exception {
        RuntimeParam.local = new Module("", ModuleStatus.READY, false, "", 0, new ArrayList<>(), new ArrayList<>());
        RuntimeParam.local.setName(moduleName);
        RuntimeParam.local.setDependsModule(depends);
        RuntimeParam.local.setAddr(IpPortInfo.getIpAdd());
        RuntimeParam.local.setPort(getPort());
        RuntimeParam.local.setStatus(ModuleStatus.READY);

        CallCmd.scanPackage(scanPackage);
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake handshake) {
        System.out.println("ws server-> new connection join");
        System.out.println(webSocket.getRemoteSocketAddress().getHostName() + ":" + webSocket.getRemoteSocketAddress().getPort());
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        //断开连接时候触发代码
        WsPool.removeClient(webSocket);
        System.out.println(reason);
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        try {
            System.out.println("ws server-> receive msg: " + message);
            webSocket.send(WebSocketHandler.callCmd(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        //错误时候触发的代码
        System.out.println("ws server-> on error");
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("ws server-> started.");
    }

}
