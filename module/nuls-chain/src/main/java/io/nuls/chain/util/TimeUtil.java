package io.nuls.chain.util;

import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.rpc.call.impl.RpcServiceImpl;
import io.nuls.core.core.ioc.SpringLiteContext;

public class TimeUtil {
    static long latestGetTime = System.currentTimeMillis();
    static long offset = 0;
    final static long TIMEOUT_MILLIS = 300000;

    public static long getCurrentTime() {
        long now = System.currentTimeMillis();
        if (now - latestGetTime >= TIMEOUT_MILLIS) {
            RpcService timeRpcService = SpringLiteContext.getBean(RpcServiceImpl.class);
            offset = timeRpcService.getTime() - System.currentTimeMillis();
            latestGetTime = now;
        }
        return (System.currentTimeMillis() + offset);
    }
}