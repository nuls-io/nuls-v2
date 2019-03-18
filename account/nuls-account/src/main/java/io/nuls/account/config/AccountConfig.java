package io.nuls.account.config;

import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Persist;
import io.nuls.tools.core.annotation.Value;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-14 14:11
 * @Description:
 * 配置文件
 */
@Configuration(persistDomain = "account")
@Data
@Persist
public class AccountConfig {

    /**
     *  编码方式
     */
    private String encoding;

    /**
     * 语言
     */
    private String language;

    /**
     * key store 存储文件夹
     */
    private String keystoreFolder;

    private String kernelUrl;

    private String rocksDbDataPath;

    private int mainChainId;

    private int mainAssetId;

    /**
     * ROCK DB 数据库文件存储路径
     */
    @Value("DataPath")
    private String dataPath;


    private ConfigBean chainConfig;


}
