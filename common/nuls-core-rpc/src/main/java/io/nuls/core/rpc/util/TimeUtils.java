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

package io.nuls.core.rpc.util;

import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.thread.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 时间服务类：用于同步网络模块标准时间
 * Time service class:Used to synchronize network standard time.
 *
 * @author vivi
 */
public class TimeUtils implements Runnable {

    private static TimeUtils instance = new TimeUtils();

    public static TimeUtils getInstance() {
        return instance;
    }

    /**
     * 重新同步时间间隔
     * Resynchronize the interval.
     * 1 minutes;
     */
    private long NET_REFRESH_TIME = 60 * 1000L;

    private static long offset;

    private boolean running;


    /**
     * 启动时间同步线程
     * Start the time synchronization thread.
     */
    public void start() {
        start(0);
    }

    public void start(long refreshTime) {
        if (running) {
            return;
        }
        running = true;
        if (refreshTime > 0) {
            NET_REFRESH_TIME = refreshTime;
        }
        Log.debug("----------- TimeUtils start -------------");
        ThreadUtils.createAndRunThread("TimeUtils", this, true);
    }


    @Override
    public void run() {
        while (true) {
            getNetworkTime();
            try {
                Thread.sleep(NET_REFRESH_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getNetworkTime() {
        Map<String, Object> params = new HashMap<>(8);
        params.put(Constants.VERSION_KEY_STR, "1.0");
        try {
            HashMap hashMap = (HashMap) request(ModuleE.NW.abbr, "nw_currentTimeMillis", params, 200L);
            long time = Long.valueOf(hashMap.get("currentTimeMillis").toString());
            offset = time - System.currentTimeMillis();
        } catch (NulsException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static Object request(String moduleCode, String cmd, Map params, Long timeout) throws NulsException {
        try {
            Response cmdResp;
            if (null == timeout) {
                cmdResp = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
            } else {
                cmdResp = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params, timeout);
            }
            Map resData = (Map) cmdResp.getResponseData();
            if (!cmdResp.isSuccess()) {

                String errorMsg = null;
                if (null == resData) {
                    errorMsg = String.format("Remote call fail. ResponseComment: %s ", cmdResp.getResponseComment());
                } else {
                    Map map = (Map) resData.get(cmd);
                    errorMsg = String.format("Remote call fail. msg: %s - code: %s - module: %s - interface: %s \n- params: %s ",
                            map.get("msg"), map.get("code"), moduleCode, cmd, JSONUtils.obj2PrettyJson(params));
                }
                throw new Exception(errorMsg);
            }
            return resData.get(cmd);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis() + offset;
    }

    public static long getCurrentTimeSeconds() {
        long millis = System.currentTimeMillis() + offset;
        return millis / 1000;
    }

    public static long getOffset() {
        return offset;
    }

}
