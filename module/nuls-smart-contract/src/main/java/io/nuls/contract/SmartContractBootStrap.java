package io.nuls.contract;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ModuleHelper;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.protocol.RegisterHelper;
import io.nuls.base.protocol.cmd.TransactionDispatcher;
import io.nuls.contract.config.ContractConfig;
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
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.util.AddressPrefixDatas;
import io.nuls.core.rpc.util.NulsDateUtils;

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
public class SmartContractBootStrap extends RpcModule {

    @Autowired
    private ContractConfig contractConfig;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;
    @Autowired
    private ContractHelper contractHelper;

    public static void main(String[] args) throws Exception {
        systemConfig();
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }


    /**
     * 初始化模块信息，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        try {
            super.init();
            //增加地址工具类初始化
            AddressTool.init(addressPrefixDatas);
            initContractDefaultLog();
            initNulsConfig();
            initDB();
            initNRC20Standard();
            initNRC721Standard();
            initNRC1155Standard();
            chainManager.initChain();
            ModuleHelper.init(this);
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
            json = IoUtils.read(NRC20_STANDARD_FILE);
        } catch (Exception e) {
            // skip it
            Log.error("init NRC20Standard error.", e);
        }
        if (json == null) {
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
            json = IoUtils.read(NRC721_STANDARD_FILE);
        } catch (Exception e) {
            // skip it
            Log.error("init NRC721Standard error.", e);
        }
        if (json == null) {
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
            json = IoUtils.read(NRC1155_STANDARD_FILE);
        } catch (Exception e) {
            // skip it
            Log.error("init NRC1155Standard error.", e);
        }
        if (json == null) {
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
        skipTables.add(ContractDBConstant.DB_NAME_CONTRACT + "_" + contractConfig.getChainConfig().getChainId());
        RocksDBManager.init(ContractContext.DATA_PATH, null, skipTables);
        ContractUtil.createTable(ContractDBConstant.DB_NAME_CONGIF);
    }

    /**
     * 返回此模块的依赖模块
     * 可写作 return new Module[]{new Module(ModuleE.LG.abbr, "1.0"),new Module(ModuleE.TX.abbr, "1.0")}
     *
     * @return
     */
    @Override
    public Module[] declareDependent() {
        return new Module[]{new Module(ModuleE.TX.abbr, "1.0"),
                new Module(ModuleE.LG.abbr, "1.0"),
                new Module(ModuleE.BL.abbr, "1.0"),
                new Module(ModuleE.AC.abbr, "1.0"),
                new Module(ModuleE.NW.abbr, "1.0"),
                new Module(ModuleE.CS.abbr, "1.0")};
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

    /**
     * 已完成spring init注入，开始启动模块
     *
     * @return 如果启动完成返回true，模块将进入ready状态，若启动失败返回false，10秒后会再次调用此方法
     */
    @Override
    public boolean doStart() {
        TransactionDispatcher dispatcher = SpringLiteContext.getBean(TransactionDispatcher.class);
        TransactionCommitAdvice commitAdvice = SpringLiteContext.getBean(TransactionCommitAdvice.class);
        TransactionRollbackAdvice rollbackAdvice = SpringLiteContext.getBean(TransactionRollbackAdvice.class);
        dispatcher.register(commitAdvice, rollbackAdvice);
        Log.info("module chain do start");
        return true;
    }

    /**
     * 所有外部依赖进入ready状态后会调用此方法，正常启动后返回Running状态
     *
     * @return
     */
    @Override
    public RpcModuleState onDependenciesReady() {
        Log.info("all dependency module ready");
        NulsDateUtils.getInstance().start();
        return RpcModuleState.Running;
    }

    @Override
    public void onDependenciesReady(Module module) {
        Log.info("dependencies [{}] ready", module.getName());
        if (module.getName().equals(ModuleE.TX.abbr)) {
            /*
             * 注册交易到交易管理模块
             */
            Map<Integer, Chain> chainMap = chainManager.getChainMap();
            for (Chain chain : chainMap.values()) {
                int chainId = chain.getChainId();
                boolean registerTx = RegisterHelper.registerTx(chainId, ProtocolGroupManager.getCurrentProtocol(chainId));
                Log.info("register tx type to tx module, chain id is {}, result is {}", chainId, registerTx);
            }
        }
        // add by pierre at 2019-11-02 需要协议升级 done
        if (module.getName().equals(ModuleE.LG.abbr)) {
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
        }
        // end code by pierre
        if (module.getName().equals(ModuleE.PU.abbr)) {
            /*
             * 注册协议到协议升级模块
             */
            Map<Integer, Chain> chainMap = chainManager.getChainMap();
            for (Chain chain : chainMap.values()) {
                int chainId = chain.getChainId();
                RegisterHelper.registerProtocol(chainId);
                Log.info("register protocol to pu module, chain id is {}", chainId);
            }
        }
    }

    /**
     * 某个外部依赖连接丢失后，会调用此方法，可控制模块状态，如果返回Ready,则表明模块退化到Ready状态，当依赖重新准备完毕后，将重新触发onDependenciesReady方法，若返回的状态是Running，将不会重新触发onDependenciesReady
     *
     * @param dependenciesModule
     * @return
     */
    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
