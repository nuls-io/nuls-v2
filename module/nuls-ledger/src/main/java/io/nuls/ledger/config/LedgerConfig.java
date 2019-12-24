package io.nuls.ledger.config;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.rpc.model.ModuleE;

/**
 * @Author: lanjinsheng
 * @Time: 2019-03-14 14:11
 * @Description: 配置文件
 */
@Component
@Configuration(domain = ModuleE.Constant.LEDGER)
public class LedgerConfig implements ModuleConfig {
    private String logLevel = "DEBUG";
    private String language;
    private String encoding;
    private int unconfirmedTxExpired;
    private String blackHolePublicKey;
    private int chainId;
    private int assetId;
    private int assetRegDestroyAmount = 200;
    private int decimals = 8;
    private String  symbol;

    public String getBlackHolePublicKey() {
        return blackHolePublicKey;
    }

    public void setBlackHolePublicKey(String blackHolePublicKey) {
        this.blackHolePublicKey = blackHolePublicKey;
    }

    /**
     * ROCK DB 数据库文件存储路径
     */
    private String dataPath;

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

    public int getUnconfirmedTxExpired() {
        return unconfirmedTxExpired;
    }

    public void setUnconfirmedTxExpired(int unconfirmedTxExpired) {
        this.unconfirmedTxExpired = unconfirmedTxExpired;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public int getAssetRegDestroyAmount() {
        return assetRegDestroyAmount;
    }

    public void setAssetRegDestroyAmount(int assetRegDestroyAmount) {
        this.assetRegDestroyAmount = assetRegDestroyAmount;
    }


    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
