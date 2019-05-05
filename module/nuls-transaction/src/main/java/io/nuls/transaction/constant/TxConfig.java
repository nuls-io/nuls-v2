package io.nuls.transaction.constant;

import io.nuls.core.core.annotation.Configuration;
import io.nuls.transaction.model.bo.config.ConfigBean;

import java.io.File;

/**
 * Transaction module setting
 * @author: Charlie
 * @date: 2019/03/14
 */
@Configuration(domain = "transaction")
public class TxConfig {
    /** 当前链默认配置*/
    private ConfigBean chainConfig;
    /**
     * ROCK DB 数据库文件存储路径
     */
    private String dataPath;
    /** 交易模块数据根目录*/
    private String txDataRoot;
    /** 模块code*/
    private String moduleCode;
    /** 主链链ID*/
    private int mainChainId;
    /** 主链主资产ID*/
    private int mainAssetId;
    /** 编码*/
    private String encoding;
    /** 未确认交易过期毫秒数-30分钟 */
    private long unconfirmedTxExpireMs;


    public ConfigBean getChainConfig() {
        return chainConfig;
    }

    public void setChainConfig(ConfigBean chainConfig) {
        this.chainConfig = chainConfig;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getTxDataRoot() {
        return dataPath + File.separator + txDataRoot;
    }

    public void setTxDataRoot(String txDataRoot) {
        this.txDataRoot = txDataRoot;
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

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public long getUnconfirmedTxExpireMs() {
        return unconfirmedTxExpireMs;
    }

    public void setUnconfirmedTxExpireMs(long unconfirmedTxExpireMs) {
        this.unconfirmedTxExpireMs = unconfirmedTxExpireMs;
    }


}
