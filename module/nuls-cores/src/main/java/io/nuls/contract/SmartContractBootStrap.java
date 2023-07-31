package io.nuls.contract;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.protocol.cmd.TransactionDispatcher;
import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.ContractTokenAssetsInfo;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.tx.common.TransactionCommitAdvice;
import io.nuls.contract.tx.common.TransactionRollbackAdvice;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.LogUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.io.IoUtils;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rockdb.manager.RocksDBManager;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.util.AddressPrefixDatas;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.nuls.contract.constant.ContractConstant.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 智能合约模块启动及初始化管理
 * Smart Contract Module Startup and Initialization Management
 *
 * @author: PierreLuo
 * @date: 2019-03-14
 */
@Component
public class SmartContractBootStrap implements INulsCoresBootstrap {

    @Autowired
    private NulsCoresConfig contractConfig;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;
    @Autowired
    private ContractHelper contractHelper;

    @Override
    public int order() {
        return 7;
    }

    @Override
    public void mainFunction(String[] args) {
        this.init();
    }

    public void init() {
        try {
            systemConfig();
            initContractDefaultLog();
            initNulsConfig();
            initDB();
            initNRC20Standard();
            initNRC721Standard();
            initNRC1155Standard();
            chainManager.initChain();
        } catch (Exception e) {
            Log.error("ContractBootsrap init error!");
            throw new RuntimeException(e);
        }
    }

    private void initNulsConfig() {
        ContractContext.DEFAULT_ENCODING = Charset.forName(contractConfig.getEncoding());
        ContractContext.DATA_PATH = contractConfig.getDataPath() + File.separator + ModuleE.SC.name;
        ContractContext.MAIN_ASSETS_ID = contractConfig.getMainAssetId();
        ContractContext.MAIN_CHAIN_ID = contractConfig.getMainChainId();
        ContractContext.CHAIN_ID = contractConfig.getChainId();
        ContractContext.ASSET_ID = contractConfig.getAssetId();
        if (StringUtils.isNotBlank(contractConfig.getCrossTokenSystemContract())) {
            ContractContext.CROSS_CHAIN_SYSTEM_CONTRACT = AddressTool.getAddress(contractConfig.getCrossTokenSystemContract());
        }
        ContractContext.setContractHelper(contractHelper);
    }

    /**
     * 初始化系统编码
     * Initialization System Coding
     */
    private static void systemConfig() throws Exception {
        System.setProperty("protostuff.runtime.allow_null_array_element", "true");
        System.setProperty(ContractConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    /**
     * 初始化模块日志
     */
    private void initContractDefaultLog() {
        LogUtil.configDefaultLog(ContractConstant.LOG_FILE_NAME, contractConfig.getPackageLogPackages(), contractConfig.getPackageLogLevels());

    }

    /**
     * 初始化NRC20合约标准格式
     */
    private void initNRC20Standard() {
        String json = null;
        try {
            json = IoUtils.read("contract" + File.separator + NRC20_STANDARD_FILE);
        } catch (Exception e) {
            // skip it
            Log.error("init NRC20Standard error.", e);
        }
        if (json == null) {
            Log.warn("init NRC20Standard empty data file!");
            return;
        }

        Map<String, ProgramMethod> jsonMap = null;
        try {
            jsonMap = JSONUtils.json2map(json, ProgramMethod.class);
        } catch (Exception e) {
            Log.error("init NRC20Standard map error.", e);
        }
        VMContext.setNrc20Methods(jsonMap);
    }

    /**
     * 初始化NRC721合约标准格式
     */
    private void initNRC721Standard() {
        String json = null;
        try {
            json = IoUtils.read("contract" + File.separator + NRC721_STANDARD_FILE);
        } catch (Exception e) {
            // skip it
            Log.error("init NRC721Standard error.", e);
        }
        if (json == null) {
            Log.warn("init NRC721Standard empty data file!");
            return;
        }

        Map<String, ProgramMethod> jsonMap = null;
        try {
            jsonMap = JSONUtils.json2map(json, ProgramMethod.class);
        } catch (Exception e) {
            Log.error("init NRC721Standard map error.", e);
        }
        VMContext.setNrc721Methods(jsonMap);
    }

    /**
     * 初始化NRC1155合约标准格式
     */
    private void initNRC1155Standard() {
        String json = null;
        try {
            json = IoUtils.read("contract" + File.separator + NRC1155_STANDARD_FILE);
        } catch (Exception e) {
            // skip it
            Log.error("init NRC1155Standard error.", e);
        }
        if (json == null) {
            Log.warn("init NRC1155Standard empty data file!");
            return;
        }

        Map<String, ProgramMethod> jsonMap = null;
        try {
            jsonMap = JSONUtils.json2map(json, ProgramMethod.class);
        } catch (Exception e) {
            Log.error("init NRC20Standard map error.", e);
        }
        VMContext.setNrc1155Methods(jsonMap);
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private void initDB() throws Exception {
        Set<String> skipTables = new HashSet<>();
        skipTables.add(ContractDBConstant.DB_NAME_CONTRACT + "_" + contractConfig.getChainId());
        RocksDBManager.init(ContractContext.DATA_PATH, null, skipTables);
        ContractUtil.createTable(ContractDBConstant.DB_NAME_CONGIF);
    }

    /**
     * 返回当前模块的描述信息
     *
     * @return
     */
    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.SC.abbr, "1.0");
    }

    private boolean doStart() {
        TransactionDispatcher dispatcher = SpringLiteContext.getBean(TransactionDispatcher.class);
        TransactionCommitAdvice commitAdvice = SpringLiteContext.getBean(TransactionCommitAdvice.class);
        TransactionRollbackAdvice rollbackAdvice = SpringLiteContext.getBean(TransactionRollbackAdvice.class);
        dispatcher.register(ModuleE.SC, commitAdvice, rollbackAdvice);
        Log.info("module chain do start");
        return true;
    }

    public void onDependenciesReady() {
        doStart();
        Log.info("all dependency module ready");
        // add by pierre at 2019-11-02 需要协议升级 done
        // 缓存token注册资产的资产ID和token合约地址
        Map<Integer, Chain> chainMap = chainManager.getChainMap();
        for (Chain chain : chainMap.values()) {
            int chainId = chain.getChainId();
            if(ProtocolGroupManager.getCurrentVersion(chainId) < ContractContext.UPDATE_VERSION_V250) {
                continue;
            }
            List<Map> regTokenList;
            try {
                regTokenList = LedgerCall.getRegTokenList(chainId);
                if(regTokenList != null && !regTokenList.isEmpty()) {
                    Map<String, ContractTokenAssetsInfo> tokenAssetsInfoMap = chain.getTokenAssetsInfoMap();
                    Map<String, String> tokenAssetsContractAddressInfoMap = chain.getTokenAssetsContractAddressInfoMap();
                    regTokenList.stream().forEach(map -> {
                        int assetId = Integer.parseInt(map.get("assetId").toString());
                        String tokenContractAddress = map.get("assetOwnerAddress").toString();
                        tokenAssetsInfoMap.put(tokenContractAddress, new ContractTokenAssetsInfo(chainId, assetId));
                        tokenAssetsContractAddressInfoMap.put(chainId + "-" + assetId, tokenContractAddress);
                    });
                }
            } catch (NulsException e) {
                throw new RuntimeException(e);
            }
            Log.info("initial cross token asset completed");
        }
        // end code by pierre
    }

}
