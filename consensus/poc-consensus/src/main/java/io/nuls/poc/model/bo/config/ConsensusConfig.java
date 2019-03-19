package io.nuls.poc.model.bo.config;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;

/**
 * 共识模块配置类
 * @author tag
 * */
@Configuration(persistDomain = "consensus")
public class ConsensusConfig {
    /**
     * 初始链配置文件
     * Initial Chain Profile
     * */
    @Value("consensusConfig")
    private ConfigBean configBean;


    public ConfigBean getConfigBean() {
        return configBean;
    }

    public void setConfigBean(ConfigBean configBean) {
        this.configBean = configBean;
    }

}
