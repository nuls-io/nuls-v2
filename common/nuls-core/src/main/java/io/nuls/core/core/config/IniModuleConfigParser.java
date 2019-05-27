package io.nuls.core.core.config;

import io.nuls.core.log.Log;
import org.ini4j.Config;
import org.ini4j.Ini;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 20:59
 * @Description: 功能描述
 */
public class IniModuleConfigParser implements ModuleConfigParser {
    @Override
    public String fileSuffix() {
        return "ini";
    }

    @Override
    public Map<String, Map<String,ConfigurationLoader.ConfigItem>> parse(String configFile,InputStream inputStream) throws Exception {
        Config cfg = new Config();
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        ini.load(inputStream);
        Map<String,Map<String,ConfigurationLoader.ConfigItem>> res = new HashMap<>(ini.size());
        ini.values().forEach(s-> {
            Map<String,ConfigurationLoader.ConfigItem> domainValues = new HashMap<>(s.size());
            s.forEach((key, value) -> domainValues.put(key, new ConfigurationLoader.ConfigItem(configFile, value)));
            res.put(s.getName(),domainValues);
        });
        Log.debug("{},加载配置：{}",configFile,res);
        return res;
    }
}
