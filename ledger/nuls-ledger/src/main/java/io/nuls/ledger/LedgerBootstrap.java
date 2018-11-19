package io.nuls.ledger;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.config.AppConfig;
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
public class LedgerBootstrap {
    public static void main(String[] args) {
        Log.info("ledger Bootstrap start...");
        try {
            AppConfig.loadModuleConfig();

            initRocksDb();

            initServer();
            //springLite容器初始化
            SpringLiteContext.init("io.nuls.ledger", new ModularServiceMethodInterceptor());
            //启动时间同步线程
            TimeService.getInstance().start();
        } catch (Exception e) {
            Log.error("ledger Bootstrap failed", e);
            System.exit(-1);
        }
    }


    /**
     * 初始化websocket服务器，供其他模块调用本模块接口
     *
     * @throws Exception
     */
    public static void initServer() {
        try {
            WsServer server = new WsServer(8956);
            server.init("ledger", new String[]{}, "io.nuls.ledger.rpc.cmd");
            server.start();
            CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
        } catch (Exception e) {
            Log.error("ledger initServer failed", e);
        }
    }


    /**
     * 初始化数据库
     */
    public static void initRocksDb() {
        try {
            RocksDBService.init(AppConfig.moduleConfig.getDatabaseDir());
            RocksDBService.createTable(AppConfig.moduleConfig.getDatabaseName());
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
