package io.nuls.chain.config;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;

/**
 * @author lanjinsheng
 */
@Configuration(domain = "nulsChain")
public class NulsChainConfig {

    private String logLevel = "DEBUG";
    private String language;
    private String encoding;
    /**
     * ROCK DB 数据库文件存储路径
     */
    @Value("DataPath")
    private String dataPath;
    /**
     * 初始配置参数
     */
    private String assetSymbolMax;
    private String assetNameMax;
    private String assetDepositNuls;
    private String assetDepositNulsDestroyRate;
    private String assetDepositNulsLockRate;

    private String assetInitNumberMin;
    private String assetInitNumberMax;
    private String assetDecimalPlacesMin;
    private String assetDecimalPlacesMax;
    private String assetRecoveryRate;

    private String nulsChainId;
    private String nulsChainName;
    private String nulsAssetId;
    private String nulsAssetInitNumberMax;
    private String nulsAssetSymbol;

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
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

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getAssetSymbolMax() {
        return assetSymbolMax;
    }

    public void setAssetSymbolMax(String assetSymbolMax) {
        this.assetSymbolMax = assetSymbolMax;
    }

    public String getAssetNameMax() {
        return assetNameMax;
    }

    public void setAssetNameMax(String assetNameMax) {
        this.assetNameMax = assetNameMax;
    }

    public String getAssetDepositNuls() {
        return assetDepositNuls;
    }

    public void setAssetDepositNuls(String assetDepositNuls) {
        this.assetDepositNuls = assetDepositNuls;
    }

    public String getAssetDepositNulsDestroyRate() {
        return assetDepositNulsDestroyRate;
    }

    public void setAssetDepositNulsDestroyRate(String assetDepositNulsDestroyRate) {
        this.assetDepositNulsDestroyRate = assetDepositNulsDestroyRate;
    }

    public String getAssetDepositNulsLockRate() {
        return assetDepositNulsLockRate;
    }

    public void setAssetDepositNulsLockRate(String assetDepositNulsLockRate) {
        this.assetDepositNulsLockRate = assetDepositNulsLockRate;
    }

    public String getAssetInitNumberMin() {
        return assetInitNumberMin;
    }

    public void setAssetInitNumberMin(String assetInitNumberMin) {
        this.assetInitNumberMin = assetInitNumberMin;
    }

    public String getAssetInitNumberMax() {
        return assetInitNumberMax;
    }

    public void setAssetInitNumberMax(String assetInitNumberMax) {
        this.assetInitNumberMax = assetInitNumberMax;
    }

    public String getAssetDecimalPlacesMin() {
        return assetDecimalPlacesMin;
    }

    public void setAssetDecimalPlacesMin(String assetDecimalPlacesMin) {
        this.assetDecimalPlacesMin = assetDecimalPlacesMin;
    }

    public String getAssetDecimalPlacesMax() {
        return assetDecimalPlacesMax;
    }

    public void setAssetDecimalPlacesMax(String assetDecimalPlacesMax) {
        this.assetDecimalPlacesMax = assetDecimalPlacesMax;
    }

    public String getAssetRecoveryRate() {
        return assetRecoveryRate;
    }

    public void setAssetRecoveryRate(String assetRecoveryRate) {
        this.assetRecoveryRate = assetRecoveryRate;
    }

    public String getNulsChainId() {
        return nulsChainId;
    }

    public void setNulsChainId(String nulsChainId) {
        this.nulsChainId = nulsChainId;
    }

    public String getNulsChainName() {
        return nulsChainName;
    }

    public void setNulsChainName(String nulsChainName) {
        this.nulsChainName = nulsChainName;
    }

    public String getNulsAssetId() {
        return nulsAssetId;
    }

    public void setNulsAssetId(String nulsAssetId) {
        this.nulsAssetId = nulsAssetId;
    }

    public String getNulsAssetInitNumberMax() {
        return nulsAssetInitNumberMax;
    }

    public void setNulsAssetInitNumberMax(String nulsAssetInitNumberMax) {
        this.nulsAssetInitNumberMax = nulsAssetInitNumberMax;
    }

    public String getNulsAssetSymbol() {
        return nulsAssetSymbol;
    }

    public void setNulsAssetSymbol(String nulsAssetSymbol) {
        this.nulsAssetSymbol = nulsAssetSymbol;
    }
}
