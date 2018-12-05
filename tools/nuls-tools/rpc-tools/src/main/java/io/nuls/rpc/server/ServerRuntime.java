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

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.*;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.data.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 服务端运行时所需要的变量和方法
 * Variables and static methods required for server-side runtime
 *
 * @author tangyi
 * @date 2018/11/23
 */
public class ServerRuntime {

    /**
     * 本模块所有对外提供的接口的详细信息
     * local module(io.nuls.rpc.RegisterApi) information
     */
    public static RegisterApi local = new RegisterApi();


    /**
     * 接口最近调用时间
     * Recent call time of interface
     */
    static Map<String, Long> cmdInvokeTime = new HashMap<>();

    /**
     * 接口返回值改变次数
     * Number of return value changes
     */
    private static Map<String, Long> cmdChangeCount = new HashMap<>();
    static Map<String, Object[]> cmdLastResponse = new HashMap<>();

    /**
     * 本模块配置信息
     * Configuration information of this module
     */
    public static Map<String, ConfigItem> configItemMap = new ConcurrentHashMap<>();


    /**
     * 单次响应队列，数组的第一个元素是Websocket对象，数组的第二个元素是Message
     * Single called queue. The first element of the array is the websocket object, and the second element of the array is Message.
     */
    static final Queue<Object[]> REQUEST_SINGLE_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 多次响应队列（根据Period），数组的第一个元素是Websocket对象，数组的第二个元素是Message
     * Multiply called queue (Period). The first element of the array is the websocket object, and the second element of the array is Message.
     */
    static final Queue<Object[]> REQUEST_PERIOD_LOOP_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 多次响应队列（根据Event count），数组的第一个元素是Websocket对象，数组的第二个元素是Message
     * Multiply called queue (Event count). The first element of the array is the websocket object, and the second element of the array is Message.
     */
    static final Queue<Object[]> REQUEST_EVENT_COUNT_LOOP_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 取消订阅列表
     * Unsubscribe list
     */
    static final List<String> UNSUBSCRIBE_LIST = Collections.synchronizedList(new ArrayList<>());

    /**
     * Return the first item of REQUEST_SINGLE_QUEUE
     *
     * @return Object[]
     */
    static Object[] firstObjArrInRequestSingleQueue() {
        return firstObjArrInQueue(REQUEST_SINGLE_QUEUE);
    }

    /**
     * Return the first item of REQUEST_PERIOD_LOOP_QUEUE
     *
     * @return Object[]
     */
    static Object[] firstObjArrInRequestPeriodLoopQueue() {
        return firstObjArrInQueue(REQUEST_PERIOD_LOOP_QUEUE);
    }

    /**
     * Return the first item of REQUEST_EVENT_COUNT_LOOP_QUEUE
     *
     * @return Object[]
     */
    static Object[] firstObjArrInRequestEventCountLoopQueue() {
        return firstObjArrInQueue(REQUEST_EVENT_COUNT_LOOP_QUEUE);
    }

    /**
     * 获取队列中的第一个元素，然后移除队列
     * Get the first item and remove
     *
     * @return 队列的第一个元素. The first item in SERVER_RESPONSE_QUEUE.
     */
    private static synchronized Object[] firstObjArrInQueue(Queue<Object[]> objectsQueue) {
        Object[] objects = objectsQueue.peek();
        objectsQueue.poll();
        return objects;
    }


    /**
     * 根据cmd命令和版本号获取本地方法
     * Getting local methods from CMD commands and version
     *
     * @param cmd        Command of remote method
     * @param minVersion Version of remote method
     * @return CmdDetail
     */
    static CmdDetail getLocalInvokeCmd(String cmd, double minVersion) {

        /*
        根据version排序
        Sort according to version
         */
        local.getApiMethods().sort(Comparator.comparingDouble(CmdDetail::getVersion));

        CmdDetail find = null;
        for (CmdDetail cmdDetail : local.getApiMethods()) {
            /*
            cmd不一致，跳过
            CMD inconsistency, skip
             */
            if (!cmdDetail.getMethodName().equals(cmd)) {
                continue;
            }

            /*
            大版本不一样，跳过
            Big version is different, skip
             */
            if ((int) minVersion != (int) cmdDetail.getVersion()) {
                continue;
            }

            /*
            没有备选方法，则设置当前方法为备选方法
            If there is no alternative method, set the current method as the alternative method
             */
            if (find == null) {
                find = cmdDetail;
                continue;
            }

            /*
            如果当前方法版本更高，则设置当前方法为备选方法
            If the current method version is higher, set the current method as an alternative method
             */
            if (cmdDetail.getVersion() > find.getVersion()) {
                find = cmdDetail;
            }
        }

        return find;
    }


