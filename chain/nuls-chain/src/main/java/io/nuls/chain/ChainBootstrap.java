package io.nuls.chain;

import io.nuls.chain.config.NulsConfig;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.thread.TimeService;

/**
 * @author tangyi
 * @date 2018/11/7
 */
public class ChainBootstrap {
    public static void main(String[] args) {
        try {

            Log.info("Chain Bootstrap start...");

            initCfg();

            //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
            RocksDBService.init(CmRuntimeInfo.dataPath);

            SpringLiteContext.init("io.nuls.chain", new ModularServiceMethodInterceptor());

            startRpcServer();

            TimeService.getInstance().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initCfg() throws Exception {

        NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConfig.MODULES_CONFIG_FILE);

        // database data path
        CmRuntimeInfo.dataPath = NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.DB, CmConstants.DB_DATA_PATH, null);

        // set system encoding
        NulsConfig.DEFAULT_ENCODING = NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.CFG_SYSTEM_SECTION, CmConstants.CFG_SYSTEM_DEFAULT_ENCODING);

        // set system language
        String language = NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.CFG_SYSTEM_SECTION, CmConstants.CFG_SYSTEM_LANGUAGE);
        I18nUtils.loadLanguage("languages", language);
        I18nUtils.setLanguage(language);

    }

    private static void startRpcServer() throws Exception {
        WsServer wsServer = new WsServer(HostInfo.randomPort());
        wsServer.init("cm", new String[]{"m2", "m3"}, "io.nuls.chain.cmd");
        wsServer.startAndSyncKernel("ws://127.0.0.1:8887");
    }
}
