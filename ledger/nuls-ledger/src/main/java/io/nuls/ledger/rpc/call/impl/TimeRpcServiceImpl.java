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
package io.nuls.ledger.rpc.call.impl;

import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.rpc.call.TimeRpcService;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.annotation.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用网络模块的RPC接口
 *
 * @author lan
 * @description
 * @date 2018/12/07
 **/
@Service
public class TimeRpcServiceImpl implements TimeRpcService {
    @Override
    public long getTime() {
        long time = 0;
        Map<String, Object> map = new HashMap<>();
        try {
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, CmdConstant.CMD_NW_GET_TIME_CALL, map, 100);
            if (null != response && response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                time = Long.valueOf(((Map) responseData.get(CmdConstant.CMD_NW_GET_TIME_CALL)).get("currentTimeMillis").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (time == 0) {
                time = System.currentTimeMillis();
            }
        }
        return time;
    }
}
