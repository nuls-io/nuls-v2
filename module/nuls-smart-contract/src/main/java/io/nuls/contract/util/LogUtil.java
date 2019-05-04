/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import io.nuls.core.log.logback.LogAppender;
import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author: PierreLuo
 * @date: 2019-04-24
 */
public class LogUtil {
    private LogUtil() {
    }

    public static void configDefaultLog(String folderName, String fileName, Level fileLevel, Level consoleLevel, String systemLogLevel, String packageLogPackages, String packageLogLevels) {
        int rootLevelInt = Math.min(fileLevel.toInt(), consoleLevel.toInt());

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setAdditive(false);
        rootLogger.setLevel(Level.toLevel(rootLevelInt));
        RollingFileAppender fileAppender = LogAppender.getAppender(folderName + File.separator + fileName, fileLevel);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n");
        encoder.start();
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        rootLogger.addAppender(fileAppender);

        Log.DEFAULT_BASIC_LOGGER = LoggerBuilder.getLogger(folderName, fileName, fileLevel, consoleLevel);
        Log.DEFAULT_BASIC_LOGGER.addBasicPath(Log.class.getName());

        if (StringUtils.isNotBlank(systemLogLevel)) {
            String systemLogName = io.nuls.core.log.Log.BASIC_LOGGER.getLogger().getName();
            Logger systemLogger = context.getLogger(systemLogName);
            systemLogger.setAdditive(false);
            systemLogger.setLevel(Level.toLevel(systemLogLevel));
        }
        if (StringUtils.isNotBlank(packageLogPackages) && StringUtils.isNotBlank(packageLogLevels)) {
            String[] packages = packageLogPackages.split(",");
            String[] levels = packageLogLevels.split(",");
            int levelsLength = levels.length;
            String packagePath;
            String logLevel;
            Logger packageLogger;
            for (int i = 0, length = packages.length; i < length; i++) {
                packagePath = packages[i];
                if(i >= levelsLength) {
                    logLevel = "INFO";
                } else {
                    logLevel = levels[i];
                }
                packageLogger = context.getLogger(packagePath);
                packageLogger.setLevel(Level.toLevel(logLevel));
            }
        }
    }

    public static void configChainLog(Integer chainId, String folderName, String fileName, Level fileLevel, Level consoleLevel) {
        folderName = "chain-" + chainId + File.separator + folderName;
        NulsLogger nulsLogger = LoggerBuilder.getLogger(folderName, fileName, fileLevel, consoleLevel);
        nulsLogger.addBasicPath(Log.class.getName());
        Log.BASIC_LOGGER_MAP.put(chainId, nulsLogger);
    }

    public static void configDefaultLog(String folderName, String fileName, Level fileLevel, Level consoleLevel) {
        configDefaultLog(folderName, fileName, fileLevel, consoleLevel, null, null, null);
    }

}
