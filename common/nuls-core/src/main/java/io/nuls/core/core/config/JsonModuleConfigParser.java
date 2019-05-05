package io.nuls.core.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.core.io.IoUtils;
import io.nuls.core.parse.JSONUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 17:51
 * @Description: 功能描述
 */
public class JsonModuleConfigParser implements ModuleConfigParser {
    @Override
    public String fileSuffix() {
        return "json";
    }

    @Override
    public Map<String,Map<String, ConfigurationLoader.ConfigItem>> parse(String configFile,InputStream inputStream) {
        try {
            String configJson = IoUtils.readRealPath(inputStream);
            Map<String,Object> data = JSONUtils.json2map(configJson);
            Map<String,ConfigurationLoader.ConfigItem> res = new HashMap<>(data.size());
            data.forEach((key, value) -> {
                try {
                    if (ConfigSetting.isPrimitive(value.getClass())) {
                        res.put(key, new ConfigurationLoader.ConfigItem(configFile, String.valueOf(value)));
                    } else {
                        res.put(key, new ConfigurationLoader.ConfigItem(configFile, JSONUtils.obj2json(value)));
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("json配置文件解析错误：" + key);
                }
            });
            return Map.of(ConfigurationLoader.GLOBAL_DOMAIN,res);
        } catch (Exception e) {
            throw new RuntimeException("json配置文件解析错误");
        }
    }
}
