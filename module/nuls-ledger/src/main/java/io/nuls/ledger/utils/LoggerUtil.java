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

import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.model.ModuleE;

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
    public static final NulsLogger COMMON_LOG = LoggerBuilder.getLogger(ModuleE.Constant.LEDGER);
    public static final NulsLogger TX_TEST = LoggerBuilder.getLogger("commit_tx", 2);
    public static NulsLogger logger(int chainId) {
        if (null == loggerMap.get(String.valueOf(chainId))) {
            createLogger(chainId);
        }
        return loggerMap.get(String.valueOf(chainId));
    }

    public static void createLogger(int chainId) {
        loggerMap.put(String.valueOf(chainId), LoggerBuilder.getLogger(ModuleE.Constant.LEDGER, chainId));
    }
}
