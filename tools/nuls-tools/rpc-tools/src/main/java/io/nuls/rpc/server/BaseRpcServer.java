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

import io.nuls.rpc.model.CmdInfo;
import io.nuls.rpc.info.IpPortInfo;
import io.nuls.rpc.info.RpcInfo;
import io.nuls.rpc.model.Module;
import io.nuls.rpc.model.Rpc;
import io.nuls.tools.core.ioc.ScanUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
        this.addr = IpPortInfo.getIpAddLocally();
        this.port = IpPortInfo.randomPort();

        init();
    }

    BaseRpcServer(int port) {
        this.addr = IpPortInfo.getIpAddLocally();
        this.port = port;

        init();
    }

    private void init() {
        RpcInfo.local = new Module("", "", "", 0, new ArrayList<>(), new ArrayList<>());
    }

    String getBaseUri() {
        return "http://" + addr + ":" + port + "/";
    }

    public void init(String moduleName, List<String> depends) {
        RpcInfo.local.setName(moduleName);
        RpcInfo.local.setDependsModule(depends);
        RpcInfo.local.setAddr(getAddr());
        RpcInfo.local.setPort(getPort());
    }

    public void setStatus(String status) {
        RpcInfo.local.setStatus(status);
    }

    public String getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }

    public void scanPackage(String packageName) throws Exception {
        List<Class> classList = ScanUtil.scan(packageName);
        for (Class clz : classList) {
            Method[] methods = clz.getMethods();
            for (Method method : methods) {
                Rpc rpc = annotation2Rpc(method);
                if (rpc != null) {
                    RpcInfo.registerRpc(rpc);
                }
            }
        }
    }

    private Rpc annotation2Rpc(Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (CmdInfo.class.getName().equals(annotation.annotationType().getName())) {
                CmdInfo cmdInfo = (CmdInfo) annotation;
                return new Rpc(cmdInfo.cmd(), cmdInfo.version(), method.getDeclaringClass().getName(), method.getName(), cmdInfo.preCompatible());
            }
        }
        return null;
    }

    /**
     * server 启动入口，不同server不同实现
     * 也允许自定义server
     */
    public abstract void start();
}
