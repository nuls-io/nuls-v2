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

import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;

import java.util.Map;

import static io.nuls.protocol.utils.LoggerUtil.commonLog;

/**
 * 调用网络模块接口的工具
 * Utility class that invokes the network module interface
 *
 * @author captain
 * @version 1.0
 * @date 19-1-25 上午11:28
 */
public class NetworkUtil {

    /**
     * 获取时间戳
     * Get timestamp
     *
     * @return
     */
    public static long currentTimeMillis() {
        try {
//            Map<String, Object> params = new HashMap<>(1);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_currentTimeMillis", null);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map result = (Map) responseData.get("nw_currentTimeMillis");
                return (Long) result.get("currentTimeMillis");
            }
        } catch (Exception e) {
            commonLog.error(e);
        }
        commonLog.error("get nw_currentTimeMillis fail, use System.currentTimeMillis");
        return System.currentTimeMillis();
    }

}
