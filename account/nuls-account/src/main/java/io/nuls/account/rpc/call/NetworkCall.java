/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用网络模块接口的工具
 *
 * @author qinyifeng
 * @date 2018/12/25
 */
public class NetworkCall {

    /**
     * 获取当前网络时间
     *
     * @return
     * @throws NulsException
     */
    public static long getCurrentTimeMillis() throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_currentTimeMillis", params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.NW.abbr, "nw_currentTimeMillis", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("nw_currentTimeMillis");
            return (long) result.get("currentTimeMillis");
        } catch (Exception e) {
            Log.error("Calling remote interface failed. module:{} - interface:{}", ModuleE.NW.abbr, "nw_currentTimeMillis");
            throw new NulsException(e);
        }
    }

}
