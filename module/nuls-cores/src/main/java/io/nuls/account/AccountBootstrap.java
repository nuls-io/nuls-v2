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
     * Return the description information of the current module
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
     * Initialize module information, such as initializationRockDBWait, after initialization here, you can use other optionsbeanofafterPropertiesSetUsed in
     */
    public void init() {
        try {
            //Initialize configuration items
            initCfg();
            //Initialize database
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
             * Address tool initialization
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
     * Initialize database
     * Initialization database
     */
    private void initDB() throws Exception {
        //Read the configuration file, store the data in the root directory, initialize and open all table connections in that directory, and place them in the cache
        RocksDBService.init(accountConfig.getDataPath() + File.separator + ModuleE.AC.name);
        //Initialize Table
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
