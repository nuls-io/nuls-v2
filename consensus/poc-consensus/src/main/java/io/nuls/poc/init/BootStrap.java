package io.nuls.poc.init;

import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.tx.TxRegisterDetail;
import io.nuls.poc.storage.LanguageService;
import io.nuls.poc.utils.annotation.ResisterTx;
import io.nuls.poc.utils.enumeration.TxMethodType;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;

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
            SpringLiteContext.getBean(ChainManager.class).runChain();
            registerTx();
            initServer();
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
        try {
            System.setProperty(ConsensusConstant.SYS_FILE_ENCODING, UTF_8.name());
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, UTF_8);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private static void initDB() throws Exception{
        try {
            Properties properties = ConfigLoader.loadProperties(ConsensusConstant.DB_CONFIG_NAME);
            String path = properties.getProperty(ConsensusConstant.DB_DATA_PATH, ConsensusConstant.DB_DATA_DEFAULT_PATH);
            RocksDBService.init(path);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 初始化国际化资源文件语言
     * Initialization of International Resource File Language
     */
    private static void initLanguage() throws Exception{
        try {
            LanguageService languageService = SpringLiteContext.getBean(LanguageService.class);
            String languageDB = languageService.getLanguage();
            I18nUtils.loadLanguage("", "");
            String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
            I18nUtils.setLanguage(language);
            if (null == languageDB) {
                languageService.saveLanguage(language);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 向交易模块注册交易
     * Register transactions with the transaction module
     */
    private static void registerTx() {
        List<Class> classList = ScanUtil.scan(ConsensusConstant.RPC_PATH);
        if (classList == null || classList.size() == 0) {
            return;
        }
        Map<Integer, TxRegisterDetail> registerDetailMap = new HashMap<>(16);
        for (Class clz : classList) {
            Method[] methods = clz.getMethods();
            for (Method method : methods) {
                ResisterTx annotation = getRegisterAnnotation(method);
                if (annotation != null) {
                    if (!registerDetailMap.containsKey(annotation.txType())) {
                        registerDetailMap.put(annotation.txType(), new TxRegisterDetail(annotation.txType()));
                    }
                    if (annotation.methodType().equals(TxMethodType.COMMIT)) {
                        registerDetailMap.get(annotation.txType()).setCommitCmd(annotation.methodName());
                    } else if (annotation.methodType().equals(TxMethodType.VALID)) {
                        registerDetailMap.get(annotation.txType()).setValidateCmd(annotation.methodName());
                    } else if (annotation.methodType().equals(TxMethodType.ROLLBACK)) {
                        registerDetailMap.get(annotation.txType()).setRollbackCmd(annotation.methodName());
                    }
                }
            }
        }
        //todo 向交易管理模块注册交易
    }

    private static ResisterTx getRegisterAnnotation(Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (ResisterTx.class.equals(annotation.annotationType())) {
                return (ResisterTx) annotation;
            }
        }
        return null;
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
                        //.dependencies(ModuleE.LG.abbr, "1.0")
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
