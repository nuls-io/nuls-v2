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
import io.nuls.rpc.model.CmdInfo;
import io.nuls.rpc.model.Module;
import io.nuls.rpc.model.Rpc;
import io.nuls.rpc.model.RpcCmd;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.parse.JSONUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/31
 * @description
 */
public class CallCmd {



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
        for (Module module : RuntimeParam.remoteModuleMap.values()) {
            for (Rpc rpc : module.getRpcList()) {
                if (rpc.getCmd().equals(rpcCmd.getCmd())) {
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
    public static Rpc getLocalInvokeRpc(String cmd, double minVersion) {

        RuntimeParam.local.getRpcList().sort(Comparator.comparingDouble(Rpc::getVersion));

        Rpc findRpc = null;
        for (Rpc rpc : RuntimeParam.local.getRpcList()) {
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
        if (packageName == null || packageName.length() == 0) {
            return;
        }
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
            RuntimeParam.local.getRpcList().add(registerRpc);
        }
    }

    private static boolean isRegister(Rpc sourceRpc) {
        boolean exist = false;
        for (Rpc rpc : RuntimeParam.local.getRpcList()) {
            if (rpc.getCmd().equals(sourceRpc.getCmd()) && rpc.getVersion() == sourceRpc.getVersion()) {
                exist = true;
                break;
            }
        }

        return exist;
    }

    /**
     * send local module information to kernel
     */
    public static void syncWebsocket(String kernelUri) throws Exception {
        int id = RuntimeParam.nextSequence();
        RpcCmd rpcCmd = new RpcCmd(id, "version", 1.0, new Object[]{RuntimeParam.local});
        WsClient wsClient = RuntimeParam.getWsClient(kernelUri);

        wsClient.send(JSONUtils.obj2json(rpcCmd));
        Map remoteMap = wsClient.wsResponse(id);

        Map resultMap = (Map) remoteMap.get("result");
        RuntimeParam.local.setAvailable((Boolean) resultMap.get("available"));

        Map<String, Object> moduleMap = JSONUtils.json2map(JSONUtils.obj2json(resultMap.get("modules")));
        for (String key : moduleMap.keySet()) {
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(moduleMap.get(key)), Module.class);
            RuntimeParam.remoteModuleMap.put(key, module);
        }
    }

    public static String singleCmdAsWs(String cmd, Object[] params, double minVersion) throws Exception {
        int id = RuntimeParam.sequence.incrementAndGet();
        RpcCmd rpcCmd = new RpcCmd(id, cmd, minVersion, params);

        List<String> remoteUriList = CallCmd.getRemoteUri(rpcCmd);
        if (remoteUriList.size() == 0) {
            return "No cmd found->" + cmd + "." + minVersion;
        }
        if (remoteUriList.size() > 1) {
            return "Multiply cmd found->" + cmd;
        }

        String remoteUri = remoteUriList.get(0);
        WsClient wsClient = RuntimeParam.getWsClient(remoteUri);
        wsClient.send(JSONUtils.obj2json(rpcCmd));
        Map remoteMap = wsClient.wsResponse(id);
        return JSONUtils.obj2json(remoteMap);
    }
}
