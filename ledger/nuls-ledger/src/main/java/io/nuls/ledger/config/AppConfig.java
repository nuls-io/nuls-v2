package io.nuls.ledger.config;

import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.ModuleConfig;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.parse.config.IniEntity;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wangkun23 on 2018/11/19.
 */
public class AppConfig {

    static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    public static ModuleConfig moduleConfig;

    /**
     * load module ini config
     */
    public static void loadModuleConfig() {
        logger.info("loadModuleConfig......");
        IniEntity moduleIni = loadIni(LedgerConstant.MODULES_CONFIG_FILE);
        ModuleConfig moduleConfig = new ModuleConfig();

        try {
            String section = "database";
            String databaseDir = moduleIni.getCfgValue(section, "database.dir");
            if (StringUtils.isNotBlank(databaseDir)) {
                moduleConfig.setDatabaseDir(databaseDir);
            }
            String reset = moduleIni.getCfgValue(section, "database.reset");
            if (StringUtils.isNotBlank(reset)) {
                moduleConfig.setDatabaseReset(Boolean.valueOf(reset));
            }
            String dbVersion = moduleIni.getCfgValue(section, "database.version");
            if (StringUtils.isNotBlank(dbVersion)) {
                moduleConfig.setDatabaseVersion(Integer.valueOf(dbVersion));
            }

            /**
             * kernel config
             */
            String kernelSection = "kernel";
            String kernelHost = moduleIni.getCfgValue(kernelSection, "host");
            if (StringUtils.isNotBlank(kernelHost)) {
                moduleConfig.setKernelHost(kernelHost);
            }
            String kernelPort = moduleIni.getCfgValue(kernelSection, "port");
            if (StringUtils.isNotBlank(kernelPort)) {
                moduleConfig.setKernelPort(Integer.valueOf(kernelPort));
            }

            logger.info("moduleConfig is {}", moduleConfig);
        } catch (Exception e) {
            logger.error("load module ini failed.", e);
        }
        AppConfig.moduleConfig = moduleConfig;
    }

    /**
     * load ini config file from class path
     *
     * @param fileName
     * @throws IOException
     */
    public static IniEntity loadIni(String fileName) {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName);
        Config cfg = new Config();
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
            ini.load(inputStream);
        } catch (IOException e) {
            logger.error("load module ini failed.", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("load module ini failed.", e);
                }
            }
        }
        return new IniEntity(ini);
    }
}
