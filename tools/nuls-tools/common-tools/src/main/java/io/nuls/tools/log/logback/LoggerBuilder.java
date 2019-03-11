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
 */
public class LoggerBuilder {

    private static final Map<String, NulsLogger> container = new HashMap<>();
    private static final Level DEFAULT_LEVEL = Level.ALL;

    static {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("io.netty");
        logger.setAdditive(false);
        logger.setLevel(Level.INFO);

        Logger logger1 = context.getLogger("org.mongodb.driver.protocol.command");
        logger1.setAdditive(false);
        logger1.setLevel(Level.INFO);

    }

    public static NulsLogger getLogger(String folderName, String fileName) {
        String realKey = folderName + "/" + fileName;
        return getLogger(realKey);
    }


    public static NulsLogger getLogger(String folderName, String fileName, Level level) {
        String realKey = folderName + "/" + fileName;
        return getLogger(realKey, level, level);
    }

    public static NulsLogger getLogger(String folderName, String fileName, Level fileLevel, Level consoleLevel) {
        String realKey = folderName + "/" + fileName;
        return getLogger(realKey, fileLevel, consoleLevel);
    }

    public static NulsLogger getLogger(String fileName) {
        return getLogger(fileName, DEFAULT_LEVEL, DEFAULT_LEVEL);
    }

    public static NulsLogger getLogger(String fileName, Level level) {
        return getLogger(fileName, level, level);
    }

    public static NulsLogger getLogger(String fileName, Level fileLevel, Level consoleLevel) {
        NulsLogger logger = container.get(fileName);
        if (logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = build(fileName, fileLevel, consoleLevel);
            container.put(fileName, logger);
        }
        return logger;
    }

    @SuppressWarnings("unchecked")
    private static NulsLogger build(String fileName, Level fileLevel, Level consoleLevel) {
        RollingFileAppender fileAppender = LogAppender.getAppender(fileName, fileLevel);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(fileAppender.getEncoder().toString());
        //设置不向上级打印信息
        logger.setAdditive(false);
        logger.addAppender(fileAppender);
        //输出到控制台
        Appender consoleAppender = LogAppender.createConsoleAppender(consoleLevel);
        logger.addAppender(consoleAppender);

        return new NulsLogger(logger);
    }
}
