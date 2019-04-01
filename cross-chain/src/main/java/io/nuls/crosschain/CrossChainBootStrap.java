package io.nuls.crosschain;

import io.nuls.crosschain.constant.CrossChainConstant;
import io.nuls.crosschain.srorage.LanguageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
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
 * 跨链模块启动及初始化管理
 * Cross Chain Module Startup and Initialization Management
 *
 * @author tag
 * 2019/04/01
 */
@Component
public class CrossChainBootStrap extends RpcModule {
    public static void main(String[] args){
        if (args == null || args.length == 0) {
            args = new String[]{HostInfo.getLocalIP() + ":8887/ws"};
        }
        NulsRpcModuleBootstrap.run(CrossChainConstant.BOOT_PATH,args);
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
        return CrossChainConstant.RPC_PATH;
    }

    @Override
    public Module[] getDependencies() {
        return new Module[]{

        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CC.abbr,"1.0");
    }

    @Override
    public boolean doStart() {
        return true;
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
        System.setProperty(CrossChainConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private static void initDB() throws Exception {
        Properties properties = ConfigLoader.loadProperties(CrossChainConstant.DB_CONFIG_NAME);
        String path = properties.getProperty(CrossChainConstant.DB_DATA_PATH, CrossChainConstant.DB_DATA_DEFAULT_PATH);
        RocksDBService.init(path);
        RocksDBService.createTable(CrossChainConstant.DB_NAME_CONSUME_LANGUAGE);
        RocksDBService.createTable(CrossChainConstant.DB_NAME_CONSUME_CONGIF);
    }

    /**
     * 初始化国际化资源文件语言
     * Initialization of International Resource File Language
     */
    private static void initLanguage() throws Exception {
        LanguageService languageService = SpringLiteContext.getBean(LanguageService.class);
        String languageDB = languageService.getLanguage();
        I18nUtils.loadLanguage(CrossChainBootStrap.class,"", "");
        String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
        I18nUtils.setLanguage(language);
        if (null == languageDB) {
            languageService.saveLanguage(language);
        }
    }
}
