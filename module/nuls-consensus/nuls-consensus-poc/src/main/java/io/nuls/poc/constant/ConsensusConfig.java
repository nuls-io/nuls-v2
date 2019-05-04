package io.nuls.poc.constant;

import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;

import java.io.File;

/**
 * 共识模块配置类
 * @author tag
 * @date 2019-03-26
 * */
@Configuration(domain = "consensus")
public class ConsensusConfig {
    /**
     * 初始链配置文件
     * Initial Chain Profile
     * */
    @Value("consensusConfig")
    private ConfigBean configBean;

    private String dataPath;

    private String dataFolder;

    /** 模块code*/
    private String moduleCode;

    /** 主链链ID*/
    private int mainChainId;

    /** 主链主资产ID*/
    private int mainAssetId;

    /** 语言*/
    private String language;

    /** 编码*/
    private String encoding;

    /**
     * 跨链交易手续费主链收取手续费比例
     * Cross-Chain Transaction Fee Proportion of Main Chain Fee Collection
     * */
    private int mainChainCommissionRatio;

    /**
     * 出块节点密码
     * */
    private String password;

    /**
     * 种子节点
     * Seed node
     */
    private String seedNodes;


    public ConfigBean getConfigBean() {
        return configBean;
    }

    public void setConfigBean(ConfigBean configBean) {
        this.configBean = configBean;
    }


    public String getDataFolder() {
        return dataPath + File.separator + dataFolder;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getMainChainCommissionRatio() {
        return mainChainCommissionRatio;
    }

    public void setMainChainCommissionRatio(int mainChainCommissionRatio) {
        this.mainChainCommissionRatio = mainChainCommissionRatio;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSeedNodes() {
        return seedNodes;
    }

    public void setSeedNodes(String seedNodes) {
        this.seedNodes = seedNodes;
    }
}
