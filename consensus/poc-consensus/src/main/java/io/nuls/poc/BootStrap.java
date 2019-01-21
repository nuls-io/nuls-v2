package io.nuls.poc;

import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.storage.LanguageService;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.rpc.server.runtime.ServerRuntime;
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
 * 2018/11/7
 */
public class BootStrap {
    /**
     * 共识模块启动方法
     * Consensus module startup method
     */
    public static void main(String[] args) {
        try {
            initSys();
            initDB();
            SpringLiteContext.init(ConsensusConstant.CONTEXT_PATH);
            initLanguage();
            initServer();
            while (!ServerRuntime.isReady()) {
                Log.debug("wait depend modules ready");
                Thread.sleep(2000L);
            }
            SpringLiteContext.getBean(ChainManager.class).runChain();
        } catch (Exception e) {
            Log.error("consensus startup error！");
            Log.error(e);
        }
    }

    /**
     * 初始化系统编码
     * Initialization System Coding
     */
    private static void initSys() throws Exception{
        System.setProperty(ConsensusConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private static void initDB() throws Exception{
        Properties properties = ConfigLoader.loadProperties(ConsensusConstant.DB_CONFIG_NAME);
        String path = properties.getProperty(ConsensusConstant.DB_DATA_PATH, ConsensusConstant.DB_DATA_DEFAULT_PATH);
        RocksDBService.init(path);

    }

    /**
     * 初始化国际化资源文件语言
     * Initialization of International Resource File Language
     */
    private static void initLanguage() throws Exception{
        LanguageService languageService = SpringLiteContext.getBean(LanguageService.class);
        String languageDB = languageService.getLanguage();
        I18nUtils.loadLanguage("", "");
        String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
        I18nUtils.setLanguage(language);
        if (null == languageDB) {
            languageService.saveLanguage(language);
        }
    }

    /**
     * 共识模块启动WebSocket服务，用于其他模块连接共识模块与共识模块交互
     */
    private static void initServer() {
        try {
            try {
                WsServer.getInstance(ModuleE.CS)
                        .moduleRoles(new String[]{"1.0"})
                        .moduleVersion("1.0")
                        .dependencies(ModuleE.BL.abbr, "1.0")
                        .scanPackage("io.nuls.poc.rpc")
                        .connect("ws://127.0.0.1:8887");
                CmdDispatcher.syncKernel();
            } catch (Exception e) {
                Log.error("Account initServer failed", e);
            }
        } catch (Exception e) {
            Log.error("Consensus startup webSocket server error!");
            e.printStackTrace();
        }
    }
}
