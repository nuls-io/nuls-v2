package io.nuls.chain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.chain.config.NulsConfig;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.chain.service.impl.RpcServiceImpl;
import io.nuls.chain.storage.InitDB;
import io.nuls.chain.storage.impl.*;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Component;
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
@Component
public class ChainBootstrap extends RpcModule {
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{HostInfo.getLocalIP() + ":8887"};
        }
        NulsRpcModuleBootstrap.run("io.nuls.chain", args);
    }

    private void start() {
        try {
            Log.info("Chain Bootstrap start...");
            /* 自动注入 (Autowired) */
            /* 如果属性不匹配，不要报错 (If the attributes do not match, don't report an error) */
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            /* Read resources/module.ini to initialize the configuration */
            initCfg();
            /* Configuration to Map */
            initWithFile();
            /*storage info*/
            initWithDatabase();
            /* 把Nuls2.0主网信息存入数据库中 (Store the Nuls2.0 main network information into the database) */
            initMainChain();
            /* 进行数据库数据初始化（避免异常关闭造成的事务不一致） */
            initChainDatas();
            /*注册交易处理器*/
            regTxRpc();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            Log.error(e);
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
        I18nUtils.loadLanguage(ChainBootstrap.class, "languages", language);
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
        RocksDBService.init(ConfigManager.getValue(CmConstants.DB_DATA_PATH));
        InitDB assetStorage = SpringLiteContext.getBean(AssetStorageImpl.class);
        assetStorage.initTableName();
        InitDB blockHeightStorage = SpringLiteContext.getBean(BlockHeightStorageImpl.class);
        blockHeightStorage.initTableName();
        InitDB cacheDatasStorage = SpringLiteContext.getBean(CacheDatasStorageImpl.class);
        cacheDatasStorage.initTableName();
        InitDB chainAssetStorage = SpringLiteContext.getBean(ChainAssetStorageImpl.class);
        chainAssetStorage.initTableName();
        InitDB chainStorage = SpringLiteContext.getBean(ChainStorageImpl.class);
        chainStorage.initTableName();
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
        boolean regResult = false;
        while (!regResult) {
            regResult = rpcService.regTx();
        }
    }
    @Override
    public Module[] getDependencies() {
        return new Module[]{new Module(ModuleE.NW.abbr, "1.0"),
                new Module(ModuleE.TX.abbr, "1.0")};
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CM.abbr, "1.0");
    }
    /**
     * 初始化模块信息，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        super.init();
        try {
            /* 自动注入 (Autowired) */
            /* 如果属性不匹配，不要报错 (If the attributes do not match, don't report an error) */
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            /* Read resources/module.ini to initialize the configuration */
            initCfg();
            /* Configuration to Map */
            initWithFile();
            /*storage info*/
            initWithDatabase();
            /* 把Nuls2.0主网信息存入数据库中 (Store the Nuls2.0 main network information into the database) */
            initMainChain();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            Log.error(e);
        }
    }
    @Override
    public boolean doStart() {
        try {
            /* 进行数据库数据初始化（避免异常关闭造成的事务不一致） */
            initChainDatas();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            Log.error(e);
        }
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        try {
            /*注册交易处理器*/
            regTxRpc();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            Log.error(e);
        }
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
