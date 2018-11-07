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

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.model.*;
import io.nuls.rpc.model.Module;
import io.nuls.tools.core.ioc.ScanUtil;
import org.java_websocket.WebSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public static Module local;

    /**
     * local Config item information
     */
    public static List<ConfigItem> configItemList = Collections.synchronizedList(new ArrayList<>());

    /**
     * remote module information
     * key: module name/code
     * value: module(io.nuls.rpc.Module)
     */
    public static ConcurrentMap<String, Module> remoteModuleMap = new ConcurrentHashMap<>();

    /**
     * cmd sequence
     */
    public static AtomicInteger sequence = new AtomicInteger(0);


    /**
     * all the call cmd from other module
     */
    public static final List<Object[]> requestQueue = Collections.synchronizedList(new ArrayList<>());

    public static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    /**
     * the response from other module
     */
    public static final List<Map> responseQueue = Collections.synchronizedList(new ArrayList<>());

    /**
     * WebSocket clients
     * The client and the module correspond one by one
     * key: uri(ws://127.0.0.1:8887)
     * value: WsClient
     */
    private static ConcurrentMap<String, WsClient> wsClientMap = new ConcurrentHashMap<>();

    /**
     * get WsClient through uri
     */
    public static WsClient getWsClient(String uri) throws Exception {
        if (!wsClientMap.containsKey(uri)) {
            WsClient wsClient = new WsClient(uri);
            wsClient.connect();
            while (!wsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
                Thread.sleep(10);
            }
            wsClientMap.put(uri, wsClient);
        }
        return wsClientMap.get(uri);
    }

    /**
     * get the next call counter
     */
    public static int nextSequence() {
        return sequence.incrementAndGet();
    }


    /**
     * Object to bytes
     */
    public static byte[] obj2Bytes(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        return byteArrayOutputStream.toByteArray();
    }


    /**
     * get remote rpc uri based on cmd
     */
    public static List<String> getRemoteUri(CmdRequest cmdRequest) {
        List<String> remoteUriList = new ArrayList<>();
        for (Module module : RuntimeInfo.remoteModuleMap.values()) {
            for (CmdDetail cmdDetail : module.getCmdDetailList()) {
                if (cmdDetail.getCmd().equals(cmdRequest.getCmd())) {
                    remoteUriList.add("ws://" + module.getAddr() + ":" + module.getPort());
                    break;
                }
            }
        }
        return remoteUriList;
    }

    /**
     * get local Rpc based on cmd & minimum version
     */
    public static CmdDetail getLocalInvokeCmd(String cmd, double minVersion) {

        RuntimeInfo.local.getCmdDetailList().sort(Comparator.comparingDouble(CmdDetail::getVersion));

        CmdDetail find = null;
        for (CmdDetail cmdDetail : RuntimeInfo.local.getCmdDetailList()) {
            if (cmdDetail.getCmd().equals(cmd) && cmdDetail.getVersion() >= minVersion) {
                if (find == null) {
                    find = cmdDetail;
                } else if (cmdDetail.getVersion() > cmdDetail.getVersion()) {
                    if (cmdDetail.isPreCompatible()) {
                        find = cmdDetail;
                    } else {
                        break;
                    }
                }
            }
        }
        return find;
    }

    /**
     * scan package, auto register cmd
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
                if (cmdDetail != null) {
                    registerCmdDetail(cmdDetail);
                }
            }
            System.out.println("====================");
        }
    }

    /**
     * get the annotation of method, if it was instance of CmdInfo, build CmdInfo
     */
    private static CmdDetail annotation2CmdDetail(Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (CmdAnnotation.class.getName().equals(annotation.annotationType().getName())) {
                CmdAnnotation cmdAnnotation = (CmdAnnotation) annotation;
                return new CmdDetail(cmdAnnotation.cmd(), cmdAnnotation.version(), method.getDeclaringClass().getName(), method.getName(), cmdAnnotation.preCompatible());
            }
        }
        return null;
    }

    /**
     * scan & register rpc
     */
    private static void registerCmdDetail(CmdDetail cmdDetail) throws Exception {
        if (isRegister(cmdDetail)) {
            throw new Exception("Duplicate cmd found: " + cmdDetail.getCmd() + "-" + cmdDetail.getVersion());
        } else {
            RuntimeInfo.local.getCmdDetailList().add(cmdDetail);
        }
    }

    private static boolean isRegister(CmdDetail sourceCmdDetail) {
        boolean exist = false;
        for (CmdDetail cmdDetail : RuntimeInfo.local.getCmdDetailList()) {
            if (cmdDetail.getCmd().equals(sourceCmdDetail.getCmd()) && cmdDetail.getVersion() == sourceCmdDetail.getVersion()) {
                exist = true;
                break;
            }
        }

        return exist;
    }
}
