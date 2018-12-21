package io.nuls.tools.log.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.OptionHelper;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * 日志打印管理类，日志文件创建，日志文件大小，保存时间，日志输出格式等设置管理
 * Log Printing Management Class, Log File Creation, Log File Size, Save Time, Log Output Format and other settings management
 *
 * @author tag
 * 2018/12/17
 * */
public class LogAppender {
    private final static String PROJECT_PATH = "user.dir";
    /**
     * 通过传入的名字和级别，动态设置appender
     *
     * @param fileName
     * @return
     */
    public static RollingFileAppender getAppender(String fileName){
        String rootPath = System.getProperty(PROJECT_PATH);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        RollingFileAppender appender = new RollingFileAppender();

        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        //但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        appender.setContext(context);
        //设置文件名
        appender.setFile(OptionHelper.substVars(rootPath+"/logs/"+"/"+fileName + ".log",context));
        appender.setAppend(true);
        appender.setPrudent(false);

        //设置文件创建时间及大小的类
        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
        //文件名格式
        String fp = OptionHelper.substVars(rootPath+"/logs/"+"/"+ fileName + ".%d{yyyy-MM-dd}.%i.zip",context);
        //最大日志文件大小
        policy.setMaxFileSize("100MB");
        //设置文件名模式
        policy.setFileNamePattern(fp);
        //设置最大历史记录为15条
        policy.setMaxHistory(7);
        //总大小限制
        policy.setTotalSizeCap(FileSize.valueOf("2GB"));
        //设置父节点是appender
        policy.setParent(appender);
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        //但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        policy.setContext(context);
        policy.start();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        //但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        encoder.setContext(context);
        //设置格式
        /*encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [%t] %replace(%caller{1}){'\\t|Caller.{1}0|\\r\\n', ''} - %msg%n");*/
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS}  [%thread] %-5level - %msg%n");
        encoder.start();
        //加入下面两个节点
        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }

    public static Appender<ILoggingEvent> createConsoleAppender(){
        ConsoleAppender appender = new ConsoleAppender();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        appender.setContext(context);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        //但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        encoder.setContext(context);
        //设置格式
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS}  [%thread] %-5level - %msg%n");
        encoder.start();
        //加入下面两个节点
        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }
}
