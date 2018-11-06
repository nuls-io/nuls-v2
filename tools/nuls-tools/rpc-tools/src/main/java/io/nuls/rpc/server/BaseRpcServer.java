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

import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.Module;
import io.nuls.rpc.model.ModuleStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/18
 * @description
 */
public abstract class BaseRpcServer {
    private String addr;
    private int port;

    BaseRpcServer() {
        this.addr = HostInfo.getIpAddLocally();
        this.port = HostInfo.randomPort();

        init();
    }

    BaseRpcServer(int port) {
        this.addr = HostInfo.getIpAddLocally();
        this.port = port;

        init();
    }

    private void init() {
        RuntimeInfo.local = new Module("", ModuleStatus.READY, false, "", 0, new ArrayList<>(), new ArrayList<>());
    }

    String getBaseUri() {
        return "http://" + addr + ":" + port + "/";
    }

    public void init(String moduleName, List<String> depends) {
        RuntimeInfo.local.setName(moduleName);
        RuntimeInfo.local.setDependsModule(depends);
        RuntimeInfo.local.setAddr(getAddr());
        RuntimeInfo.local.setPort(getPort());
        RuntimeInfo.local.setStatus(ModuleStatus.READY);
    }

    public String getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }



    /**
     * server 启动入口，不同server不同实现
     * 也允许自定义server
     */
    public abstract void start();
}
