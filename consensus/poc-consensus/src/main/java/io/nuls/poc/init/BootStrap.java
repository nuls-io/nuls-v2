package io.nuls.poc.init;

import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.config.ConfigItem;
import io.nuls.poc.model.bo.config.ConfigManager;
import io.nuls.poc.storage.ConfigeService;
import io.nuls.poc.storage.LanguageService;
import io.nuls.poc.utils.ConsensusConstant;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.JSONUtils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 共识模块启动及初始化管理
 * @author tag
 * 2018/11/7
 * */
public class BootStrap {
    public static void main(String[] args){
        try {
            /*SpringLiteContext.init("io.nuls.poc");
            initServer();
            TimeService.getInstance().run();*/
            /*String configJson = IoUtils.read(ConsensusConstant.CONFIG_FILE_PATH);
            System.out.println(configJson);
            Map<String,Object> map = JSONUtils.json2map(configJson);
            System.out.println(map);*/
            /*List<ConfigItem> configItemList = new ArrayList<>();
            for(int i=0;i<10;i++){
                ConfigItem item = new ConfigItem("item"+i,true);
                configItemList.add(item);
            }
            String str = JSONUtils.obj2json(configItemList);
            System.out.println(JSONUtils.obj2json(configItemList));*/
            /*String str = IoUtils.read(ConsensusConstant.CONFIG_FILE_PATH);
            List<ConfigItem> configItemList = JSONUtils.json2list(str,ConfigItem.class);
            System.out.println(configItemList.get(0).getValue());
            System.out.println(configItemList);*/
            /*Properties properties = ConfigLoader.loadProperties("db_config.properties");
            String path = properties.getProperty("rocksdb.datapath", "./data");
            System.out.println(path);*/
            ConfigBean bean = new ConfigBean();
            bean.setStopAgent_lockTime(10000);
            bean.setRedPublish_lockTime(2000);
            bean.setPacking_interval(10000);
            bean.setPacking_amount(5555);
            byte[] beanByte = ObjectUtils.objectToBytes(bean);
            ConfigBean bean1 = ObjectUtils.bytesToObject(beanByte);
            System.out.println(bean1.getCommission_max());
            System.out.println(bean1.getStopAgent_lockTime());
        }catch (Exception e){
            Log.error("consensus startup error！");
            Log.error(e);
        }
        System.out.println("end");
    }

    public static void init(int chain_id){
        try{
            //初始化系统参数
            initSys();
            //初始化数据库配置文件
            initDB();
            //初始化上下文
            SpringLiteContext.init("io.nuls.poc");
            //初始化国际资源文件语言
            initLanguage();
            //加载本地配置参数,并本地服务

            //启动WebSocket服务,向外提供RPC接口
            initServer();
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化系统参数
     * */
    public static void initSys(){
        try {
            System.setProperty("protostuff.runtime.allow_null_array_element", "true");
            System.setProperty("file.encoding", UTF_8.name());
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, UTF_8);
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化国际化资源文件语言
     * */
    public static void initLanguage(){
        try {
            LanguageService languageService = SpringLiteContext.getBean(LanguageService.class);
            String languageDB = (String) languageService.getLanguage();
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
     * */
    public static void sysStart(int chain_id){
        try {
            //读取数据库链信息配置
            ConfigeService configeService = SpringLiteContext.getBean(ConfigeService.class);
            Map<Integer, ConfigBean> configMap = configeService.getList();
            //如果系统是第一次运行，则本地数据库没有存储链信息，此时需要从配置文件读取主链配置信息
            if(configMap == null || configMap.size() == 0){
                //读取配置文件信息
                String configJson = IoUtils.read(ConsensusConstant.CONFIG_FILE_PATH);
                List<ConfigItem> configItemList = JSONUtils.json2list(configJson,ConfigItem.class);
                //初始化配置管理类
                ConfigManager.initManager(configItemList,chain_id);
                //初始化链相关表
                initTabel(chain_id);
                //启动内部服务

            }else{
                //初始化配置管理类
                ConfigManager.config_map.putAll(configMap);
                //初始化各条链相关表
                for (int id : configMap.keySet()) {
                    initTabel(id);
                }
                //启动内部服务,先启动主链，在启动子链

            }
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 共识模块启动WebSocket服务，用于其他模块连接共识模块与共识模块交互
     * */
    public static void initServer(){
        try {
            WsServer s = new WsServer(8888);
            s.init("consensus", null, "io.nuls.poc.rpc");
            s.start();
            CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
        }catch (Exception e){
            Log.error("Consensus startup webSocket server error!");
        }
    }

    /**
     * 初始化数据库
     * */
    public static void initDB(){
        try {
            Properties properties = ConfigLoader.loadProperties("db_config.properties");
            String path = properties.getProperty("rocksdb.datapath", "./data");
            RocksDBService.init(path);
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化链相关表
     * */
    public static void initTabel(int chain_id){
        try {
            //创建共识节点表
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chain_id);
            //创建共识信息表
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chain_id);
            //创建红黄牌信息表
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH+chain_id);
        }catch (Exception e){
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.info(e.getMessage());
            }else{
                Log.error(e);
            }
        }
    }
}
