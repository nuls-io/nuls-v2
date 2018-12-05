package io.nuls.poc.init;

import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.config.ConfigItem;
import io.nuls.poc.model.bo.tx.TxRegisterDetail;
import io.nuls.poc.storage.ConfigService;
import io.nuls.poc.storage.LanguageService;
import io.nuls.poc.utils.annotation.ResisterTx;
import io.nuls.poc.utils.enumeration.TxMethodType;
import io.nuls.poc.utils.manager.ConfigManager;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.poc.utils.manager.SchedulerManager;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.JSONUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
/**
 * 共识模块启动及初始化管理
 * Consensus Module Startup and Initialization Management
 *
 * @author tag
 * 2018/11/7
 * */
public class BootStrap {
    /**
     * 共识模块启动方法
     * Consensus module startup method
     * */
    public static void main(String[] args){
        try {
            initSys();
            initDB();
            SpringLiteContext.init(ConsensusConstant.CONTEXT_PATH);
            initLanguage();
            sysStart();
            registerTx();
            initServer();
        }catch (Exception e){
            Log.error("consensus startup error！");
            Log.error(e);
        }
    }

    /**
     * 初始化系统编码
     * Initialization System Coding
     * */
    private static void initSys(){
        try {
            System.setProperty(ConsensusConstant.SYS_FILE_ENCODING, UTF_8.name());
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, UTF_8);
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化数据库
     * Initialization database
     * */
    private static void initDB(){
        try {
            Properties properties = ConfigLoader.loadProperties(ConsensusConstant.DB_CONFIG_NAME);
            String path = properties.getProperty(ConsensusConstant.DB_DATA_PATH, ConsensusConstant.DB_DATA_DEFAULT_PATH);
            RocksDBService.init(path);
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化国际化资源文件语言
     * Initialization of International Resource File Language
     * */
    private static void initLanguage(){
        try {
            LanguageService languageService = SpringLiteContext.getBean(LanguageService.class);
            String languageDB = languageService.getLanguage();
            I18nUtils.loadLanguage("","");
            String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
            I18nUtils.setLanguage(language);
            if (null == languageDB) {
                languageService.saveLanguage(language);
            }
        }catch (Exception e){
            Log.error(e);
        }
    }


    /**
     * 配置信息读取，内部服务启动
     * Configuration information read, internal service start
     * */
    private static void sysStart(){
        try {
            //读取数据库链信息配置
            ConfigService configService = SpringLiteContext.getBean(ConfigService.class);
            Map<Integer, ConfigBean> configMap = configService.getList();
            /*
            如果系统是第一次运行，则本地数据库没有存储链信息，此时需要从配置文件读取主链配置信息
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if(configMap == null || configMap.size() == 0){
                int chainId  = ConsensusConstant.MAIN_CHAIN_ID;
                //读取配置文件信息
                String configJson = IoUtils.read(ConsensusConstant.CONFIG_FILE_PATH);
                List<ConfigItem> configItemList = JSONUtils.json2list(configJson,ConfigItem.class);
                //初始化配置管理类
                ConfigManager.initManager(configItemList,chainId);
                //初始化链相关表
                initTable(chainId);
                //初始化本地缓存数据（共识节点，委托信息，惩罚信息等）
                ConsensusManager.getInstance().initData(chainId);
                //启动内部服务
                SchedulerManager.createChainScheduler(chainId);
            }else{
                //初始化配置管理类
                ConfigManager.config_map.putAll(configMap);
                //初始化各条链相关表
                for (int id : configMap.keySet()) {
                    initTable(id);
                    //初始化本地缓存数据（共识节点，委托信息，惩罚信息等）
                    ConsensusManager.getInstance().initData(id);
                }
                //启动内部服务,先启动主链，在启动子链
                SchedulerManager.createChainScheduler(ConfigManager.config_map);
            }
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化链相关表
     * Initialization chain correlation table
     * */
    private static void initTable(int chainId){
        try {
            /*
            创建共识节点表
            Create consensus node tables

            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chainId);

            /*
            创建共识信息表
            Create consensus information tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainId);

            /*
            创建红黄牌信息表
            Creating Red and Yellow Card Information Table
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH+chainId);
        }catch (Exception e){
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.info(e.getMessage());
            }else{
                Log.error(e);
            }
        }
    }

    /**
     * 向交易模块注册交易
     * Register transactions with the transaction module
     * */
    private static void registerTx(){
        List<Class> classList = ScanUtil.scan(ConsensusConstant.RPC_PATH);
        if(classList == null || classList.size() == 0){
            return;
        }
        Map<Integer, TxRegisterDetail> registerDetailMap= new HashMap<>(16);
        for (Class clz:classList){
            Method[] methods = clz.getMethods();
            for (Method method:methods) {
                ResisterTx annotation = getRegisterAnnotation(method);
                if(annotation != null){
                    if (!registerDetailMap.containsKey(annotation.txType())){
                        registerDetailMap.put(annotation.txType(),new TxRegisterDetail(annotation.txType()));
                    }
                    if(annotation.methodType().equals(TxMethodType.COMMIT)){
                        registerDetailMap.get(annotation.txType()).setCommitCmd(annotation.methodName());
                    }else if(annotation.methodType().equals(TxMethodType.VALID)){
                        registerDetailMap.get(annotation.txType()).setValidateCmd(annotation.methodName());
                    }else if(annotation.methodType().equals(TxMethodType.ROLLBACK)){
                        registerDetailMap.get(annotation.txType()).setRollbackCmd(annotation.methodName());
                    }
                }
            }
        }
        //todo 向交易管理模块注册交易
    }

    private static ResisterTx getRegisterAnnotation(Method method){
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation:annotations) {
            if(ResisterTx.class.equals(annotation.annotationType())){
                return (ResisterTx)annotation;
            }
        }
        return null;
    }

    /**
     * 共识模块启动WebSocket服务，用于其他模块连接共识模块与共识模块交互
     * */
    private static void initServer(){
        try {
            try {
                WsServer.getInstance(ModuleE.CS)
                        .moduleRoles(new String[]{"1.0"})
                        .moduleVersion("1.0")
                        .dependencies(ModuleE.LG.abbr, "1.0")
                        .scanPackage("io.nuls.poc.rpc")
                        .connect("ws://127.0.0.1:8887");
            } catch (Exception e) {
                Log.error("Account initServer failed", e);
            }
        }catch (Exception e){
            Log.error("Consensus startup webSocket server error!");
            e.printStackTrace();
        }
    }
}
