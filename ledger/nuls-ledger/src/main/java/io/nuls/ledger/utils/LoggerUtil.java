/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.ledger.utils;

import ch.qos.logback.classic.Level;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2018/12/17
 **/
public class LoggerUtil {
    /**
     * 日志
     */
    private static Map<String, NulsLogger> loggerMap = new HashMap<>();
    public static final String LOGGER_KEY1 = "ld";
    public static final String LOGGER_KEY2 = "ld_tx";
    public static final String LOGGER_KEY3 = "ld_txRb";
    public static final String LOGGER_KEY4 = "ld_txUncfRb";
    public static final String LOGGER_KEY5 = "ld_txAmount";
    public static final String LOGGER_KEY6 = "ld_txUncfRb2";

    public static NulsLogger logger = LoggerBuilder.getLogger("./ld", LOGGER_KEY1, Level.ALL);
//    public static NulsLogger txCommitLog = LoggerBuilder.getLogger("./ld", LOGGER_KEY2, Level.ALL);
//    public static NulsLogger txRollBackLog = LoggerBuilder.getLogger("./ld", LOGGER_KEY3, Level.ALL);
//    public static NulsLogger txUnconfirmedRollBackLog = LoggerBuilder.getLogger("./ld", LOGGER_KEY4, Level.ALL);
//    public static NulsLogger txAmount = LoggerBuilder.getLogger("./ld", LOGGER_KEY5, Level.ALL);
//    public static NulsLogger txUnconfirmedRollBackLog2 = LoggerBuilder.getLogger("./ld", LOGGER_KEY6, Level.ALL);

    public static NulsLogger logger(int chainId) {
        if(null == loggerMap.get(LOGGER_KEY1+chainId)){
            createLogger(chainId);
        }
        return loggerMap.get(LOGGER_KEY1+chainId);
    }

    public static NulsLogger txCommitLog(int chainId) {
        if(null == loggerMap.get(LOGGER_KEY2+chainId)){
            createLogger(chainId);
        }
        return loggerMap.get(LOGGER_KEY2+chainId);
    }

    public static NulsLogger txRollBackLog(int chainId) {
        if(null == loggerMap.get(LOGGER_KEY3+chainId)){
            createLogger(chainId);
        }
        return loggerMap.get(LOGGER_KEY3+chainId);
    }

    public static NulsLogger txUnconfirmedRollBackLog(int chainId) {
        if(null == loggerMap.get(LOGGER_KEY4+chainId)){
            createLogger(chainId);
        }
        return loggerMap.get(LOGGER_KEY4+chainId);
    }

    public static NulsLogger txAmount(int chainId) {
        if(null == loggerMap.get(LOGGER_KEY5+chainId)){
            createLogger(chainId);
        }
        return loggerMap.get(LOGGER_KEY5+chainId);
    }

    public static NulsLogger txUnconfirmedRollBackLog2(int chainId) {
        if(null == loggerMap.get(LOGGER_KEY6+chainId)){
            createLogger(chainId);
        }
        return loggerMap.get(LOGGER_KEY6+chainId);
    }

    public static void createLogger(int chainId) {
        String folderName = "./ld/" + chainId;
        loggerMap.put(LOGGER_KEY1+chainId, LoggerBuilder.getLogger(folderName, "ld", Level.ALL));
        loggerMap.put(LOGGER_KEY2+chainId, LoggerBuilder.getLogger(folderName, "tx", Level.ALL));
        loggerMap.put(LOGGER_KEY3+chainId, LoggerBuilder.getLogger(folderName, "txRb", Level.ALL));
        loggerMap.put(LOGGER_KEY4+chainId, LoggerBuilder.getLogger(folderName, "txUncfRb", Level.ALL));
        loggerMap.put(LOGGER_KEY5+chainId, LoggerBuilder.getLogger(folderName, "txAmount", Level.ALL));
        loggerMap.put(LOGGER_KEY6+chainId, LoggerBuilder.getLogger(folderName, "txUncfRb2", Level.ALL));
    }

}
