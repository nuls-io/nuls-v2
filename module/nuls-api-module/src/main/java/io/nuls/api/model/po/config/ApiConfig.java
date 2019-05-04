package io.nuls.api.model.po.config;

import io.nuls.tools.core.annotation.Configuration;

@Configuration(domain = "apiModule")
public class ApiConfig {

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

    private int defaultChainId;

    private int defaultAssetId;

    private String listenerIp;

    private int rpcPort;

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

    public int getDefaultChainId() {
        return defaultChainId;
    }

    public void setDefaultChainId(int defaultChainId) {
        this.defaultChainId = defaultChainId;
    }

    public int getDefaultAssetId() {
        return defaultAssetId;
    }

    public void setDefaultAssetId(int defaultAssetId) {
        this.defaultAssetId = defaultAssetId;
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
}
