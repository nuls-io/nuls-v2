package io.nuls.core.core.config;

import java.io.InputStream;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 17:47
 * @Description: Function Description
 */
public interface ModuleConfigParser {

    String MODULE_NAME = "module.";

    default String getFileName(){
        return MODULE_NAME + fileSuffix();
    }

    /**
     * Parsing file type suffixes
     * @return
     */
    String fileSuffix();

    /**
     * Parsing configuration items tomapin
     * @param fileName
     * @param inputStream
     * @return
     */
    Map<String, Map<String,ConfigurationLoader.ConfigItem>> parse(String fileName,InputStream inputStream) throws Exception;

}
