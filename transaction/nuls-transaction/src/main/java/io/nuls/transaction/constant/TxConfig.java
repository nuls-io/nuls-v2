package io.nuls.transaction.constant;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;
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
    /** 语言*/
    private String language;
    /** 编码*/
    private String encoding;
    /** 跨链交易打包确认后需要达到的最低阈值高度才生效*/
    private long ctxEffectThreshold;
    /** 跨链验证通过率百分比, 跨链通过率 */
    private String crossVerifyResultPassRat;
    /** 链内通过率 */
    private String chainNodesResultPassRate;
    /** 友链链内最近N个出块者阈值*/
    private int recentPackagerThreshold;
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

    public long getCtxEffectThreshold() {
        return ctxEffectThreshold;
    }

    public void setCtxEffectThreshold(long ctxEffectThreshold) {
        this.ctxEffectThreshold = ctxEffectThreshold;
    }

    public String getCrossVerifyResultPassRat() {
        return crossVerifyResultPassRat;
    }

    public void setCrossVerifyResultPassRat(String crossVerifyResultPassRat) {
        this.crossVerifyResultPassRat = crossVerifyResultPassRat;
    }

    public String getChainNodesResultPassRate() {
        return chainNodesResultPassRate;
    }

    public void setChainNodesResultPassRate(String chainNodesResultPassRate) {
        this.chainNodesResultPassRate = chainNodesResultPassRate;
    }

    public int getRecentPackagerThreshold() {
        return recentPackagerThreshold;
    }

    public void setRecentPackagerThreshold(int recentPackagerThreshold) {
        this.recentPackagerThreshold = recentPackagerThreshold;
    }

    public long getUnconfirmedTxExpireMs() {
        return unconfirmedTxExpireMs;
    }

    public void setUnconfirmedTxExpireMs(long unconfirmedTxExpireMs) {
        this.unconfirmedTxExpireMs = unconfirmedTxExpireMs;
    }


}
