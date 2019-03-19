package io.nuls.poc.config;

import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;

import java.io.File;

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

    private String DataPath;

    private String dataFolder;


    public ConfigBean getConfigBean() {
        return configBean;
    }

    public void setConfigBean(ConfigBean configBean) {
        this.configBean = configBean;
    }


    public String getDataFolder() {
        return DataPath + File.separator + dataFolder;
    }


}
