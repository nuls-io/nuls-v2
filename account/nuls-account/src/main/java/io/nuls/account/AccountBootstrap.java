package io.nuls.account;

import io.nuls.account.config.AccountConfig;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.rpc.call.TransactionCmdCall;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.parse.I18nUtils;

import java.util.Map;

/**
 * @author: qinyifeng
 * @date: 2018/10/15
 */
@Configuration
public class AccountBootstrap extends RpcModule {

    @Autowired
    AccountConfig accountConfig;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":8887/ws"};
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
        try {
            super.init();
            //初始化配置项
            initCfg();
            //初始化数据库
            initDB();
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
        LoggerUtil.logger.info("module ready");
        try {
            //启动链
            SpringLiteContext.getBean(ChainManager.class).runChain();
            while (!isDependencieReady(new Module(ModuleE.TX.abbr, "1.0"))) {
                Thread.sleep(1000);
            }
            //注册账户模块相关交易
            registerTx();
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
        LoggerUtil.logger.info("account onDependenciesReady");
        LoggerUtil.logger.debug("START-SUCCESS");
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
//            String configJson = IoUtils.read(NulsConfig.MODULES_CONFIG_FILE);
//            List<ConfigItem> configItems = JSONUtils.json2list(configJson, ConfigItem.class);
//            Map<String, ConfigItem> configMap = new HashMap<>();
//            configItems.forEach(e -> configMap.put(e.getName(), e));
            //set data save path
//            NulsConfig.DATA_PATH = configMap.get(AccountConstant.DB_DATA_PATH).getValue();
            //改为通过配置文件注入
            NulsConfig.DATA_PATH = accountConfig.getDataPath();
            //set system encoding
//            NulsConfig.DEFAULT_ENCODING = configMap.get(AccountConstant.CFG_SYSTEM_DEFAULT_ENCODING).getValue();
            //改为通过配置文件注入
            NulsConfig.DEFAULT_ENCODING = accountConfig.getEncoding();
            //set system language
//            String language = configMap.get(AccountConstant.CFG_SYSTEM_LANGUAGE).getValue();
            //改为通过配置文件注入
            I18nUtils.loadLanguage(AccountBootstrap.class, "languages", accountConfig.getLanguage());
            I18nUtils.setLanguage(accountConfig.getLanguage());
            NulsConfig.MAIN_ASSETS_ID = accountConfig.getMainAssetId();
            NulsConfig.MAIN_CHAIN_ID = accountConfig.getMainChainId();
            //set keystore dir
//            String keystoreFolder = configMap.get(AccountConstant.CFG_SYSTEM_TKEYSTORE_FOLDER).getValue();
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
    private static void initDB() throws Exception {
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(NulsConfig.DATA_PATH+AccountConstant.MODULE_DB_PATH);
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
            LoggerUtil.logger.error("Transaction registerTx error!");
            throw new RuntimeException(e);
        }
    }
}
