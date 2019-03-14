package io.nuls.tools.core.config;

import java.io.InputStream;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 17:47
 * @Description: 功能描述
 */
public interface ModuleConfigParser {

    /**
     * 解析文件类型的后缀
     * @return
     */
    String fileSuffix();

    /**
     * 解析配置项到map中
     * @param inputStream
     * @return
     */
    Map<String,String> parse(InputStream inputStream) throws Exception;

}
