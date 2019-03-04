package io.nuls.account;

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountParam;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.bootstrap.NettyServer;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * @author: qinyifeng
 * @date: 2018/10/15
 */
public class AccountBootstrap {
    public static void main(String[] args) {
        Log.info("Account Bootstrap start...");

        try {
            //springLite容器初始化
            SpringLiteContext.init(AccountConstant.ACCOUNT_ROOT_PATH);
            //初始化配置
            initCfg();
            //初始化数据库
            initDB();
            //启动账户模块服务
            initServer();
            while (!ConnectManager.isReady()) {
                Log.info("wait depend modules ready");
                Thread.sleep(2000L);
            }
            //启动链
            SpringLiteContext.getBean(ChainManager.class).runChain();
            Log.debug("START-SUCCESS");
        } catch (Exception e) {
            Log.error("Account Bootstrap failed", e);
            System.exit(-1);
        }
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
                I18nUtils.loadLanguage(AccountBootstrap.class,"languages", language);
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
     * 初始化websocket服务器，供其他模块调用本模块接口
     *
     * @throws Exception
     */
    public static void initServer() {

        try {
            // Start server instance
            NettyServer.getInstance(ModuleE.AC)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .dependencies(ModuleE.LG.abbr, "1.0")
                    .dependencies(ModuleE.TX.abbr, "1.0")
                    .scanPackage("io.nuls.account.rpc.cmd");

            // Get information from kernel
            String kernelUrl = "ws://" + HostInfo.getLocalIP() + ":8887/ws";
            ConnectManager.getConnectByUrl(kernelUrl);
            ResponseMessageProcessor.syncKernel(kernelUrl);
        } catch (Exception e) {
            Log.error("Account initServer failed", e);
        }
    }
}