    /**
     * 根据cmd命令获取最高版本的方法，逻辑同上
     * Getting the highest version of local methods from CMD commands
     * @param cmd Command of remote method
     * @return CmdDetail
     */
    static CmdDetail getLocalInvokeCmd(String cmd) {

        local.getApiMethods().sort(Comparator.comparingDouble(CmdDetail::getVersion));

        CmdDetail find = null;
        for (CmdDetail cmdDetail : local.getApiMethods()) {
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
     * 扫描指定路径，得到所有接口的详细信息
     * Scan the specified path for details of all interfaces
     * @param packageName Package full path
     * @throws Exception Duplicate commands found
     */
    static void scanPackage(String packageName) throws Exception {
        /*
        路径为空，跳过
        The path is empty, skip
         */
        if (StringUtils.isNull(packageName)) {
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

                /*
                重复接口只注册一次
                Repeated interfaces are registered only once
                 */
                if (!isRegister(cmdDetail)) {
                    local.getApiMethods().add(cmdDetail);
                } else {
                    throw new Exception(Constants.CMD_DUPLICATE + ":" + cmdDetail.getMethodName() + "-" + cmdDetail.getVersion());
                }
            }
        }
    }


    /**
     * 保存所有拥有CmdAnnotation注解的方法
     * Save all methods that have CmdAnnotation annotations
     * @param method Method
     * @return CmdDetail
     */
    private static CmdDetail annotation2CmdDetail(Method method) {
        CmdDetail cmdDetail = null;
        List<CmdParameter> cmdParameters = new ArrayList<>();
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            /*
            CmdAnnotation中包含了接口的必要信息
            The CmdAnnotation contains the necessary information for the interface
             */
            if (CmdAnnotation.class.getName().equals(annotation.annotationType().getName())) {
                CmdAnnotation cmdAnnotation = (CmdAnnotation) annotation;
                cmdDetail = new CmdDetail();
                cmdDetail.setMethodName(cmdAnnotation.cmd());
                cmdDetail.setMethodDescription(cmdAnnotation.description());
                cmdDetail.setMethodMinEvent(cmdAnnotation.minEvent() + "");
                cmdDetail.setMethodMinPeriod(cmdAnnotation.minPeriod() + "");
                cmdDetail.setMethodScope(cmdAnnotation.scope());
                cmdDetail.setVersion(cmdAnnotation.version());
                cmdDetail.setInvokeClass(method.getDeclaringClass().getName());
                cmdDetail.setInvokeMethod(method.getName());
            }

            /*
            参数详细说明
            Detailed description of parameters
             */
            if (Parameter.class.getName().equals(annotation.annotationType().getName())) {
                Parameter parameter = (Parameter) annotation;
                CmdParameter cmdParameter = new CmdParameter(parameter.parameterName(), parameter.parameterType(), parameter.parameterValidRange(), parameter.parameterValidRegExp());
                cmdParameters.add(cmdParameter);
            }
        }
        if (cmdDetail == null) {
            return null;
        }
        cmdDetail.setParameters(cmdParameters);

        return cmdDetail;
    }


    /**
     * 判断是否已经注册过，判断方法为：cmd+version唯一
     * Determine if the cmd has been registered
     * 1. The same cmd
     * 2. The same version
     * @param sourceCmdDetail CmdDetail
     * @return boolean
     */
    private static boolean isRegister(CmdDetail sourceCmdDetail) {
        boolean exist = false;
        for (CmdDetail cmdDetail : local.getApiMethods()) {
            if (cmdDetail.getMethodName().equals(sourceCmdDetail.getMethodName()) && cmdDetail.getVersion() == sourceCmdDetail.getVersion()) {
                exist = true;
                break;
            }
        }

        return exist;
    }

    /**
     * Set event count
     * @param cmd Command of remote method
     * @param value Response
     */
    public static void eventCount(String cmd, Response value) {
        addCmdChangeCount(cmd);
        setCmdLastValue(cmd, value);
    }

    /**
     * 返回值改变次数增加1
     * Increase the changes number of return value by 1
     * @param cmd Command of remote method
     */
    private static void addCmdChangeCount(String cmd) {
        if (!cmdChangeCount.containsKey(cmd)) {
            cmdChangeCount.put(cmd, 1L);
        } else {
            long count = cmdChangeCount.get(cmd);
            cmdChangeCount.put(cmd, count + 1);
        }
    }

    /**
     * 得到返回值的改变数量
     * Get current changes number of return value
     * @param cmd Command of remote method
     * @return long
     */
    static long getCmdChangeCount(String cmd) {
        try {
            return cmdChangeCount.get(cmd);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 设置最近改变的值
     * Set the recently changed value
     * @param cmd Command of remote method
     * @param value Response
     */
    private static void setCmdLastValue(String cmd, Response value) {
        cmdLastResponse.put(cmd, new Object[]{value, false});
    }

    /**
     *
     * @param cmd
     * @return
     */
    static Object[] getCmdLastValue(String cmd) {
        return cmdLastResponse.get(cmd);
    }
}
