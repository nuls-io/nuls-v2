package io.nuls.crosschain.nuls.constant;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.crosschain.nuls.model.bo.config.ConfigBean;

import java.io.File;

/**
 * 跨链模块配置类
 * @author tag
 * @date 2019-03-26
 * */
@Component
@Configuration(domain = ModuleE.Constant.CROSS_CHAIN)
public class NulsCrossChainConfig extends ConfigBean implements ModuleConfig {

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

    private int crossCtxType;

    private boolean mainNet;

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getDataFolder() {
        return dataPath + File.separator + dataFolder;
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

    public int getCrossCtxType() {
        return crossCtxType;
    }

    public void setCrossCtxType(int crossCtxType) {
        this.crossCtxType = crossCtxType;
    }

    public boolean isMainNet() {
        return mainNet;
    }

    public void setMainNet(boolean mainNet) {
        this.mainNet = mainNet;
    }
}
