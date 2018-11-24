package io.nuls.eventbus;

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

/**
 * @author: Niels Wang
 * @date: 2018/10/15
 */
public class EventBusBootstrap {
    public static void main(String[] args) {
        Log.info("Account Bootstrap start...");

        try {
            //启动账户模块服务
            initServer();
            //springLite容器初始化
            SpringLiteContext.init("io.nuls.account", new ModularServiceMethodInterceptor());
            //启动时间同步线程
            TimeService.getInstance().start();
        } catch (Exception e) {
            Log.error("Account Bootstrap failed", e);
            System.exit(-1);
        }
    }


    /**
     * 初始化websocket服务器，供其他模块调用本模块接口
     *
     * @throws Exception
     */
    public static void initServer(){

        try {
            WsServer server = new WsServer(8956);
            server.init("event", new String[]{}, "io.nuls.eventbus.rpc.cmd");
            server.start();
            CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
        }catch (Exception e)
        {
            Log.error("Account initServer failed", e);
        }
    }
}
