package io.nuls.ledger.config;

import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.ModuleConfig;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
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
@Component
public class AppInitializing implements InitializingBean {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppConfig appConfig;

    @Override
    public void afterPropertiesSet() throws NulsException {
        logger.info("AppInitializing......");
        IniEntity moduleIni = loadIni(LedgerConstant.MODULES_CONFIG_FILE);
        ModuleConfig moduleConfig = new ModuleConfig();

        try {
            String section = "database";
            String databaseDir = moduleIni.getCfgValue(section, "database.dir");
            if (StringUtils.isNotBlank(databaseDir)) {
                moduleConfig.setDatabaseDir(databaseDir);
            }
            String databaseName = moduleIni.getCfgValue(section, "database.name");
            if (StringUtils.isNotBlank(databaseName)) {
                moduleConfig.setDatabaseName(databaseName);
            }
            String reset = moduleIni.getCfgValue(section, "database.reset");
            if (StringUtils.isNotBlank(reset)) {
                moduleConfig.setDatabaseReset(Boolean.valueOf(reset));
            }
            String dbVersion = moduleIni.getCfgValue(section, "database.version");
            if (StringUtils.isNotBlank(dbVersion)) {
                moduleConfig.setDatabaseVersion(Integer.valueOf(dbVersion));
            }
            logger.info("moduleConfig is {}", moduleConfig);
        } catch (Exception e) {
            logger.error("load module ini failed.", e);
        }

        appConfig.setModuleConfig(moduleConfig);
    }

    /**
     * load ini config file from class path
     *
     * @param fileName
     * @throws IOException
     */
    public IniEntity loadIni(String fileName) {
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
