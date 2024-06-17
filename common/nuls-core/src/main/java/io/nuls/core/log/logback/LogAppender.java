package io.nuls.core.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.OptionHelper;
import io.nuls.core.model.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Log printing management class, log file creation, log file size, save time, log output format, and other settings management
 * Log Printing Management Class, Log File Creation, Log File Size, Save Time, Log Output Format and other settings management
 *
 * @author tag
 * 2018/12/17
 * */
public class LogAppender {

    public static String PROJECT_PATH = StringUtils.isNotBlank(System.getProperty("log.path")) ? System.getProperty("log.path") : (System.getProperty("user.dir") + File.separator + "logs");

    /**
     * Dynamically set by passing in the name and levelappender
     *
     * @param fileName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static RollingFileAppender getAppender(String fileName, Level level){
        String rootPath = PROJECT_PATH;
        if(!rootPath.endsWith(File.separator)){
            rootPath += File.separator;
        }
        if(fileName.startsWith(File.separator)){
            fileName = fileName.substring(1);
        }
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        RollingFileAppender appender = new RollingFileAppender();
        /*Set context for eachloggerAll associated withloggerContext, default context name isdefault.
        But it can be used<contextName>Set to a different name to distinguish records from different applications. Once set, it cannot be modified.*/
        appender.setContext(context);

        //Set level filters here
        LogFilter levelController = new LogFilter();
        ThresholdFilter levelFilter = levelController.getThresholdFilter(level);
        levelFilter.start();
        appender.addFilter(levelFilter);

        //Set file name
        appender.setFile(OptionHelper.substVars(rootPath+fileName + ".log",context));
        appender.setAppend(true);
        appender.setPrudent(false);
        //Class for setting file creation time and size
        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
        //File Name Format
        String fp = OptionHelper.substVars(rootPath+ fileName + ".%d{yyyy-MM-dd}.%i.zip",context);
        //Maximum log file size
        policy.setMaxFileSize(FileSize.valueOf("100MB"));
        //Set file name mode
        policy.setFileNamePattern(fp);
        //Set Save Recent3Days of logs
        policy.setMaxHistory(3);
        //Total size limit
        policy.setContext(context);
        policy.setTotalSizeCap(FileSize.valueOf("1GB"));
        //Set parent node asappender
        policy.setParent(appender);
        //Set context for eachloggerAll associated withloggerContext, default context name isdefault.
        //But it can be used<contextName>Set to a different name to distinguish records from different applications. Once set, it cannot be modified.
        policy.start();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        //Set context for eachloggerAll associated withloggerContext, default context name isdefault.
        //But it can be used<contextName>Set to a different name to distinguish records from different applications. Once set, it cannot be modified.
        encoder.setContext(context);
        //Format
        /*encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [%t] %replace(%caller{1}){'\\t|Caller.{1}0|\\r\\n', ''} - %msg%n");*/
        encoder.setPattern("%d %p [%t] - %msg%n");
        encoder.start();
        //Join the following two nodes
        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }

    @SuppressWarnings("unchecked")
    public static Appender<ILoggingEvent> createConsoleAppender(Level level){
        ConsoleAppender appender = new ConsoleAppender();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        appender.setContext(context);
        //Set level filters here
        LogFilter levelController = new LogFilter();
        ThresholdFilter levelFilter = levelController.getThresholdFilter(level);
        levelFilter.start();
        appender.addFilter(levelFilter);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        //Set context for eachloggerAll associated withloggerContext, default context name isdefault.
        //But it can be used<contextName>Set to a different name to distinguish records from different applications. Once set, it cannot be modified.
        encoder.setContext(context);
        //Format
        encoder.setPattern("%d %p [%t] - %msg%n");
        encoder.start();
        //Join the following two nodes
        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }
}
