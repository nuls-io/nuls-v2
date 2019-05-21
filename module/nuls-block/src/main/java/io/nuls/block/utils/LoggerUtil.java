/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.block.utils;

import ch.qos.logback.classic.Level;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;

/**
 * @author lan
 * @description
 * @date 2018/12/17
 **/
public class LoggerUtil {

    //update by zlj:  common.log->block.log
    /**
     * 公共日志
     */
    public static final NulsLogger commonLog = LoggerBuilder.getLogger("block");

    public static void init(int chainId, String levelString) {
        Level level = Level.valueOf(levelString);
        NulsLogger commonLog = LoggerBuilder.getLogger("chain-" + chainId + "/block/", "common", level);
        NulsLogger messageLog = LoggerBuilder.getLogger("chain-" + chainId + "/block/", "message", level);
        ChainContext context = ContextManager.getContext(chainId);
        context.setCommonLog(commonLog);
        context.setMessageLog(messageLog);
    }
}
