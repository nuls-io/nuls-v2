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

    private static ChainBootstrap chainBootstrap = null;

    private ChainBootstrap() {
    }

    public static ChainBootstrap getInstance() {
        if (chainBootstrap == null) {
            synchronized (ChainBootstrap.class) {
                if (chainBootstrap == null) {
                    chainBootstrap = new ChainBootstrap();
                }
            }
        }

        return chainBootstrap;
    }

    public static void main(String[] args) {
        ChainBootstrap.getInstance().start();
    }

    public void start() {
        try {

            Log.info("Chain Bootstrap start...");

            initCfg();

            initModule();

            SpringLiteContext.init("io.nuls.chain", new ModularServiceMethodInterceptor());

            // module init params

            startRpcServer();

            TimeService.getInstance().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCfg() throws Exception {

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

    private void initModule() throws Exception {

        CmConstants.PARAM_MAP.put(
                CmConstants.ASSET_SYMBOL_MAX, NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.PARAM, CmConstants.ASSET_SYMBOL_MAX, null));
        CmConstants.PARAM_MAP.put(
                CmConstants.ASSET_NAME_MAX, NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.PARAM, CmConstants.ASSET_NAME_MAX, null));
        CmConstants.PARAM_MAP.put(
                CmConstants.ASSET_DEPOSITNULS, NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.PARAM, CmConstants.ASSET_DEPOSITNULS, null));
        CmConstants.PARAM_MAP.put(
                CmConstants.ASSET_INITNUMBER_MIN, NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.PARAM, CmConstants.ASSET_INITNUMBER_MIN, null));
        CmConstants.PARAM_MAP.put(
                CmConstants.ASSET_INITNUMBER_MAX, NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.PARAM, CmConstants.ASSET_INITNUMBER_MAX, null));
        CmConstants.PARAM_MAP.put(
                CmConstants.ASSET_DECIMALPLACES_MIN, NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.PARAM, CmConstants.ASSET_DECIMALPLACES_MIN, null));
        CmConstants.PARAM_MAP.put(
                CmConstants.ASSET_DECIMALPLACES_MAX, NulsConfig.MODULES_CONFIG.getCfgValue(CmConstants.PARAM, CmConstants.ASSET_DECIMALPLACES_MAX, null));

        /*
          Read the configuration file, store the data in the root directory
          Initialize all table connections under the directory and put them into the cache
         */
        RocksDBService.init(CmRuntimeInfo.dataPath);

        if (!RocksDBService.existTable(CmConstants.PARAM)) {
            RocksDBService.createTable(CmConstants.PARAM);
        }

        for (String key : CmConstants.PARAM_MAP.keySet()) {
            byte[] value = RocksDBService.get(CmConstants.PARAM, key.getBytes());
            if (value != null) {
                CmConstants.PARAM_MAP.put(key, new String(value));
            }
        }
    }

    private void startRpcServer() throws Exception {
        WsServer wsServer = new WsServer(HostInfo.randomPort());
        wsServer.init("cm", new String[]{"m2", "m3"}, "io.nuls.chain.cmd");
        wsServer.startAndSyncKernel("ws://127.0.0.1:8887");
    }
}
