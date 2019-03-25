package io.nuls.contract;

import io.nuls.contract.config.ContractConfig;
import io.nuls.contract.config.NulsConfig;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.JSONUtils;

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
public class ContractBootStrap extends RpcModule {

    @Autowired
    private ContractConfig contractConfig;

    public static void main(String[] args) throws Exception {
        systemConfig();
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":8887/ws"};
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
            initNulsConfig();
            initDB();
            initLanguage();
            initNRC20Standard();
        } catch (Exception e) {
            Log.error("AccountBootsrap init error!");
            throw new RuntimeException(e);
        }
    }

    private void initNulsConfig() {
        NulsConfig.DEFAULT_ENCODING = Charset.forName(contractConfig.getEncoding());
        NulsConfig.DATA_PATH = contractConfig.getDataPath() + File.separator + "contract";
        NulsConfig.MAIN_ASSETS_ID = contractConfig.getMainAssetId();
        NulsConfig.MAIN_CHAIN_ID = contractConfig.getMainChainId();
    }

    private void initLanguage() throws NulsException {
        I18nUtils.loadLanguage(ContractBootStrap.class, "languages", contractConfig.getLanguage());
        I18nUtils.setLanguage(contractConfig.getLanguage());
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
     * 初始化NRC20合约标准格式
     */
    private static void initNRC20Standard() {
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
    private static void initDB() throws IOException {
        RocksDBService.init(NulsConfig.DATA_PATH);
        ContractUtil.createTable(ContractDBConstant.DB_NAME_CONGIF);
        ContractUtil.createTable(ContractDBConstant.DB_NAME_LANGUAGE);
    }

    /**
     * 返回此模块的依赖模块
     * 可写作 return new Module[]{new Module(ModuleE.LG.abbr, "1.0"),new Module(ModuleE.TX.abbr, "1.0")}
     *
     * @return
     */
    @Override
    public Module[] getDependencies() {
        return new Module[]{new Module(ModuleE.TX.abbr, "1.0")};
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
        Log.info("module ready");
        try {
            while (!isDependencieReady(new Module(ModuleE.TX.abbr, "1.0"))) {
                Thread.sleep(1000);
            }
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
        Log.info("running");
        return RpcModuleState.Running;
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
