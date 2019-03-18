package io.nuls.tools.core.config.persist;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Persist;
import io.nuls.tools.core.annotation.Value;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-18 11:48
 * @Description: 功能描述
 */
public class PersistManager {

    static final String CONFIG_FOLDER = "config_tmp";

    public static synchronized void saveConfigItem(Annotation annotation, Object object, Method method, Object[] params) {
        Log.info("save config item to disk");
        //获取field name  通过setter方法解析field name
        String filedName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
        try {
            Field field = object.getClass().getSuperclass().getDeclaredField(filedName);
            Persist persist = field.getAnnotation(Persist.class);
            if (persist == null) {
                //此config item不需要持久化
                return;
            }
            Value value = field.getAnnotation(Value.class);
            if (value != null) {
                filedName = value.value();
            }
        } catch (NoSuchFieldException e) {
            Log.warn("not found field :{} in {}", filedName, object.getClass());
            return;
        }
        //获取config存储文件
        File configDir = new File(CONFIG_FOLDER);
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        if (!configDir.isDirectory()) {
            Log.warn("config is not folder, abort save config item");
            return;
        }
        String configFileName = ((Configuration) annotation).persistDomain();
        File configFile = new File(CONFIG_FOLDER + File.separator + configFileName);
        Map<String, String> configValue;
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                Log.warn("create config file fail.");
                return;
            }
            configValue = new HashMap<>();
        } else {
            configValue = readConfigFileToMap(configFile);
        }
        configValue.put(filedName, String.valueOf(params[0]));
        String configValueJson = null;
        try {
            configValueJson = JSONUtils.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(configValue);
        } catch (JsonProcessingException e) {
            Log.warn("format config value fail.", e);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile)))) {
            writer.write(configValueJson);
        } catch (IOException e) {
            Log.error("save config item fail", e);
        }
    }

    public static Map<String, Map<String, String>> loadPersist() {
        File configDir = new File(CONFIG_FOLDER);
        Map<String, Map<String, String>> res = new HashMap<>();
        if (!configDir.exists() || !configDir.isDirectory()) {
            return new HashMap<>();
        }
        Arrays.stream(configDir.listFiles()).forEach(configFile -> {
            Map<String, String> config = readConfigFileToMap(configFile);
            res.put(configFile.getName(), config);
        });
        return res;
    }

    private static Map<String, String> readConfigFileToMap(File configFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)))) {
            StringBuilder temp = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                temp.append(line);
            }
            return JSONUtils.jsonToMap(temp.toString());
        } catch (IOException e) {
            Log.error("read config item fail", e);
            return new HashMap<>();
        }

    }
}