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
    private static final String BASIC_NAME = "nuls";
    private static final Map<String, Logger> container = new HashMap<>();

    public static Logger getLogger(String name) {
        Logger logger = container.get(name);
        if(logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = container.get(name);
            if(logger != null) {
                return logger;
            }
            logger = build(name);
            container.put(name,logger);
        }
        return logger;
    }

    public static Logger getBasicLoggger(){
        return getLogger(BASIC_NAME);
    }

    private static Logger build(String name) {
        RollingFileAppender fileAppender = LogAppender.getAppender(name);
        Appender consoleAppender = LogAppender.createConsoleAppender();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(fileAppender.getEncoder().toString());
        //设置不向上级打印信息
        logger.setAdditive(false);
        logger.addAppender(fileAppender);
        logger.addAppender(consoleAppender);
        return logger;
    }
}
