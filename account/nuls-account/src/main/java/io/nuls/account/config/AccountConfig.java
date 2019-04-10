package io.nuls.account.config;

import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Persist;
import io.nuls.tools.core.annotation.Value;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-14 14:11
 * @Description:
 * 配置文件
 */
@Configuration(domain = "account")
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

    private int mainChainId;

    private int mainAssetId;

    /**
     * ROCK DB 数据库文件存储路径
     */
    private String dataPath;

    private ConfigBean chainConfig;

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getKeystoreFolder() {
        return keystoreFolder;
    }

    public void setKeystoreFolder(String keystoreFolder) {
        this.keystoreFolder = keystoreFolder;
    }

    public int getMainChainId() {
        return mainChainId;
    }

    public void setMainChainId(int mainChainId) {
        this.mainChainId = mainChainId;
    }

    public int getMainAssetId() {
        return mainAssetId;
    }

    public void setMainAssetId(int mainAssetId) {
        this.mainAssetId = mainAssetId;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public ConfigBean getChainConfig() {
        return chainConfig;
    }

    public void setChainConfig(ConfigBean chainConfig) {
        this.chainConfig = chainConfig;
    }
}
