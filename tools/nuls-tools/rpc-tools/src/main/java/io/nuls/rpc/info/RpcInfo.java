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

import io.nuls.rpc.model.*;
import io.nuls.rpc.model.Module;
import io.nuls.tools.core.ioc.ScanUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RpcInfo {

    /**
     * local module(io.nuls.rpc.Module) information
     */
    public static Module local;

    /**
     * local Config item information
     */
    public static List<ConfigItem> configItemList = Collections.synchronizedList(new ArrayList<>());

    /**
     * remote module(io.nuls.rpc.Module) information
     */
    public static ConcurrentMap<String, Module> remoteModuleMap = new ConcurrentHashMap<>();



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
    public static List<String> getRemoteUri(RpcCmd rpcCmd) {
        List<String> remoteUriList = new ArrayList<>();
        for (Module module : remoteModuleMap.values()) {
            for (Rpc rpc : module.getRpcList()) {
                if (rpc.getCmd().equals(rpcCmd.getCmd())) {
                    remoteUriList.add("http://" + module.getAddr() + ":" + module.getPort());
                    break;
                }
            }
        }
        return remoteUriList;
    }

    /**
     * get local Rpc based on cmd & minimum version
     */
    public static Rpc getLocalInvokeRpc(String cmd, double minVersion) {

        local.getRpcList().sort(Comparator.comparingDouble(Rpc::getVersion));

        Rpc findRpc = null;
        for (Rpc rpc : local.getRpcList()) {
            if (rpc.getCmd().equals(cmd) && rpc.getVersion() >= minVersion) {
                if (findRpc == null) {
                    findRpc = rpc;
                } else if (rpc.getVersion() > findRpc.getVersion()) {
                    if (rpc.isPreCompatible()) {
                        findRpc = rpc;
                    } else {
                        break;
                    }
                }
            }
        }
        return findRpc;
    }

    /**
     * scan package, auto register cmd
     */
    public static void scanPackage(String packageName) throws Exception {
        List<Class> classList = ScanUtil.scan(packageName);
        for (Class clz : classList) {
            Method[] methods = clz.getMethods();
            for (Method method : methods) {
                Rpc rpc = annotation2Rpc(method);
                if (rpc != null) {
                    registerRpc(rpc);
                }
            }
            System.out.println("====================");
        }
    }

    /**
     * get the annotation of method, if it was instance of CmdInfo, build CmdInfo
     */
    private static Rpc annotation2Rpc(Method method) {
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
     * scan & register rpc
     */
    private static void registerRpc(Rpc registerRpc) throws Exception {
        if (isRegister(registerRpc)) {
            throw new Exception("Duplicate cmd found: " + registerRpc.getCmd() + "-" + registerRpc.getVersion());
        } else {
            local.getRpcList().add(registerRpc);
        }
    }

    private static boolean isRegister(Rpc sourceRpc) {
        boolean exist = false;
        for (Rpc rpc : local.getRpcList()) {
            if (rpc.getCmd().equals(sourceRpc.getCmd()) && rpc.getVersion() == sourceRpc.getVersion()) {
                exist = true;
                break;
            }
        }

        return exist;
    }

}
