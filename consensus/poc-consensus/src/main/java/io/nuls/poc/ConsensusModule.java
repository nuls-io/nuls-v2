package io.nuls.poc;

import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.storage.LanguageService;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
/**
 * 共识模块启动及初始化管理
 * Consensus Module Startup and Initialization Management
 *
 * @author tag
 * 2018/3/4
 */
@Component
public class ConsensusModule extends RpcModule {

    public static void main(String[] args){
        if (args == null || args.length == 0) {
            args = new String[]{HostInfo.getLocalIP() + ":8887/ws"};
        }
        NulsRpcModuleBootstrap.run("io.nuls",args);
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
    public String getRpcCmdPackage(){
        return ConsensusConstant.RPC_PATH;
    }

    @Override
    public Module[] getDependencies() {
        return new Module[]{new Module(ModuleE.BL.abbr, "1.0"),new Module(ModuleE.TX.abbr, "1.0"),new Module(ModuleE.BL.abbr, "1.0")};
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CS.abbr,"1.0");
    }

    @Override
    public boolean doStart() {
        try {
            while (!ConnectManager.isReady()){
                Log.debug("wait depend modules ready");
                Thread.sleep(2000L);
            }
            SpringLiteContext.getBean(ChainManager.class).runChain();
            return true;
        }catch (Exception e){
            Log.error(e);
            return false;
        }
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    /**
     * 初始化系统编码
     * Initialization System Coding
     */
    private static void initSys() throws Exception {
        System.setProperty(ConsensusConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private static void initDB() throws Exception {
        Properties properties = ConfigLoader.loadProperties(ConsensusConstant.DB_CONFIG_NAME);
        String path = properties.getProperty(ConsensusConstant.DB_DATA_PATH, ConsensusConstant.DB_DATA_DEFAULT_PATH);
        RocksDBService.init(path);
        RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_LANGUAGE);
        RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_CONGIF);
    }

    /**
     * 初始化国际化资源文件语言
     * Initialization of International Resource File Language
     */
    private static void initLanguage() throws Exception {
        LanguageService languageService = SpringLiteContext.getBean(LanguageService.class);
        String languageDB = languageService.getLanguage();
        I18nUtils.loadLanguage(ConsensusBootStrap.class,"", "");
        String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
        I18nUtils.setLanguage(language);
        if (null == languageDB) {
            languageService.saveLanguage(language);
        }
    }
}
