/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
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
