package io.nuls.contract;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.storage.LanguageStorageService;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.bootstrap.NettyServer;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

import static io.nuls.contract.constant.ContractConstant.NRC20_STANDARD_FILE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 共识模块启动及初始化管理
 * Consensus Module Startup and Initialization Management
 *
 * @author tag
 * 2018/11/7
 */
public class ContractBootStrap {
    /**
     * 共识模块启动方法
     * Consensus module startup method
     */
    public static void main(String[] args) {
        try {
            initSys();
            initDB();
            SpringLiteContext.init(ContractConstant.CONTEXT_PATH);
            initLanguage();
            initServer();
            initERC20Standard();
            while (!ConnectManager.isReady()) {
                Log.debug("wait depend modules ready");
                Thread.sleep(2000L);
            }
            SpringLiteContext.getBean(ChainManager.class).runChain();
        } catch (Exception e) {
            Log.error("consensus startup error！");
            Log.error(e);
        }
    }

    private static void initERC20Standard() {
        String json = null;
        try {
            json = IoUtils.read(NRC20_STANDARD_FILE);
        } catch (Exception e) {
            // skip it
            Log.error("init NRC20Standard error.", e);
        }
        if(json == null) {
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
     * 初始化系统编码
     * Initialization System Coding
     */
    private static void initSys() throws Exception {
        System.setProperty("protostuff.runtime.allow_null_array_element", "true");
        System.setProperty(ContractConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private static void initDB() throws IOException {
        Properties properties = ConfigLoader.loadProperties(ContractConstant.DB_CONFIG_NAME);
        String path = properties.getProperty(ContractConstant.DB_DATA_PATH, ContractConstant.DB_DATA_DEFAULT_PATH);
        RocksDBService.init(path);

    }

    /**
     * 初始化国际化资源文件语言
     * Initialization of International Resource File Language
     */
    private static void initLanguage() throws Exception {
        LanguageStorageService languageService = SpringLiteContext.getBean(LanguageStorageService.class);
        String languageDB = languageService.getLanguage();
        //TODO pierre
        //I18nUtils.loadLanguage("", "");
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
                NettyServer.getInstance(ModuleE.SC)
                        .moduleRoles(new String[]{"1.0"})
                        .moduleVersion("1.0")
                        .scanPackage("io.nuls");
                String kernelUrl = "ws://" + HostInfo.getLocalIP() + ":8887/ws";
                /*
                 * 链接到指定地址
                 * */
                ConnectManager.getConnectByUrl(kernelUrl);
                /*
                 * 和指定地址同步
                 * */
                ResponseMessageProcessor.syncKernel(kernelUrl);
            } catch (Exception e) {
                Log.error("Smart Contract initServer failed", e);
            }
        } catch (Exception e) {
            Log.error("Consensus startup webSocket server error!");
            e.printStackTrace();
        }
    }
}
