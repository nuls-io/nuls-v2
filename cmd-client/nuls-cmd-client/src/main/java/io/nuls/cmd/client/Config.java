package io.nuls.cmd.client;

import io.nuls.api.provider.Provider;
import io.nuls.cmd.client.utils.LoggerUtil;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Persist;
import io.nuls.tools.core.annotation.Value;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.I18nUtils;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:56
 * @Description:
 */
@Configuration(domain = "cmd_client")
public class Config implements InitializingBean {

    @Persist
    @Value.NotNull
    private Integer chainId;

    @Value.NotNull
    private Integer assetsId;

    @Value.NotNull
    private Provider.ProviderType providerType;


    private String language;

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

    @Override
    public void afterPropertiesSet() throws NulsException {
//        if(!I18nUtils.hasLanguage(language)){
//            LoggerUtil.logger.error("can't found language package : {}",language);
//            System.exit(0);
//        }
    }
}
