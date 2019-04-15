package io.nuls.poc;

import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConfig;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.storage.LanguageService;
import io.nuls.poc.utils.enumeration.ConsensusStatus;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.rpc.util.TimeUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.I18nUtils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
/**
 * 共识模块启动及初始化管理
 * Consensus Module Startup and Initialization Management
 *
 * @author tag
 * 2018/3/4
 */
@Component
public class ConsensusBootStrap extends RpcModule {

    @Autowired
    private ConsensusConfig consensusConfig;
    @Autowired
    private ChainManager chainManager;

    public static void main(String[] args){
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":8887/ws"};
        }
        NulsRpcModuleBootstrap.run(ConsensusConstant.BOOT_PATH, args);
    }
    /**
     * 初始化模块，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     * 在onStart前会调用此方法
     *
     */
    @Override
    public void init() {
        try {
            initSys();
            initDB();
            initLanguage();
            chainManager.initChain();
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 指定RpcCmd的包名
     * 可以不实现此方法，若不实现将使用spring init扫描的包
     * @return
     */
    @Override
    public Set<String> getRpcCmdPackage(){
        return Set.of(ConsensusConstant.RPC_PATH);
    }

    @Override
    public Module[] getDependencies() {
        return new Module[]{
                new Module(ModuleE.BL.abbr, "1.0"),
                new Module(ModuleE.AC.abbr, "1.0"),
                new Module(ModuleE.NW.abbr, "1.0"),
                new Module(ModuleE.LG.abbr, "1.0"),
                new Module(ModuleE.TX.abbr, "1.0")};
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CS.abbr,"1.0");
    }

    @Override
    public boolean doStart() {
        try {
            while (!isDependencieReady(new Module(ModuleE.TX.abbr, "1.0")) || !isDependencieReady(new Module(ModuleE.BL.abbr, "1.0"))){
                Log.debug("wait depend modules ready");
                Thread.sleep(2000L);
            }
            chainManager.runChain();
            return true;
        }catch (Exception e){
            Log.error(e);
            return false;
        }
    }

    @Override
    public void onDependenciesReady(Module module){
        try {
            if(module.getName().equals(ModuleE.TX.abbr)){
                chainManager.registerTx();
            }
        }catch (Exception e){
            Log.error(e);
        }
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        for (Chain chain:chainManager.getChainMap().values()) {
            chain.setConsensusStatus(ConsensusStatus.RUNNING);
        }
        Log.debug("cs onDependenciesReady");
        TimeUtils.getInstance().start();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        if(dependenciesModule.getName().equals(ModuleE.TX.abbr) || dependenciesModule.getName().equals(ModuleE.BL.abbr)){
            for (Chain chain:chainManager.getChainMap().values()) {
                chain.setConsensusStatus(ConsensusStatus.WAIT_RUNNING);
            }
        }
        return RpcModuleState.Ready;
    }

    /**
     * 初始化系统编码
     * Initialization System Coding
     */
    private void initSys() throws Exception {
        System.setProperty(ConsensusConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private void initDB() throws Exception {
        RocksDBService.init(consensusConfig.getDataFolder());
        RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_LANGUAGE);
        RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_CONGIF);
    }

    /**
     * 初始化国际化资源文件语言
     * Initialization of International Resource File Language
     */
    private void initLanguage() throws Exception {
        LanguageService languageService = SpringLiteContext.getBean(LanguageService.class);
        String languageDB = languageService.getLanguage();
        I18nUtils.loadLanguage(ConsensusBootStrap.class,"", consensusConfig.getLanguage());
        String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
        I18nUtils.setLanguage(language);
        if (null == languageDB) {
            languageService.saveLanguage(language);
        }
    }
}
