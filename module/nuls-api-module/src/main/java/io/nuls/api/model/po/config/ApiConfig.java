package io.nuls.api.model.po.config;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.rpc.model.ModuleE;

@Component
@Configuration(domain = ModuleE.Constant.API_MODULE)
public class ApiConfig implements ModuleConfig {

    /**
     * 编码方式
     */
    private String encoding;
    /**
     * 语言
     */
    private String language;

    /**
     * 数据库Url地址
     */
    private String databaseUrl;

    /**
     * 数据库端口号
     */
    private int databasePort;

    private int chainId;

    private int assetId;

    private int mainChainId;

    private int mainAssetId;

    private String mainSymbol;

    private String chainName;

    private String symbol;

    private int decimals;

    private String listenerIp;

    private int rpcPort;

    private String logLevel;

    private int maxAliveConnect;

    private int maxWaitTime;

    private int connectTimeOut;

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

    public String getListenerIp() {
        return listenerIp;
    }

    public void setListenerIp(String listenerIp) {
        this.listenerIp = listenerIp;
    }

    public int getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public int getDatabasePort() {
        return databasePort;
    }

    public void setDatabasePort(int databasePort) {
        this.databasePort = databasePort;
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

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public int getMaxAliveConnect() {
        return maxAliveConnect;
    }

    public void setMaxAliveConnect(int maxAliveConnect) {
        this.maxAliveConnect = maxAliveConnect;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
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

    public String getMainSymbol() {
        return mainSymbol;
    }

    public void setMainSymbol(String mainSymbol) {
        this.mainSymbol = mainSymbol;
    }

}
