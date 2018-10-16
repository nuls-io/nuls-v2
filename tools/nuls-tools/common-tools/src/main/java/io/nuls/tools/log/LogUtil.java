package io.nuls.tools.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tag
 */
public class LogUtil {
    private static Map<String, Logger> loggerMap = new HashMap<>();

    /**
     * 根据名称生成一个日志Logger类
     *
     * @param name 日志类对应在名称
     * @return 日志类
     */
    public static Logger getLogger(String name) {
        if (loggerMap.keySet().contains(name)) {
            return loggerMap.get(name);
        }
        Logger logger = LoggerFactory.getLogger(name);
        loggerMap.put(name, logger);
        return logger;
    }

    /**
     * 获取详细的日志信息
     *
     * @param msg 日志信息
     * @return 完整的日志信息
     */
    public String getRealMessage(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        return logContent;
    }

    /**
     * 获取日志记录点的全路径
     *
     * @return 日志记录点的全路径
     */
    private static String getLogTrace() {
        StringBuilder logTrace = new StringBuilder();
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        if (stack.length > 1) {
            // index为3上一级调用的堆栈信息，index为1和2都为Log类自己调两次（可忽略），index为0为主线程触发（可忽略）
            StackTraceElement ste = stack[3];
            if (ste != null) {
                // 获取类名、方法名、日志的代码行数
                logTrace.append(ste.getClassName());
                logTrace.append('.');
                logTrace.append(ste.getMethodName());
                logTrace.append('(');
                logTrace.append(ste.getFileName());
                logTrace.append(':');
                logTrace.append(ste.getLineNumber());
                logTrace.append(')');
            }
        }
        return logTrace.toString();
    }
}
