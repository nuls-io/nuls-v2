package io.nuls.contract;

import ch.qos.logback.classic.Level;
import io.nuls.contract.config.ContractConfig;
import io.nuls.contract.config.NulsConfig;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.LogUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.io.IoUtils;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.protocol.ProtocolGroupManager;
import io.nuls.core.rpc.util.ModuleHelper;
import io.nuls.core.rpc.util.RegisterHelper;
import io.nuls.core.rpc.util.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.NRC20_STANDARD_FILE;
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
            initContractDefaultLog();
            initNulsConfig();
            initDB();
            initNRC20Standard();
            chainManager.initChain();
            ModuleHelper.init(this);
        } catch (Exception e) {
            Log.error("ContractBootsrap init error!");
            throw new RuntimeException(e);
        }
    }

    private void initNulsConfig() {
        NulsConfig.DEFAULT_ENCODING = Charset.forName(contractConfig.getEncoding());
        NulsConfig.DATA_PATH = contractConfig.getDataPath() + File.separator + ModuleE.SC.name;
        NulsConfig.MAIN_ASSETS_ID = contractConfig.getMainAssetId();
        NulsConfig.MAIN_CHAIN_ID = contractConfig.getMainChainId();
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
        Level fileLevel = Level.toLevel(contractConfig.getLogFileLevel());
        Level consoleLevel = Level.toLevel(contractConfig.getLogConsoleLevel());
        LogUtil.configDefaultLog(ContractConstant.LOG_FILE_FOLDER, ContractConstant.LOG_FILE_NAME, fileLevel, consoleLevel, contractConfig.getSystemLogLevel(), contractConfig.getPackageLogPackages(), contractConfig.getPackageLogLevels());

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
     * 初始化数据库
     * Initialization database
     */
    private void initDB() throws IOException {
        RocksDBService.init(NulsConfig.DATA_PATH);
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
                new Module(ModuleE.NW.abbr, "1.0")};
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
        TimeUtils.getInstance().start();
        return RpcModuleState.Running;
    }

    @Override
    public void onDependenciesReady(Module module) {
        Log.info("dependencies [{}] ready", module.getName());
        if(module.getName().equals(ModuleE.TX.abbr)) {
            /*
             * 注册交易到交易管理模块
             */
            Map<Integer, Chain> chainMap = chainManager.getChainMap();
            for(Chain chain : chainMap.values()) {
                int chainId = chain.getChainId();
                boolean registerTx = RegisterHelper.registerTx(chainId, ProtocolGroupManager.getCurrentProtocol(chainId));
                Log.info("register tx type to tx module, chain id is {}, result is {}", chainId, registerTx);
            }
        }
        if(module.getName().equals(ModuleE.PU.abbr)) {
            /*
             * 注册协议到协议升级模块
             */
            Map<Integer, Chain> chainMap = chainManager.getChainMap();
            for(Chain chain : chainMap.values()) {
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
