/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.protocol.utils.module;

import io.nuls.protocol.constant.RunningStatusEnum;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用核心模块接口的工具类
 * Utility class that invokes the kernel module interface
 *
 * @author captain
 * @version 1.0
 * @date 19-1-25 上午11:45
 */
public class KernelUtil {

    /**
     * 更新本模块的运行时状态
     *
     * @param chainId
     * @param moduleString
     * @param status
     * @return
     */
    public static boolean updateStatus(int chainId, String moduleString, RunningStatusEnum status) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("module", moduleString);
            params.put("status", status.ordinal());

            return ResponseMessageProcessor.requestAndResponse(ModuleE.KE.abbr, "updateStatus", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

}
