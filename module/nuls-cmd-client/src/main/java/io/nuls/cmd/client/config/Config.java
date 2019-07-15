package io.nuls.cmd.client.config;

import io.nuls.base.api.provider.Provider;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.exception.NulsException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:56
 * @Description:
 */
@Configuration(domain = "cmd_client")
public class Config implements InitializingBean {

    private Integer mainChainId;

    @Value.NotNull
    private Integer chainId;

    @Value.NotNull
    private Integer assetsId;

    //默认资产小数位数
    private Integer decimals;

    @Value.NotNull
    private Provider.ProviderType providerType;

    private String language;

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

    public Provider.ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(Provider.ProviderType providerType) {
        this.providerType = providerType;
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

    @Override
    public void afterPropertiesSet() throws NulsException {
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public BigDecimal toBigUnit(BigInteger val){
        BigDecimal decimal = BigDecimal.TEN.pow(this.getDecimals());
        BigDecimal dval = BigDecimal.valueOf(val.longValue());
        return dval.divide(decimal);
    }

    public BigInteger toSmallUnit(BigDecimal val){
        BigDecimal decimal = BigDecimal.TEN.pow(this.getDecimals());
        return val.multiply(decimal).toBigInteger();
    }

    public BigInteger toSmallUnit(String val){
        return toSmallUnit(new BigDecimal(val));
    }

}
