package io.nuls.tools.core.config;

import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.config.IniEntity;
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
    public Map<String, String> parse(InputStream inputStream) throws Exception {
        Config cfg = new Config();
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        ini.load(inputStream);
        Map<String,String> res = new HashMap<>();
        ini.values().forEach(s->{
            s.entrySet().forEach(item->{
                res.put(item.getKey(),item.getValue());
            });
        });
        return res;
    }
}
