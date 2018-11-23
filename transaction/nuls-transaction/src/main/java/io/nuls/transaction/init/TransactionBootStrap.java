package io.nuls.transaction.init;

import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.thread.TimeService;
import io.nuls.transaction.constant.TransactionConstant;
import io.nuls.transaction.db.rocksdb.storage.LanguageStorageService;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public class TransactionBootStrap {

    public static void main(String[] args) {
        try {
            init(1);
            TimeService.getInstance().start();
        }catch (Exception e){
            Log.error("Transaction startup error!");
            Log.error(e);
        }
    }
    public static void init(int chain_id){
        try{
            //初始化系统参数
            initSys();
            //初始化数据库配置文件
            initDB();
            //初始化上下文
            SpringLiteContext.init(TransactionConstant.CONTEXT_PATH);
            //初始化国际资源文件语言
            initLanguage();
            //加载本地配置参数,并启动本地服务
            //sysStart(chain_id);
            //启动WebSocket服务,向外提供RPC接口
            initServer();
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化系统编码
     * */
    public static void initSys(){
        try {
            System.setProperty(TransactionConstant.SYS_ALLOW_NULL_ARRAY_ELEMENT, "true");
            System.setProperty(TransactionConstant.SYS_FILE_ENCODING, UTF_8.name());
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
            LanguageStorageService languageService = SpringLiteContext.getBean(LanguageStorageService.class);
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
     * 初始化数据库
     * */
    public static void initDB(){
        try {
            Properties properties = ConfigLoader.loadProperties(TransactionConstant.DB_CONFIG_NAME);
            String path = properties.getProperty(TransactionConstant.DB_DATA_PATH, TransactionConstant.DB_DATA_DEFAULT_PATH);
            RocksDBService.init(path);
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 共识模块启动WebSocket服务，用于其他模块连接共识模块与共识模块交互
     * */
    public static void initServer(){
        try {
            WsServer s = new WsServer(TransactionConstant.TX_CMD_PORT);
           /* s.init(TransactionConstant.TX_MODULE_NAME, null, TransactionConstant.TX_CMD_PATH);
            s.startAndSyncKernel(TransactionConstant.KERNEL_URL);*/
        }catch (Exception e){
            Log.error("Transaction startup webSocket server error!");
            e.printStackTrace();
        }
    }
}
