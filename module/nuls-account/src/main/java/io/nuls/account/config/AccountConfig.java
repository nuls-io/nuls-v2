package io.nuls.account.config;

import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Persist;

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

    private int chainId;

    private int assetId;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    /**
     * ROCK DB 数据库文件存储路径
     */
    private String dataPath;

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
        ConfigBean configBean = new ConfigBean();
        configBean.setAssetsId(assetId);
        configBean.setChainId(chainId);
        return configBean;
    }
}
