package io.nuls.tools.core.config;

import io.nuls.tools.parse.ConfigLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 20:53
 * @Description: 功能描述
 */
public class PropertiesModuleConfigParser implements ModuleConfigParser {
    @Override
    public String fileSuffix() {
        return "properties";
    }

    @Override
    public Map<String, String> parse(InputStream inputStream) throws Exception {
        Properties prop = ConfigLoader.loadProperties(inputStream);
        Map<String,String> res = new HashMap<>();
        prop.entrySet().stream().forEach(entry->{
            res.put(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
        });
        return res;
    }
}
