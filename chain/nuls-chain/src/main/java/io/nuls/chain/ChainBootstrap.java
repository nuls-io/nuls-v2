package io.nuls.chain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.chain.config.NulsConfig;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.chain.service.impl.RpcServiceImpl;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.config.ConfigManager;

import java.util.Map;

import static io.nuls.chain.util.LoggerUtil.Log;

/**
 * 链管理模块启动类
 * Main class of BlockChain module
 *
 * @author tangyi
 * @date 2018/11/7
 */
public class ChainBootstrap {

    private static ChainBootstrap chainBootstrap = null;

    private ChainBootstrap() {
    }

    /**
     * 单例模式，只能启动一个链管理模块
     * Singleton mode, only one chain management module can be started
     *
     * @return ChainBootstrap
     */
    private static ChainBootstrap getInstance() {
        if (chainBootstrap == null) {
            synchronized (ChainBootstrap.class) {
                if (chainBootstrap == null) {
                    chainBootstrap = new ChainBootstrap();
                }
            }
        }

        return chainBootstrap;
    }

    /**
     * 链管理模块启动入口
     * Chain management module startup entry
     *
     * @param args null
     */
    public static void main(String[] args) {
        ChainBootstrap.getInstance().start();
    }

    private void start() {
        try {
            Log.info("Chain Bootstrap start...");

            /* 如果属性不匹配，不要报错 (If the attributes do not match, don't report an error) */
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            /* Read resources/module.ini to initialize the configuration */
            initCfg();

            /* Configuration to Map */
            initWithFile();

            /* Database data to Map */
            initWithDatabase();

            /* 自动注入 (Autowired) */
            SpringLiteContext.init("io.nuls.chain", new ModularServiceMethodInterceptor());

            /* 把Nuls2.0主网信息存入数据库中 (Store the Nuls2.0 main network information into the database) */
            initMainChain();
            /* 进行数据库数据初始化（避免异常关闭造成的事务不一致） */
            initChainDatas();
            /* 提供对外接口 (Provide external interface) */
            startRpcServer();
            waitDependencies();
            /*注册交易处理器*/
            regTxRpc();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            Log.error(e);
        }
    }

    void waitDependencies() throws InterruptedException {
        while (!ServerRuntime.isReady()) {
            Log.info("wait depend modules ready");
            Thread.sleep(2000L);
        }
    }

    /**
     * 读取resources/module.ini，初始化配置
     * Read resources/module.ini to initialize the configuration
     *
     * @throws Exception Any error will throw an exception
     */
    private void initCfg() throws Exception {
        /* 读取resources/module.ini (Read resources/module.json) */
        ConfigLoader.loadJsonCfg(NulsConfig.MODULES_CONFIG_FILE);
        /* 设置系统编码 (Set system encoding) */
        String encoding = ConfigManager.getValue(CmConstants.CFG_SYSTEM_DEFAULT_ENCODING);
        NulsConfig.setEncoding(encoding);
        /* 设置系统语言 (Set system language) */
        String language = ConfigManager.getValue(CmConstants.CFG_SYSTEM_LANGUAGE);
        I18nUtils.loadLanguage("languages", language);
        I18nUtils.setLanguage(language);
    }

    /**
     * 把初始化配置的值从NulsConfig.MODULES_CONFIG中放到Map中，不同类型的值放进不同Map
     * Put the value of the initial configuration from NulsConfig.MODULES_CONFIG into the Map
     */
    private void initWithFile() {
        /* 基本配置信息 (Basic configuration) */
        configToMap(CmConstants.PARAM_MAP, CmConstants.ASSET_SYMBOL_MAX);
        configToMap(CmConstants.PARAM_MAP, CmConstants.ASSET_NAME_MAX);
        configToMap(CmConstants.PARAM_MAP, CmConstants.ASSET_DEPOSIT_NULS);
        configToMap(CmConstants.PARAM_MAP, CmConstants.ASSET_INIT_NUMBER_MIN);
        configToMap(CmConstants.PARAM_MAP, CmConstants.ASSET_INIT_NUMBER_MAX);
        configToMap(CmConstants.PARAM_MAP, CmConstants.ASSET_DECIMAL_PLACES_MIN);
        configToMap(CmConstants.PARAM_MAP, CmConstants.ASSET_DECIMAL_PLACES_MAX);
        configToMap(CmConstants.PARAM_MAP, CmConstants.ASSET_RECOVERY_RATE);

        /* 默认的跨链主资产 (Nuls) (Default cross-chain master asset) */
        configToMap(CmConstants.CHAIN_ASSET_MAP, CmConstants.NULS_CHAIN_ID);
        configToMap(CmConstants.CHAIN_ASSET_MAP, CmConstants.NULS_CHAIN_NAME);
        configToMap(CmConstants.CHAIN_ASSET_MAP, CmConstants.NULS_ASSET_ID);
        configToMap(CmConstants.CHAIN_ASSET_MAP, CmConstants.NULS_ASSET_MAX);
        configToMap(CmConstants.CHAIN_ASSET_MAP, CmConstants.NULS_ASSET_SYMBOL);
    }

    /**
     * 如果数据库中有相同的配置，则以数据库为准
     * If the database has the same configuration, use the database data
     *
     * @throws Exception Any error will throw an exception
     */
    private void initWithDatabase() throws Exception {
        /* 打开数据库连接 (Open database connection) */
        RocksDBService.init(CmConstants.DATA_PATH);

        /* 配置信息的表：param (Table of configuration: param) */
        if (!RocksDBService.existTable(CmConstants.PARAM)) {
            RocksDBService.createTable(CmConstants.PARAM);
        }

        /* 读取表中的数据更新Map (Read the data in the table, then update map) */
        for (Map.Entry<String, String> entry : CmConstants.PARAM_MAP.entrySet()) {
            String key = entry.getKey();
            byte[] value = RocksDBService.get(CmConstants.PARAM, key.getBytes());
            if (value != null) {
                CmConstants.PARAM_MAP.put(key, new String(value));
            }
        }
    }

    private void configToMap(Map<String, String> toMap, String key) {
        toMap.put(key, ConfigManager.getValue(key));
    }

    /**
     * 把Nuls2.0主网信息存入数据库中
     * Store the Nuls2.0 main network information into the database
     *
     * @throws Exception Any error will throw an exception
     */
    private void initMainChain() throws Exception {
        SpringLiteContext.getBean(ChainService.class).initMainChain();
    }

    private void initChainDatas() throws Exception {
        SpringLiteContext.getBean(CacheDataService.class).initBlockDatas();
    }

    private void regTxRpc() throws Exception {
        RpcService rpcService = SpringLiteContext.getBean(RpcServiceImpl.class);
        rpcService.regTx();
    }

    /**
     * @throws Exception Any error will throw an exception
     */
    private void startRpcServer() throws Exception {
        WsServer.getInstance(ModuleE.CM)
                .moduleRoles(new String[]{"1.0"})
                .moduleVersion("1.0")
                .dependencies(ModuleE.NW.abbr, "1.1")
                .dependencies(ModuleE.TX.abbr, "1.1")
                .scanPackage("io.nuls.chain.cmd")
                .connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();
    }

    /**
     * @throws Exception Any error will throw an exception
     */
    private void regTxHandler() throws Exception {

    }

}
