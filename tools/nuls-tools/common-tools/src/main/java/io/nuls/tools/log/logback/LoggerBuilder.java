package io.nuls.tools.log.logback;

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

    public static NulsLogger getLogger(String folderName,String fileName) {
        String realKey = folderName+"/"+fileName;
        NulsLogger logger = container.get(realKey);
        if(logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = container.get(realKey);
            if(logger != null) {
                return logger;
            }
            logger = build(realKey);
            container.put(realKey,logger);
        }
        return logger;
    }

    public static NulsLogger getLogger(String fileName) {
        NulsLogger logger = container.get(fileName);
        if(logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = container.get(fileName);
            if(logger != null) {
                return logger;
            }
            logger = build(fileName);
            container.put(fileName,logger);
        }
        return logger;
    }


    private static NulsLogger build(String fileName) {
        RollingFileAppender fileAppender = LogAppender.getAppender(fileName);
        Appender consoleAppender = LogAppender.createConsoleAppender();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(fileAppender.getEncoder().toString());
        //设置不向上级打印信息
        logger.setAdditive(false);
        logger.addAppender(fileAppender);
        logger.addAppender(consoleAppender);
        return new NulsLogger(logger);
    }
}
