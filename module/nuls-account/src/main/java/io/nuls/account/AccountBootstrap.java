package io.nuls.account;

import io.nuls.account.config.AccountConfig;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.util.ModuleHelper;
import io.nuls.core.rpc.util.RegisterHelper;
import io.nuls.core.rpc.util.TimeUtils;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;

/**
 * @author: qinyifeng
 * @date: 2018/10/15
 */
@Component
public class AccountBootstrap extends RpcModule {

    @Autowired
    private AccountConfig accountConfig;

    @Autowired
    private ChainManager chainManager;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    /**
     * 返回此模块的依赖模块
     * 可写作 return new Module[]{new Module(ModuleE.LG.abbr, "1.0"),new Module(ModuleE.TX.abbr, "1.0")}
     *
     * @return
     */
    @Override
    public Module[] declareDependent() {
        return new Module[0];
    }

    /**
     * 返回当前模块的描述信息
     *
     * @return
     */
    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.AC.abbr, "1.0");
    }


    /**
     * 初始化模块信息，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        try {
            super.init();
            //初始化配置项
            initCfg();
            //初始化数据库
            initDB();
            chainManager.initChain();
            ModuleHelper.init(this);
        } catch (Exception e) {
            LoggerUtil.logger.error("AccountBootsrap init error!");
            throw new RuntimeException(e);
        }
    }

    /**
     * 已完成spring init注入，开始启动模块
     *
     * @return 如果启动完成返回true，模块将进入ready状态，若启动失败返回false，10秒后会再次调用此方法
     */
    @Override
    public boolean doStart() {
//        Map<String, Properties> lan = I18nUtils.getAll();
//        lan.entrySet().forEach(entry->{
//            entry.getValue().forEach((key,value)->{
//                Log.info("{}:{}",key,value);
//            });
//        });
        return true;
    }

    @Override
    public void onDependenciesReady(Module module) {
        if (ModuleE.TX.abbr.equals(module.getName())) {
            //注册账户模块相关交易
            chainManager.registerTx();
            LoggerUtil.logger.info("register tx ...");
        }
        if (ModuleE.PU.abbr.equals(module.getName())) {
            //注册账户模块相关交易
            chainManager.getChainMap().keySet().forEach(RegisterHelper::registerProtocol);
            LoggerUtil.logger.info("register protocol ...");
        }
    }

    /**
     * 所有外部依赖进入ready状态后会调用此方法，正常启动后返回Running状态
     *
     * @return
     */
    @Override
    public RpcModuleState onDependenciesReady() {
        TimeUtils.getInstance().start();
        LoggerUtil.logger.info("account onDependenciesReady");
        LoggerUtil.logger.info("START-SUCCESS");
        return RpcModuleState.Running;
    }

    /**
     * 某个外部依赖连接丢失后，会调用此方法，可控制模块状态，如果返回Ready,则表明模块退化到Ready状态，当依赖重新准备完毕后，将重新触发onDependenciesReady方法，若返回的状态是Running，将不会重新触发onDependenciesReady
     *
     * @param module
     * @return
     */
    @Override
    public RpcModuleState onDependenciesLoss(Module module) {
        return RpcModuleState.Ready;
    }

    public void initCfg() {
        try {
            NulsConfig.DATA_PATH = accountConfig.getDataPath();
            LoggerUtil.logger.info("dataPath:{}",NulsConfig.DATA_PATH);
            NulsConfig.DEFAULT_ENCODING = accountConfig.getEncoding();
            NulsConfig.MAIN_ASSETS_ID = accountConfig.getMainAssetId();
            NulsConfig.MAIN_CHAIN_ID = accountConfig.getMainChainId();
            NulsConfig.BLACK_HOLE_ADDRESS = AddressTool.getAddress(accountConfig.getBlackHoleAddress());
            if (StringUtils.isNotBlank(accountConfig.getKeystoreFolder())) {
                NulsConfig.ACCOUNTKEYSTORE_FOLDER_NAME = accountConfig.getDataPath() + accountConfig.getKeystoreFolder();
            }
        } catch (Exception e) {
            LoggerUtil.logger.error("Account Bootstrap initCfg failed", e);
            throw new RuntimeException("Account Bootstrap initCfg failed");
        }
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private  void initDB() throws Exception {
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(accountConfig.getDataPath()+AccountConstant.MODULE_DB_PATH);
        //初始化表
        try {
            //If tables do not exist, create tables.
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT);
            }
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_CONGIF)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_CONGIF);
            }
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT);
            }
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                LoggerUtil.logger.error(e.getMessage());
                throw new NulsException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
            } else {
                LoggerUtil.logger.info(e.getMessage());
            }
        }
    }

}
