package io.nuls.tools.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志管理工具类
 * Log Management Tool Class
 *
 * @author tag
 * 2018/12/17
 * */
public class LoggerBuilder {

    private static final Map<String, NulsLogger> container = new HashMap<>();
    private static final Level DEFAULT_LEVEL = Level.ALL;
    private static final boolean DEFAULT_CONSOLE_APPEND = true;

    public static NulsLogger getLogger(String folderName,String fileName) {
        String realKey = folderName+"/"+fileName;
        return getLogger(realKey);
    }

    public static NulsLogger getLogger(String folderName,String fileName,Level level) {
        String realKey = folderName+"/"+fileName;
        return getLogger(realKey,level,DEFAULT_CONSOLE_APPEND);
    }

    public static NulsLogger getLogger(String folderName,String fileName,Level level,boolean consoleAppend) {
        String realKey = folderName+"/"+fileName;
        return getLogger(realKey,level,consoleAppend);
    }

    public static NulsLogger getLogger(String fileName) {
        return getLogger(fileName,DEFAULT_LEVEL,DEFAULT_CONSOLE_APPEND);
    }

    public static NulsLogger getLogger(String fileName,Level level) {
        return getLogger(fileName,level,DEFAULT_CONSOLE_APPEND);
    }

    public static NulsLogger getLogger(String fileName,Level level,boolean consoleAppend) {
        NulsLogger logger = container.get(fileName);
        if(logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = build(fileName,level,consoleAppend);
            container.put(fileName,logger);
        }
        return logger;
    }

    private static NulsLogger build(String fileName, Level level,boolean consoleAppend) {
        RollingFileAppender fileAppender = LogAppender.getAppender(fileName,level);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(fileAppender.getEncoder().toString());
        //设置不向上级打印信息
        logger.setAdditive(false);
        logger.addAppender(fileAppender);
        if(consoleAppend){
            Appender consoleAppender = LogAppender.createConsoleAppender();
            logger.addAppender(consoleAppender);
        }
        return new NulsLogger(logger);
    }
}
