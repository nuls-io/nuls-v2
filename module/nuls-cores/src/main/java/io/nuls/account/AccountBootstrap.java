package io.nuls.account;

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.util.AddressPrefixDatas;

import java.io.File;

/**
 * @author: qinyifeng
 * @date: 2018/10/15
 */
@Component
public class AccountBootstrap implements INulsCoresBootstrap {

    @Autowired
    private NulsCoresConfig accountConfig;

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;

    @Override
    public int order() {
        return 6;
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

    @Override
    public void mainFunction(String[] args) {
        init();
    }

    /**
     * 初始化模块信息，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     */
    public void init() {
        try {
            //初始化配置项
            initCfg();
            //初始化数据库
            initDB();
            chainManager.initChain();
        } catch (Exception e) {
            LoggerUtil.LOG.error("AccountBootsrap init error!");
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onDependenciesReady() {
        LoggerUtil.LOG.info("account onDependenciesReady");
        LoggerUtil.LOG.info("START-SUCCESS");
    }

    public void initCfg() {
        try {
            NulsConfig.DATA_PATH = accountConfig.getDataPath();
            LoggerUtil.LOG.info("dataPath:{}", NulsConfig.DATA_PATH);
            NulsConfig.DEFAULT_ENCODING = accountConfig.getEncoding();
            NulsConfig.MAIN_ASSETS_ID = accountConfig.getMainAssetId();
            NulsConfig.MAIN_CHAIN_ID = accountConfig.getMainChainId();
            NulsConfig.BLACK_HOLE_PUB_KEY = HexUtil.decode(accountConfig.getBlackHolePublicKey());
            /**
             * 地址工具初始化
             */
            AddressTool.init(addressPrefixDatas);
            AddressTool.addPrefix(accountConfig.getChainId(), accountConfig.getAddressPrefix());
            if (StringUtils.isNotBlank(accountConfig.getKeystoreFolder())) {
                NulsConfig.ACCOUNTKEYSTORE_FOLDER_NAME = accountConfig.getDataPath() + accountConfig.getKeystoreFolder();
            }
        } catch (Exception e) {
            LoggerUtil.LOG.error("Account Bootstrap initCfg failed :{}", e.getMessage(), e);
            throw new RuntimeException("Account Bootstrap initCfg failed");
        }
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private void initDB() throws Exception {
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(accountConfig.getDataPath() + File.separator + ModuleE.AC.name);
        //初始化表
        try {
            //If tables do not exist, create tables.
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT);
            }
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT);
            }
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK);
            }
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_CONTRACT_CALL)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_CONTRACT_CALL);
            }
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                LoggerUtil.LOG.error(e.getMessage());
                throw new NulsException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
            } else {
                LoggerUtil.LOG.info(e.getMessage());
            }
        }
    }

}
