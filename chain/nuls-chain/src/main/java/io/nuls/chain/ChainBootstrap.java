package io.nuls.chain;

import io.nuls.chain.config.NulsChainConfig;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.chain.service.impl.RpcServiceImpl;
import io.nuls.chain.storage.InitDB;
import io.nuls.chain.storage.impl.*;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.rpc.util.TimeUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.config.ConfigManager;

/**
 * 链管理模块启动类
 * Main class of BlockChain module
 *
 * @author tangyi
 * @date 2018/11/7
 */
@Component
public class ChainBootstrap extends RpcModule {
    @Autowired
    private NulsChainConfig nulsChainConfig;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":8887/ws"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }


    /**
     * 读取resources/module.ini，初始化配置
     * Read resources/module.ini to initialize the configuration
     *
     * @throws Exception Any error will throw an exception
     */
    private void initCfg() throws Exception {
        /* 设置系统语言 (Set system language) */
        I18nUtils.loadLanguage(ChainBootstrap.class, "languages", nulsChainConfig.getLanguage());
        I18nUtils.setLanguage(nulsChainConfig.getLanguage());
    }
    /**
     * 如果数据库中有相同的配置，则以数据库为准
     * If the database has the same configuration, use the database entity
     *
     * @throws Exception Any error will throw an exception
     */
    private void initWithDatabase() throws Exception {
        /* 打开数据库连接 (Open database connection) */
        RocksDBService.init(ConfigManager.getValue(nulsChainConfig.getDataPath()));
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
            /* Read resources/module.ini to initialize the configuration */
            initCfg();
            /*storage info*/
            initWithDatabase();
            /* 把Nuls2.0主网信息存入数据库中 (Store the Nuls2.0 main network information into the database) */
            initMainChain();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            LoggerUtil.logger().error(e);
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
            LoggerUtil.logger().error(e);
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
            LoggerUtil.logger().error(e);
        }
        TimeUtils.getInstance().start();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
