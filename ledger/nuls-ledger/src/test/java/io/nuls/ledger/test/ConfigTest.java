package io.nuls.ledger.test;

import io.nuls.tools.parse.config.IniEntity;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wangkun23 on 2018/11/19.
 */
public class ConfigTest {

    final Logger logger = LoggerFactory.getLogger(ConfigTest.class);

    @Test
    public void load() throws Exception {
        String fileName = "modules.ini";
        InputStream is = ClassLoader.getSystemResourceAsStream(fileName);
        Config cfg = new Config();
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        //ini.load(url);
        try {
            ini.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
        IniEntity iniEntity = new IniEntity(ini);

        logger.info("iniEntity {}", iniEntity);
        logger.info("rocksdb.datapath {}", iniEntity.getCfgValue("db", "rocksdb.datapath"));
    }
}
