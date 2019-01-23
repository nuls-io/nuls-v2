package io.nuls.transaction.init;

import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.thread.TimeService;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.h2.dao.impl.BaseService;
import io.nuls.transaction.db.h2.dao.impl.TransactionH2ServiceImpl;
import io.nuls.transaction.db.rocksdb.storage.LanguageStorageService;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.rpc.call.NetworkCall;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
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
            //初始化系统参数
            initSys();
            //初始化数据库配置文件
            initDB();
            //初始化上下文
            SpringLiteContext.init(TxConstant.CONTEXT_PATH);
            //初始化国际资源文件语言
            initLanguage();
            //启动WebSocket服务,向外提供RPC接口
            initServer();
            initH2Table();
            while (!ServerRuntime.isReady()) {
                Log.info("wait depend modules ready");
                Thread.sleep(2000L);
            }
            //启动链
            SpringLiteContext.getBean(ChainManager.class).runChain();
            //注册网络消息协议
            NetworkCall.registerProtocol();
            Log.debug("START-SUCCESS");
        }catch (Exception e){
            Log.error("Transaction startup error!");
            Log.error(e);
        }
    }

    /**
     * 初始化系统编码
     * */
    public static void initSys(){
        try {
            System.setProperty(TxConstant.SYS_ALLOW_NULL_ARRAY_ELEMENT, "true");
            System.setProperty(TxConstant.SYS_FILE_ENCODING, UTF_8.name());
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, UTF_8);
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化数据库
     * */
    public static void initDB(){
        try {
            Properties properties = ConfigLoader.loadProperties(TxConstant.DB_CONFIG_NAME);
            String path = properties.getProperty(TxConstant.DB_DATA_PATH,
                    TransactionBootStrap.class.getClassLoader().getResource("").getPath() + "data");
            RocksDBService.init(path);

            //todo 单个节点跑多链的时候 h2是否需要通过chain来区分数据库(如何分？)，待确认！！
            String resource = "mybatis/mybatis-config.xml";
            InputStream in = Resources.getResourceAsStream(resource);
            BaseService.sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
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
            I18nUtils.loadLanguage("languages","");
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
     * 共识模块启动WebSocket服务，用于其他模块连接共识模块与共识模块交互
     * */
    public static void initServer(){
        try {
            // todo 依赖模块 Start server instance
            WsServer.getInstance(ModuleE.TX)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .dependencies(ModuleE.NW.abbr, "1.0")
                    .dependencies(ModuleE.LG.abbr, "1.0")
                    .scanPackage("io.nuls.transaction.rpc.cmd")
                    .connect("ws://192.168.1.57:8887");

            // Get information from kernel
            CmdDispatcher.syncKernel();
        }catch (Exception e){
            Log.error("Transaction startup webSocket server error!");
            e.printStackTrace();
        }
    }

    /**
     * 创建H2的表, 如果存在则不会创建
     */
    public static void initH2Table(){
        TransactionH2Service ts = SpringLiteContext.getBean(TransactionH2Service.class);
        ts.createTxTablesIfNotExists(TxConstant.H2_TX_TABLE_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_INDEX_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_UNIQUE_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_NUMBER);
    }
}
