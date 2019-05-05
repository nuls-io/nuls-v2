package io.nuls.core.core.config.persist;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Persist;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-18 11:48
 * @Description:
 * 配置项持久化管理类
 * 1.保存配置项到文本文件中，已json<string,string>格式存储。通过拦截器拦截{@link Configuration}注解宿主类的setter方法实现触发修改。通过判断field的{@link Persist}注解判断此field是否需要持久化
 * 2.将配置信息从文本文件中读取出来。
 * 存储目录为{user.dir}/config_tmp，每个{@link Configuration}可以指定一个domain（也就是一个独立的存储文件），通过{@link Configuration#domain}注解属性指定
 *
 */
public class PersistManager {

    static final String CONFIG_FOLDER = "config_tmp";

    /**
     * 持久化配置项
     * @param annotation
     * @param object
     * @param method
     * @param params
     */
    public static synchronized void saveConfigItem(Configuration annotation, Object object, Method method, Object[] params) {
        if(params.length == 0){
            return ;
        }
        Log.info("save config item to disk");
        //获取field name  通过setter方法解析field name
        String filedName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
        try {
            Field field = object.getClass().getSuperclass().getDeclaredField(filedName);
            //判断field是否需要持久化
            Persist persist = field.getAnnotation(Persist.class);
            if (persist == null) {
                //此config item不需要持久化
                return;
            }
            //获取field是否指定了配置项的名字
            Value value = field.getAnnotation(Value.class);
            if (value != null) {
                //如果指定已指定的名字为存储key值
                filedName = value.value();
            }
        } catch (NoSuchFieldException e) {
            Log.warn("not found field :{} in {}", filedName, object.getClass());
            return;
        }
        //获取config存储文件
        //存储的文件夹
        File configDir = new File(CONFIG_FOLDER);
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        if (!configDir.isDirectory()) {
            Log.warn("config is not folder, abort save config item");
            return;
        }
        //获取域名
        String configFileName = annotation.domain();
        File configFile = new File(CONFIG_FOLDER + File.separator + configFileName);
        //将配置存储文件中的数据读取到内存中，如果没有找到配置文件初始化一个空map。
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
        //更新配置项的值
        configValue.put(filedName, String.valueOf(params[0]));
        String configValueJson = null;
        try {
            //转换为json字符串，writerWithDefaultPrettyPrinter方法会对json字符串格式化成更利于阅读的格式
            configValueJson = JSONUtils.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(configValue);
        } catch (JsonProcessingException e) {
            Log.warn("format config value fail.", e);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile)))) {
            //写入文件中
            writer.write(configValueJson);
        } catch (IOException e) {
            Log.error("save config item fail", e);
        }
    }

    /**
     * 将所有配置持久化数据读取到内存中
     * @return
     */
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

    /**
     * 将配置存储文件中的数据读取到Map<String,String>中
     * @param configFile
     * @return
     */
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