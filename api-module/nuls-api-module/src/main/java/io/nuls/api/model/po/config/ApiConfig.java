package io.nuls.api.model.po.config;

import io.nuls.tools.core.annotation.Configuration;
import lombok.Data;

@Configuration
@Data
public class ApiConfig {

    /**
     * 编码方式
     */
    private String encoding;

    /**
     * 语言
     */
    private String language;

    /**
     * mongoDB 数据库ip
     */
    private String mongoIp;

    /**
     * mongoDB 数据库端口号
     */
    private int mongoPort;

    private int defaultChainId;

    private int defaultAssetId;

    private String listenerIp;

    private int rpcPort;

}
