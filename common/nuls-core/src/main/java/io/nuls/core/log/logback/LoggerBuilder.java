package io.nuls.core.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import io.nuls.core.model.StringUtils;
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

    private static final Map<String, NulsLogger> CONTAINER = new HashMap<>();
    private static final Level DEFAULT_LEVEL = Level.ALL;

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

    public static NulsLogger getLogger(String fileName) {
        Level level = StringUtils.isNotBlank(System.getProperty("log.level")) ? Level.toLevel(System.getProperty("log.level")) : DEFAULT_LEVEL;
        return getLogger(fileName, level, level);
    }

    public static NulsLogger getLogger(String fileName,int chainId) {
        Level level = StringUtils.isNotBlank(System.getProperty("log.level")) ? Level.toLevel(System.getProperty("log.level")) : DEFAULT_LEVEL;
        return getLogger("chain_"+chainId+ "_" +fileName, level, level);
    }

    private static NulsLogger getLogger(String fileName, Level fileLevel, Level consoleLevel) {
        NulsLogger logger = CONTAINER.get(fileName);
        if (logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = build(fileName, fileLevel, consoleLevel);
            CONTAINER.put(fileName, logger);
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
