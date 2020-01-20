package io.nuls.chain.config;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.rpc.model.ModuleE;

import java.math.BigInteger;

/**
 * @author lanjinsheng
 */
@Component
@Configuration(domain = ModuleE.Constant.CHAIN_MANAGER)
public class NulsChainConfig implements ModuleConfig {

    private String logLevel = "DEBUG";
    private String language;
    private String encoding = "UTF-8";
    /**
     * ROCK DB 数据库文件存储路径
     */
    private String dataPath;
    /**
     * 初始配置参数
     */
    private String chainNameMax;
    private String assetSymbolMax;
    private String assetNameMax;
    private BigInteger assetDepositNuls;
    private BigInteger assetDestroyNuls;
    private String assetDepositNulsDestroyRate;
    private String assetDepositNulsLockRate;

    private String assetInitNumberMin;
    private String assetInitNumberMax;
    private String assetDecimalPlacesMin;
    private String assetDecimalPlacesMax;
    private String assetRecoveryRate;

    private String mainChainId;
    private String chainName;
    private String mainAssetId;
    private String nulsAssetInitNumberMax;
    private String mainSymbol;
    private String addressPrefix;
    private String nulsFeeMainNetRate = "0.6";
    private int nulsFeeMainNetPercent = 60;
    private int nulsFeeOtherNetPercent = 40;
    private String blackHolePublicKey;
    private String defaultDecimalPlaces = "8";

    private int chainAssetsTaskIntervalMinute;

    public int getChainAssetsTaskIntervalMinute() {
        return chainAssetsTaskIntervalMinute;
    }

    public void setChainAssetsTaskIntervalMinute(int chainAssetsTaskIntervalMinute) {
        this.chainAssetsTaskIntervalMinute = chainAssetsTaskIntervalMinute;
    }

    public String getDefaultDecimalPlaces() {
        return defaultDecimalPlaces;
    }

    public void setDefaultDecimalPlaces(String defaultDecimalPlaces) {
        this.defaultDecimalPlaces = defaultDecimalPlaces;
    }

    public String getBlackHolePublicKey() {
        return blackHolePublicKey;
    }

    public void setBlackHolePublicKey(String blackHolePublicKey) {
        this.blackHolePublicKey = blackHolePublicKey;
    }

    public int getNulsFeeMainNetPercent() {
        return nulsFeeMainNetPercent;
    }

    public void setNulsFeeMainNetPercent(int nulsFeeMainNetPercent) {
        this.nulsFeeMainNetPercent = nulsFeeMainNetPercent;
    }

    public int getNulsFeeOtherNetPercent() {
        return nulsFeeOtherNetPercent;
    }

    public void setNulsFeeOtherNetPercent(int nulsFeeOtherNetPercent) {
        this.nulsFeeOtherNetPercent = nulsFeeOtherNetPercent;
    }


    public String getNulsFeeMainNetRate() {
        return nulsFeeMainNetRate;
    }

    public void setNulsFeeMainNetRate(String nulsFeeMainNetRate) {
        this.nulsFeeMainNetRate = nulsFeeMainNetRate;
    }

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

    public BigInteger getAssetDepositNuls() {
        return assetDepositNuls;
    }

    public void setAssetDepositNuls(BigInteger assetDepositNuls) {
        this.assetDepositNuls = assetDepositNuls;
    }

    public BigInteger getAssetDestroyNuls() {
        return assetDestroyNuls;
    }

    public void setAssetDestroyNuls(BigInteger assetDestroyNuls) {
        this.assetDestroyNuls = assetDestroyNuls;
    }

    public String getAssetDepositNulsDestroyRate() {
        return assetDepositNulsDestroyRate;
    }

    public void setAssetDepositNulsDestroyRate(String assetDepositNulsDestroyRate) {
        this.assetDepositNulsDestroyRate = assetDepositNulsDestroyRate;
    }

    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
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

    public String getMainChainId() {
        return mainChainId;
    }

    public void setMainChainId(String mainChainId) {
        this.mainChainId = mainChainId;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public String getMainAssetId() {
        return mainAssetId;
    }

    public void setMainAssetId(String mainAssetId) {
        this.mainAssetId = mainAssetId;
    }

    public String getNulsAssetInitNumberMax() {
        return nulsAssetInitNumberMax;
    }

    public void setNulsAssetInitNumberMax(String nulsAssetInitNumberMax) {
        this.nulsAssetInitNumberMax = nulsAssetInitNumberMax;
    }

    public String getMainSymbol() {
        return mainSymbol;
    }

    public void setMainSymbol(String mainSymbol) {
        this.mainSymbol = mainSymbol;
    }

    public String getChainNameMax() {
        return chainNameMax;
    }

    public void setChainNameMax(String chainNameMax) {
        this.chainNameMax = chainNameMax;
    }

}
