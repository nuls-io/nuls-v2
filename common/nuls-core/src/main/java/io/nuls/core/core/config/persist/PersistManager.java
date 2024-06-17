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
 * Configuration item persistence management class
 * 1.Save configuration items to a text file, alreadyjson<string,string>Format storage. Intercept through interceptors{@link Configuration}Annotate the host class'ssetterMethod implementation triggers modification. By judgmentfieldof{@link Persist}Annotate to determine thisfieldDo you need persistence
 * 2.Read configuration information from a text file.
 * The storage directory is{user.dir}/config_tmp, each{@link Configuration}You can specify adomain（That is, an independent storage file）, through{@link Configuration#domain}Annotation attribute specification
 *
 */
public class PersistManager {

    static final String CONFIG_FOLDER = "config_tmp";

    /**
     * Persistence configuration items
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
        //obtainfield name  adoptsetterMethod Analysisfield name
        String filedName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
        try {
            Field field = object.getClass().getSuperclass().getDeclaredField(filedName);
            //judgefieldDo you need persistence
            Persist persist = field.getAnnotation(Persist.class);
            if (persist == null) {
                //thisconfig itemNo persistence required
                return;
            }
            //obtainfieldHas the name of the configuration item been specified
            Value value = field.getAnnotation(Value.class);
            if (value != null) {
                //If a specified name is specified as the storagekeyvalue
                filedName = value.value();
            }
        } catch (NoSuchFieldException e) {
            Log.warn("not found field :{} in {}", filedName, object.getClass());
            return;
        }
        //obtainconfigStore files
        //Storage folder
        File configDir = new File(CONFIG_FOLDER);
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        if (!configDir.isDirectory()) {
            Log.warn("config is not folder, abort save config item");
            return;
        }
        //Obtain domain name
        String configFileName = annotation.domain();
        File configFile = new File(CONFIG_FOLDER + File.separator + configFileName);
        //Read the data from the configuration storage file into memory, and if the configuration file is not found, initialize an empty onemap.
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
        //Update the value of configuration items
        configValue.put(filedName, String.valueOf(params[0]));
        String configValueJson = null;
        try {
            //Convert tojsonString,writerWithDefaultPrettyPrinterThe method will have an impact onjsonFormat strings in a more readable format
            configValueJson = JSONUtils.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(configValue);
        } catch (JsonProcessingException e) {
            Log.warn("format config value fail.", e);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile)))) {
            //Write to file
            writer.write(configValueJson);
        } catch (IOException e) {
            Log.error("save config item fail", e);
        }
    }

    /**
     * Read all persistent configuration data into memory
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
     * Read the data from the configuration storage file toMap<String,String>in
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
