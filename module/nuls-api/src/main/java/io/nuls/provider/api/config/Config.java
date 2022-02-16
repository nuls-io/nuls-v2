package io.nuls.provider.api.config;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Value;

import static io.nuls.provider.api.constant.SdkConstant.SDK_API;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:56
 * @Description:
 */
@Component
@Configuration(domain = SDK_API)
public class Config implements ModuleConfig {

    private String providerType;

    private Integer mainChainId;

    @Value.NotNull
    private Integer chainId;

    @Value.NotNull
    private Integer assetsId;

    private String chainName;

    private String language;

    private String addressPrefix;

    private String symbol;

    private int decimals;

    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isMainChain() {
        return chainId.equals(mainChainId);
    }

    public Integer getChainId() {
        return chainId;
    }

    public void setChainId(Integer chainId) {
        this.chainId = chainId;
    }

    public Integer getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(Integer assetsId) {
        this.assetsId = assetsId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getMainChainId() {
        return mainChainId;
    }

    public void setMainChainId(Integer mainChainId) {
        this.mainChainId = mainChainId;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
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
}
