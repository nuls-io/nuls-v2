package io.nuls.account.config;

import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Persist;
import io.nuls.core.rpc.model.ModuleE;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-14 14:11
 * @Description: 配置文件
 */
@Component
@Configuration(domain = ModuleE.Constant.ACCOUNT)
@Persist
public class AccountConfig implements ModuleConfig {

    /**
     * 编码方式
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

    private String addressPrefix;

    private String blackHolePublicKey;

    public String getBlackHolePublicKey() {
        return blackHolePublicKey;
    }

    public void setBlackHolePublicKey(String blackHolePublicKey) {
        this.blackHolePublicKey = blackHolePublicKey;
    }

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

    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    public ConfigBean getChainConfig() {
        ConfigBean configBean = new ConfigBean();
        configBean.setAssetId(assetId);
        configBean.setChainId(chainId);
        return configBean;
    }
}
