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

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.Module;
import io.nuls.rpc.model.ModuleStatus;
import io.nuls.tools.log.Log;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void init(String moduleName, String[] depends, String scanPackage) throws Exception {
        List<String> dps = depends == null ? new ArrayList<>() : Arrays.asList(depends);
        RuntimeInfo.local = new Module(moduleName, ModuleStatus.READY, true, HostInfo.getIpAdd(), getPort(), new ArrayList<>(), dps);

        RuntimeInfo.scanPackage(scanPackage);
    }

    public void startAndSyncKernel(String kernelUri) throws Exception {
        this.start();
        Thread.sleep(1000);
        CmdDispatcher.syncKernel(kernelUri);
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake handshake) {
        Log.info("new connection join->" + webSocket.getRemoteSocketAddress().getHostName() + ":" + webSocket.getRemoteSocketAddress().getPort());
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        try {
            Log.info("ws server-> receive msg: " + message);
            RuntimeInfo.REQUEST_QUEUE.add(new Object[]{webSocket, message});
            RuntimeInfo.fixedThreadPool.execute(new WsProcessor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        Log.error("ws server-> on error");
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Log.info("ws server-> started.");
    }

}
