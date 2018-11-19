package io.nuls.ledger.config;

import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
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

    @Override
    public void afterPropertiesSet() throws NulsException {
        logger.info("AppInitializing##");
    }

    public IniEntity loadIni(String fileName) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(fileName);
        Config cfg = new Config();
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        //ini.load(url);
        ini.load(is);
        return new IniEntity(ini);
    }
}
