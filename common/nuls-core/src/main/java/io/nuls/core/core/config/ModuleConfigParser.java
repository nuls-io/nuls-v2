package io.nuls.core.core.config;

import java.io.InputStream;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 17:47
 * @Description: 功能描述
 */
public interface ModuleConfigParser {

    String MODULE_NAME = "module.";

    default String getFileName(){
        return MODULE_NAME + fileSuffix();
    }

    /**
     * 解析文件类型的后缀
     * @return
     */
    String fileSuffix();

    /**
     * 解析配置项到map中
     * @param fileName
     * @param inputStream
     * @return
     */
    Map<String, Map<String,ConfigurationLoader.ConfigItem>> parse(String fileName,InputStream inputStream) throws Exception;

}
