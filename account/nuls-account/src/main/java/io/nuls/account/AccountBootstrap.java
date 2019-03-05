package io.nuls.account;

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountParam;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.rpc.call.TransactionCmdCall;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author: qinyifeng
 * @date: 2018/10/15
 */
@Component
public class AccountBootstrap extends RpcModule {
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{HostInfo.getLocalIP() + ":8887/ws"};
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
    public Module[] getDependencies() {
        return new Module[]{new Module(ModuleE.LG.abbr, "1.0"),
                new Module(ModuleE.TX.abbr, "1.0")};
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
        super.init();
        initCfg();
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(AccountParam.getInstance().getDataPath());
    }

    /**
     * 已完成spring init注入，开始启动模块
     *
     * @return 如果启动完成返回true，模块将进入ready状态，若启动失败返回false，10秒后会再次调用此方法
     */
    @Override
    public boolean doStart() {
        Log.info("module ready");
        try {
            //初始化数据库
            initDB();
            //启动链
            SpringLiteContext.getBean(ChainManager.class).runChain();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 所有外部依赖进入ready状态后会调用此方法，正常启动后返回Running状态
     *
     * @return
     */
    @Override
    public RpcModuleState onDependenciesReady() {
        Log.info("account onDependenciesReady");
        //注册账户模块相关交易
        registerTx();
        Log.debug("START-SUCCESS");
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

    public static void initCfg() {
        try {
            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConfig.MODULES_CONFIG_FILE);

            AccountParam accountParam = AccountParam.getInstance();
            //set data save path
            accountParam.setDataPath(NulsConfig.MODULES_CONFIG.getCfgValue(AccountConstant.CFG_DB_SECTION, AccountConstant.DB_DATA_PATH, null));

            try {
                //set system encoding
                NulsConfig.DEFAULT_ENCODING = NulsConfig.MODULES_CONFIG.getCfgValue(AccountConstant.CFG_SYSTEM_SECTION, AccountConstant.CFG_SYSTEM_DEFAULT_ENCODING);
                //set system language
                String language = NulsConfig.MODULES_CONFIG.getCfgValue(AccountConstant.CFG_SYSTEM_SECTION, AccountConstant.CFG_SYSTEM_LANGUAGE);
                I18nUtils.loadLanguage(AccountBootstrap.class, "languages", language);
                I18nUtils.setLanguage(language);
                //ACCOUNTKEYSTORE_FOLDER_NAME
                String keystoreFolder = NulsConfig.MODULES_CONFIG.getCfgValue(AccountConstant.CFG_SYSTEM_SECTION, AccountConstant.CFG_SYSTEM_TKEYSTORE_FOLDER);
                if (StringUtils.isNotBlank(keystoreFolder)) {
                    NulsConfig.ACCOUNTKEYSTORE_FOLDER_NAME = keystoreFolder;
                }
                NulsConfig.KERNEL_MODULE_URL = NulsConfig.MODULES_CONFIG.getCfgValue(AccountConstant.CFG_SYSTEM_SECTION, AccountConstant.KERNEL_MODULE_URL);
            } catch (Exception e) {
                Log.error(e);
            }
        } catch (IOException e) {
            Log.error("Account Bootstrap initCfg failed", e);
            throw new RuntimeException("Account Bootstrap initCfg failed");
        }
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private static void initDB() throws Exception {
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(AccountParam.getInstance().getDataPath());

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
                Log.error(e.getMessage());
                throw new NulsException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
            } else {
                Log.info(e.getMessage());
            }
        }
    }

    /**
     * 注册交易
     */
    private static void registerTx() {
        try {
            ChainManager chainManager = SpringLiteContext.getBean(ChainManager.class);
            for (Map.Entry<Integer, Chain> entry : chainManager.getChainMap().entrySet()) {
                Chain chain = entry.getValue();
                //注册账户相关交易
                while (true) {
                    if (TransactionCmdCall.registerTx(chain.getConfig().getChainId())) {
                        break;
                    }
                    Thread.sleep(3000L);
                }
            }
        } catch (Exception e) {
            Log.error("Transaction registerTx error!");
            throw new RuntimeException(e);
        }
    }
}
