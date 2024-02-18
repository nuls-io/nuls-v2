package io.nuls.core.core.config;

import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.config.persist.PersistManager;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 17:36
 * @Description: Load the configuration file and read it askey-valueFormal attribute list
 * Load order, priority from bottom to top, overwrite those loaded first with the same attribute name
 * readresourceUnder the pathmodule.* file
 * readuser.dirRelative pathmodule.* file
 * jvm-optionRead fromactive.moduleThis item should be configured with an absolute path to a file
 * * support json,properties,ncf 3The priority for each format isncf,properties,json
 * <p>
 * After reading, inject the attributespringIn the management class, injection is divided into two situations
 * 1.similarspringofConfigurationAnnotate classes. If a class is annotated once, it will be compared with the property name of the class through the parameter name of the configuration item. If it is consistent, it will be injected. If the type does not match during the injection process, an exception will be thrown
 * 2.The property configuration in the class includesValueAnnotation, injecting values into class properties
 */
@Component
public class ConfigurationLoader {

    public static class ConfigItem {

        String value;

        String configFile;

        public ConfigItem(String configFile, String value) {
            this.configFile = configFile;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getConfigFile() {
            return configFile;
        }
    }

    private static final String JVM_OPTION_ACTIVE_MODULE = "active.config";

    public static final String GLOBAL_DOMAIN = "global";

    /**
     * Store parsed configuration items
     */
    Map<String, Map<String, ConfigItem>> configData = new HashMap<>();

    Map<String, Map<String, String>> persistConfigData = new HashMap<>();

    Map<String, ModuleConfigParser> parserMap = new HashMap<>();

    public ConfigurationLoader() {
        configData.put(GLOBAL_DOMAIN, new HashMap<>());
        ModuleConfigParser json = new JsonModuleConfigParser();
        parserMap.put(json.fileSuffix(), json);
        ModuleConfigParser ini = new IniModuleConfigParser();
        parserMap.put(ini.fileSuffix(), ini);
        ModuleConfigParser ncf = new NcfModuleConfigParser();
        parserMap.put(ncf.fileSuffix(), ncf);
        ModuleConfigParser properties = new PropertiesModuleConfigParser();
        parserMap.put(properties.fileSuffix(), properties);

    }


    public void load() {
        loadResourceModule();
        loadJarPathModule();
        loadJvmActiveModuleFile();
        loadJvmOptionConfigItem();
        loadForPersist();
        if (configData.isEmpty()) {
            Log.info("config item list is empty");
            return;
        }
//        Log.info("config item list:");
    }

    /**
     * adoptjvm option -DXXX=XXX How to set configuration items
     */
    private void loadJvmOptionConfigItem() {
        configData.values().forEach(configItemList -> {
            configItemList.entrySet().stream().forEach(entry -> {
                if (StringUtils.isNotBlank(System.getProperty(entry.getKey()))) {
                    configItemList.put(entry.getKey(), new ConfigItem("JAVA_OPTS:-D" + entry.getKey(), System.getProperty(entry.getKey())));
                }
            });

        });
    }

    private void loadForPersist() {
        persistConfigData = PersistManager.loadPersist();
    }

    private void loadJvmActiveModuleFile() {
        String fileName = System.getProperty(JVM_OPTION_ACTIVE_MODULE);
        if (StringUtils.isNotBlank(fileName)) {
            parserMap.forEach((key, value) -> {
                if (fileName.endsWith(key)) {
                    loadForFile(fileName, value);
                }
            });
        }
    }

    private void loadJarPathModule() {
        parserMap.forEach((key, value) -> loadForFile(value.getFileName(), value));
    }

    private void loadForFile(String fileName, ModuleConfigParser parser) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            try {
                Log.info("found config file : {}", file.getAbsolutePath());
                mergeConfigItem(parser.parse(file.getAbsolutePath(), new FileInputStream(file)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            StringBuilder newFileName = new StringBuilder();
            for (int i = 0; i < fileName.length(); i++) {
                if (i == 0) {
                    newFileName.append(String.valueOf(fileName.charAt(i)).toUpperCase());
                } else {
                    newFileName.append(fileName.charAt(i));
                }
            }
            file = new File(newFileName.toString());
            if (file.exists() && file.isFile()) {
                Log.info("found config file : {}", newFileName.toString());
                try {
                    mergeConfigItem(parser.parse(file.getAbsolutePath(), new FileInputStream(file)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void loadResourceModule() {
        parserMap.forEach((key, value) -> {
            URL url = getClass().getClassLoader().getResource(value.getFileName());
            if (url == null) {
                return;
            }
            Log.info("found config file : {}", value.getFileName());
            try {
                mergeConfigItem(value.parse(url.getPath(), url.openStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public Map<String, Map<String, ConfigItem>> getConfigData() {
        return configData;
    }

    public String getValue(String domain, String key) {
        return getValueForOptional(domain,key).orElse(null);
    }

    public Optional<String> getValueForOptional(String domain, String key) {
        ConfigItem configItem = getConfigItem(domain, key);
        if(configItem == null){
            return Optional.empty();
        }
        return Optional.of(configItem.value);
    }

    public String getValue(String key) {
        return getValue(GLOBAL_DOMAIN, key);
    }

    public Optional<String> getValueForOptional(String key) {
        return getValueForOptional(GLOBAL_DOMAIN, key);
    }

    public ConfigItem getConfigItemForGolbal(String key) {
        return configData.get(GLOBAL_DOMAIN).get(key);
    }

    public ConfigItem getConfigItem(String key) {
        List<Map<String, ConfigItem>> dataList = configData.entrySet().stream().filter(entry -> !entry.getKey().equals(GLOBAL_DOMAIN)).map(e -> e.getValue()).collect(Collectors.toList());
        for (Map<String, ConfigItem> items : dataList) {
            if (items.containsKey(key)) {
                return items.get(key);
            }
        }
        return getConfigItemForGolbal(key);
    }

    public ConfigItem getConfigItem(String domain, String key) {
        if (!configData.containsKey(domain)) {
            ConfigItem res = getConfigItemForGolbal(key);
            return res;
        }
        ConfigItem item = configData.get(domain).get(key);
        if (item == null) {
            ConfigItem res = getConfigItemForGolbal(key);
            return res;
        }
        return item;
    }

    public ConfigItem getConfigItemForCore(String key) {
        ConfigItem item = null;
        for (String ncDomain : BaseConstant.NULS_CORES_DOMAINS) {
            if (!configData.containsKey(ncDomain)) {
                continue;
            } else {
                item = configData.get(ncDomain).get(key);
            }
            if (item != null) {
                return item;
            }
        }
        if (item == null) {
            ConfigItem res = getConfigItemForGolbal(key);
            return res;
        }
        return item;
    }

    public ConfigItem getConfigItemForPersist(String persistDomain, String key) {
        Map<String, String> persistConfig = persistConfigData.get(persistDomain);
        if (persistConfig == null) {
            return getConfigItem(persistDomain, key);
        }
        String persistConfigValue = persistConfig.get(key);
        if (persistConfigValue != null) {
            return new ConfigItem("PERSIST", persistConfigValue);
        }
        return getConfigItem(persistDomain, key);
    }

    private void mergeConfigItem(Map<String, Map<String, ConfigItem>> configItems) {
        configItems.entrySet().stream().forEach(entry -> {
            if (!configData.containsKey(entry.getKey())) {
                configData.put(entry.getKey(), entry.getValue());
            } else {
                configData.get(entry.getKey()).putAll(entry.getValue());
            }
        });
    }

}
