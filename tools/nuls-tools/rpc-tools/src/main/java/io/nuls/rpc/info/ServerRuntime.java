package io.nuls.rpc.info;

import io.nuls.rpc.model.*;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author tangyi
 * @date 2018/11/23
 * @description
 */
public class ServerRuntime {

    /**
     * local module(io.nuls.rpc.ModuleInfo) information
     */
    public static ModuleInfo local = new ModuleInfo();

    public static Map<String, Long> cmdInvokeTime = new HashMap<>();
    public static Map<String, Integer> cmdInvokeHeight = new HashMap<>();

    /**
     * local Config item information
     */
    public static Map<String, ConfigItem> configItemMap = new ConcurrentHashMap<>();



    /**
     * The pending request command received through RPC
     * Array [0] is the Websocket object for communication
     * Array [1] is the content of the communication
     */
    public static final List<Object[]> REQUEST_QUEUE = Collections.synchronizedList(new ArrayList<>());

    /**
     * The thread pool object that handles the request
     */
    public static ExecutorService fixedThreadPool = ThreadUtils.createThreadPool(5, 500, new NulsThreadFactory("handleRequest"));

    /**
     * Get local command
     * Sort by version number
     * 1. Not less than the incoming version number
     * 2. Forward compatible
     * 3. The highest version that meet conditions 1 and 2 at the same time
     */
    public static CmdDetail getLocalInvokeCmd(String cmd, double minVersion) {

        local.getRegisterApi().getApiMethods().sort(Comparator.comparingDouble(CmdDetail::getVersion));

        CmdDetail find = null;
        for (CmdDetail cmdDetail : local.getRegisterApi().getApiMethods()) {
            if (!cmdDetail.getMethodName().equals(cmd)) {
                continue;
            }
            if ((int) minVersion != (int) cmdDetail.getVersion()) {
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
     * Get local command
     * Sort by version number
     * The highest version
     */
    public static CmdDetail getLocalInvokeCmd(String cmd) {

        local.getRegisterApi().getApiMethods().sort(Comparator.comparingDouble(CmdDetail::getVersion));

        CmdDetail find = null;
        for (CmdDetail cmdDetail : local.getRegisterApi().getApiMethods()) {
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
     * Scan the provided package
     * Analysis annotation, register cmd
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
                if (cmdDetail == null) {
                    continue;
                }

                if (!isRegister(cmdDetail)) {
                    local.getRegisterApi().getApiMethods().add(cmdDetail);
                } else {
                    throw new Exception(Constants.CMD_DUPLICATE + ":" + cmdDetail.getMethodName() + "-" + cmdDetail.getVersion());
                }
            }
        }
    }

    /**
     * Get annotation of methods
     * If the annotation is CmdAnnotation, it means that the cmd needs to be registered
     */
    private static CmdDetail annotation2CmdDetail(Method method) {
        CmdDetail cmdDetail = null;
        List<CmdParameter> cmdParameters = new ArrayList<>();
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (CmdAnnotation.class.getName().equals(annotation.annotationType().getName())) {
                CmdAnnotation cmdAnnotation = (CmdAnnotation) annotation;
                cmdDetail = new CmdDetail();
                cmdDetail.setMethodName(cmdAnnotation.cmd());
                cmdDetail.setMethodDescription(cmdAnnotation.description());
                cmdDetail.setMethodMinEvent(cmdAnnotation.minEvent()+"");
                cmdDetail.setMethodMinPeriod(cmdAnnotation.minPeriod()+"");
                cmdDetail.setMethodScope(cmdAnnotation.scope());
                cmdDetail.setVersion(cmdAnnotation.version());
                cmdDetail.setInvokeClass(method.getDeclaringClass().getName());
                cmdDetail.setInvokeMethod(method.getName());
            }
            if (Parameters.class.getName().equals(annotation.annotationType().getName())) {
                Parameters parameters = (Parameters) annotation;
                for (Parameter parameter : parameters.value()) {
                    CmdParameter cmdParameter = new CmdParameter(parameter.parameterName(), parameter.parameterType(), parameter.parameterValidRange(), parameter.parameterValidRegExp());
                    cmdParameters.add(cmdParameter);
                }
            }
        }
        if (cmdDetail == null) {
            return null;
        }
        cmdDetail.setParameters(cmdParameters);

        return cmdDetail;
    }

    /**
     * Determine if the cmd has been registered
     * 1. The same cmd
     * 2. The same version
     */
    private static boolean isRegister(CmdDetail sourceCmdDetail) {
        boolean exist = false;
        for (CmdDetail cmdDetail : local.getRegisterApi().getApiMethods()) {
            if (cmdDetail.getMethodName().equals(sourceCmdDetail.getMethodName()) && cmdDetail.getVersion() == sourceCmdDetail.getVersion()) {
                exist = true;
                break;
            }
        }

        return exist;
    }

    /**
     * Constructing a new Response object
     */
    public static Response newResponse(String requestId, String status, String comment) {
        Response response = new Response();
        response.setRequestId(requestId);
        response.setResponseStatus(status);
        response.setResponseComment(comment);
        response.setResponseMaxSize("0");
        return response;
    }
}
