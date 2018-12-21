package io.nuls.account;

import io.nuls.account.constant.AccountParam;
import io.nuls.account.init.AccountBootstrap;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.NoUse;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.thread.TimeService;

/**
 * @author EdwardChan
 *
 * Oct.8th 2018
 *
 * Initialize the Service before test
 *
 * 单元测试需要自动运行，不一样依赖于其它的进程，所以后续尝试将手动启动WsKernal,AcccountBootStrap放到这里统一初始化
 *
 *
 * **/
public class ServiceInitializer {

    public static boolean isInitialized = Boolean.FALSE;

    public static void initialize() throws Exception {
        if (!isInitialized) {
            //NoUse.mockKernel();
            AccountBootstrap.main(null);

            isInitialized = Boolean.TRUE;
        }
    }
}
