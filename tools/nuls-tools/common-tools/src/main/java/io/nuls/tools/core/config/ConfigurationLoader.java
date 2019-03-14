package io.nuls.tools.core.config;

import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 17:36
 * @Description:
 *    加载配置文件，将配置文件读取成key-value形式的属性列表
 *    加载顺序，优先级从下到上，属性名字相同后加载的覆盖先加载的
 *    读取resource路径下的module.* 文件
 *    读取user.dir相对路径下的module.* 文件
 *    jvm-option中读取active.module项，此项应配置一个文件绝对路径
 *    * 支持 json,properties,ncf 3种格式，优先级分别是ncf,properties,json
 *
 *    读取完成后，将属性注入spring管理的类中，分两种情况注入
 *    1.类似spring的Configuration注解类，发现类有次注解后，会通过配置项的参数名与类的属性名比对，一致的就注入，注入过程中类型不匹配的情况抛出异常
 *    2.类中属性配置有Value注解，注入值到类属性中
 */
@Component
public class ConfigurationLoader {

    private static final String MODULE_NAME = "module.";

    private static final String JVM_OPTION_ACTIVE_MODULE = "active.module";

    /**
     * 存储解析好的配置项
     */
    Map<String,String> configData = new HashMap<>();

    Map<String,ModuleConfigParser> parserMap = new HashMap<>();

    public ConfigurationLoader(){
        ModuleConfigParser json = new JsonModuleConfigParser();
        parserMap.put(json.fileSuffix(),json);
        ModuleConfigParser ini = new IniModuleConfigParser();
        parserMap.put(ini.fileSuffix(),ini);
        ModuleConfigParser ncf = new NcfModuleConfigParser();
        parserMap.put(ncf.fileSuffix(),ncf);
        ModuleConfigParser properties = new PropertiesModuleConfigParser();
        parserMap.put(properties.fileSuffix(),properties);
    }


    public void load(){
        loadResourceModule();
        loadJarPathModule();
        loadJvmOptionActiveModule();
    }


    private void loadJvmOptionActiveModule() {
        String fileName = System.getProperty(JVM_OPTION_ACTIVE_MODULE);
        if(StringUtils.isNotBlank(fileName)){
            parserMap.entrySet().forEach(entry->{
                if(fileName.endsWith(entry.getKey())){
                    loadForFile(fileName,entry.getValue());
                }
            });
        }
    }

    private void loadJarPathModule() {
        parserMap.entrySet().forEach(parserEntry -> {
            String fileName = MODULE_NAME + parserEntry.getKey();
            loadForFile(fileName,parserEntry.getValue());
        });
    }

    private void loadForFile(String fileName,ModuleConfigParser parser){
        File file = new File(fileName);
        if(file.exists() && file.isFile()){
            try {
                configData.putAll(parser.parse(new FileInputStream(new File(fileName))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadResourceModule() {
        parserMap.entrySet().forEach(parserEntry->{
            String fileName = MODULE_NAME + parserEntry.getKey();
            URL url = getClass().getClassLoader().getResource(fileName);
            if(url == null){
                return ;
            }
            try {
                configData.putAll(parserEntry.getValue().parse(url.openStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public String getValue(String key){
        return configData.get(key);
    }

}
