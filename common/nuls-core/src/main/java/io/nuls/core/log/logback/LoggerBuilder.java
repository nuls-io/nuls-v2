package io.nuls.core.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import io.nuls.core.model.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
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
    private static final Level DEFAULT_LEVEL = Level.INFO;

    static {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("io.netty");
        logger.setAdditive(false);
        logger.setLevel(Level.ERROR);

        Logger mongodbLogger = context.getLogger("org.mongodb.driver.protocol.command");
        mongodbLogger.setAdditive(false);
        mongodbLogger.setLevel(Level.ERROR);

        Logger mongodbLogger2 = context.getLogger("org.mongodb.driver.cluster");
        mongodbLogger2.setAdditive(false);
        mongodbLogger2.setLevel(Level.ERROR);
    }

    public static NulsLogger getLogger(String folderName, String fileName) {
        String realKey = folderName + "/" + fileName;
        return getLogger(realKey);
    }


    private static NulsLogger getLogger(String folderName, String fileName, Level level) {
        String realKey = folderName + "/" + fileName;
        return getLogger(realKey, level, level);
    }

    public static NulsLogger getLogger(String folderName, String fileName, Level fileLevel, Level consoleLevel) {
        String realKey = folderName + "/" + fileName;
        return getLogger(realKey, fileLevel, consoleLevel);
    }

    public static NulsLogger getLogger(String folderName, String fileName, List<String> packageNames, Level fileLevel, Level consoleLevel) {
        String realKey = folderName + "/" + fileName;
        NulsLogger logger = container.get(realKey);
        if (logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = build(realKey, packageNames, fileLevel, consoleLevel);
            container.put(realKey, logger);
        }
        return logger;
    }

    public static NulsLogger getLogger(String fileName) {
        Level level = StringUtils.isNotBlank(System.getProperty("log.level")) ? Level.toLevel(System.getProperty("log.level")) : DEFAULT_LEVEL;
        return getLogger(fileName, level, level);
    }

    public static NulsLogger getLogger(String fileName,int chainId) {
        Level level = StringUtils.isNotBlank(System.getProperty("log.level")) ? Level.toLevel(System.getProperty("log.level")) : DEFAULT_LEVEL;
        return getLogger("chain_"+chainId+ "_" +fileName, level, level);
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

    private static NulsLogger build(String fileName, List<String> packageNames, Level fileLevel, Level consoleLevel) {
        RollingFileAppender fileAppender = LogAppender.getAppender(fileName, fileLevel);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(fileAppender.getEncoder().toString());
        //设置不向上级打印信息
        logger.setAdditive(false);
        logger.addAppender(fileAppender);

        for (String name : packageNames) {
            Logger log = context.getLogger(name);
            logger.setAdditive(false);
            log.addAppender(fileAppender);
        }
        //输出到控制台
        Appender consoleAppender = LogAppender.createConsoleAppender(consoleLevel);
        logger.addAppender(consoleAppender);
        return new NulsLogger(logger);
    }
}
